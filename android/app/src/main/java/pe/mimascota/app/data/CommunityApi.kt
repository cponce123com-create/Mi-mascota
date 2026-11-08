package pe.mimascota.app.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CommunityPost(val id: String, val title: String, val body: String)
data class CommunityComment(val id: String, val body: String)
data class LostPetListing(val name: String, val species: String, val district: String?)
data class AdoptionListing(val id: String, val name: String, val species: String, val description: String, val district: String?)
data class OwnedListing(val id: String, val title: String, val status: String)
data class ModerationItem(val id: String, val kind: String, val title: String, val details: String)
data class AdoptionRequest(val id: String, val message: String, val status: String)

/** Tokens are obtained from Firebase just before protected requests, never stored in Room. */
suspend fun communityToken(): String? = runCatching {
    FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
}.getOrNull()

class CommunityApi(private val baseUrl: String) {
    private suspend fun request(method: String, path: String, body: JSONObject? = null, token: String? = null): String =
        withContext(Dispatchers.IO) {
            require(baseUrl.startsWith("https://")) { "La API requiere HTTPS" }
            val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = method
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("Accept", "application/json")
                if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
                if (body != null) {
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                }
                val status = connection.responseCode
                if (status !in 200..299) error("HTTP $status: " + when (status) {
                    401 -> "inicia sesión de nuevo"
                    403 -> "no tienes permiso"
                    404 -> "registro no encontrado"
                    409 -> "este registro ya existe"
                    429 -> "demasiados intentos; vuelve a probar después"
                    else -> "no se completó la operación"
                })
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }

    suspend fun posts(): List<CommunityPost> {
        val rows = JSONArray(request("GET", "/v1/community/posts"))
        return (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            CommunityPost(it.getString("id"), it.optString("title"), it.optString("body")) } }
    }

    suspend fun lostPets(): List<LostPetListing> {
        val rows = JSONArray(request("GET", "/v1/lost-pets"))
        return (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            val pet = row.getJSONObject("pet")
            LostPetListing(pet.optString("name"), pet.optString("species"),
                row.optString("district").takeIf { it.isNotBlank() && it != "null" })
        }
    }

    suspend fun adoptions(): List<AdoptionListing> {
        val rows = JSONArray(request("GET", "/v1/adoptions"))
        return (0 until rows.length()).map { i ->
            val row = rows.getJSONObject(i)
            AdoptionListing(row.getString("id"), row.getString("petName"), row.getString("species"),
                row.getString("description"), row.optString("district").takeIf { it.isNotBlank() && it != "null" })
        }
    }

    suspend fun comments(postId: String): List<CommunityComment> {
        val rows = JSONArray(request("GET", "/v1/community/posts/$postId/comments"))
        return (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            CommunityComment(it.getString("id"), it.getString("body")) } }
    }

    suspend fun submitComment(postId: String, body: String, token: String) {
        request("POST", "/v1/community/posts/$postId/comments", JSONObject().put("body", body), token)
    }

    suspend fun post(title: String, body: String, token: String) {
        request("POST", "/v1/community/posts",
            JSONObject().put("title", title).put("body", body), token)
    }

    suspend fun syncPet(pet: PetEntity, token: String) {
        val data = JSONObject().put("id", pet.id).put("name", pet.name).put("species", pet.species)
        pet.birthDate?.let { data.put("birthDate", it) }
        request("POST", "/v1/pets", data, token)
    }

    suspend fun submitLost(pet: PetEntity, district: String, details: String, token: String) {
        syncPet(pet, token)
        request("POST", "/v1/lost-pets",
            JSONObject().put("petId", pet.id).put("district", district).put("details", details), token)
    }

    suspend fun submitAdoption(name: String, species: String, description: String, district: String, token: String) {
        val data = JSONObject().put("petName", name).put("species", species)
            .put("description", description).put("district", district)
        request("POST", "/v1/adoptions", data, token)
    }

    suspend fun apply(id: String, message: String, token: String) {
        request("POST", "/v1/adoptions/$id/apply", JSONObject().put("message", message), token)
    }

    suspend fun myAdoptions(token: String): Pair<List<OwnedListing>, List<OwnedListing>> {
        val obj = JSONObject(request("GET", "/v1/adoptions/mine", token = token))
        fun rows(key: String) = obj.getJSONArray(key)
        val listings = rows("listings")
        val applications = rows("applications")
        return Pair(
            (0 until listings.length()).map { i -> listings.getJSONObject(i).let {
                OwnedListing(it.getString("id"), it.getString("petName"), it.getString("status")) } },
            (0 until applications.length()).map { i -> applications.getJSONObject(i).let {
                OwnedListing(it.getString("id"), it.getJSONObject("listing").getString("petName"), it.getString("status")) } }
        )
    }

    suspend fun myLost(token: String): List<OwnedListing> {
        val rows = JSONArray(request("GET", "/v1/lost-pets/mine", token = token))
        return (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            OwnedListing(it.getString("id"), it.getJSONObject("pet").getString("name"),
                it.getString("status") + " / " + it.getString("visibility")) } }
    }

    suspend fun resolveLost(id: String, token: String) {
        request("PATCH", "/v1/lost-pets/$id/resolve", JSONObject(), token)
    }

    suspend fun applicationsFor(id: String, token: String): List<AdoptionRequest> {
        val rows = JSONArray(request("GET", "/v1/adoptions/$id/applications", token = token))
        return (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
            AdoptionRequest(it.getString("id"), it.getString("message"), it.getString("status")) } }
    }

    suspend fun respondApplication(id: String, accept: Boolean, token: String) {
        request("PATCH", "/v1/adoption-applications/$id/respond",
            JSONObject().put("status", if (accept) "ACCEPTED" else "REJECTED"), token)
    }

    suspend fun closeAdoption(id: String, token: String) {
        request("PATCH", "/v1/adoptions/$id/close", JSONObject(), token)
    }
    suspend fun moderationQueue(token: String): List<ModerationItem> {
        val root = JSONObject(request("GET", "/v1/moderation/queue", token = token))
        val items = mutableListOf<ModerationItem>()
        fun collect(key: String, kind: String, title: (JSONObject) -> String, text: (JSONObject) -> String) {
            val rows = root.getJSONArray(key)
            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                items += ModerationItem(row.getString("id"), kind, title(row), text(row))
            }
        }
        collect("posts", "post", { it.optString("title") }, { it.optString("body") })
        collect("lost", "lost", { it.getJSONObject("pet").optString("name") }, { it.optString("details") })
        collect("adoptions", "adoption", { it.optString("petName") }, { it.optString("description") })
        collect("comments", "comment", { it.getJSONObject("post").optString("title") }, { it.optString("body") })
        return items
    }

    suspend fun moderate(item: ModerationItem, approve: Boolean, token: String) {
        require(item.kind in setOf("post", "lost", "adoption", "comment"))
        request("PATCH", "/v1/moderation/${item.kind}/${item.id}",
            JSONObject().put("approve", approve), token)
    }

}
