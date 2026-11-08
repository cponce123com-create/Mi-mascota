package pe.mimascota.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class CommunityPost(val title: String, val body: String)
data class LostPetListing(val name: String, val species: String, val district: String?)

class CommunityApi(private val baseUrl: String) {
    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        require(baseUrl.startsWith("https://")) { "La API requiere HTTPS" }
        val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            if (connection.responseCode !in 200..299) error("Error HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
    suspend fun posts(): List<CommunityPost> {
        val rows = JSONArray(get("/v1/community/posts"))
        return (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            CommunityPost(row.optString("title"), row.optString("body"))
        }
    }
    suspend fun lostPets(): List<LostPetListing> {
        val rows = JSONArray(get("/v1/lost-pets"))
        return (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            val pet = row.getJSONObject("pet")
            LostPetListing(pet.optString("name"), pet.optString("species"),
                row.optString("district").takeIf { it.isNotBlank() && it != "null" })
        }
    }
}
