package pe.mimascota.app.backup

import org.json.JSONArray
import org.json.JSONObject
import pe.mimascota.app.data.*
import java.time.LocalDate

/** Portable JSON metadata; references to photos are not photo contents. */
object BackupManifest {
    const val VERSION = 3
    fun encode(snapshot: BackupSnapshot): String {
        fun rows(items: List<JSONObject>): JSONArray = JSONArray().apply { items.forEach { put(it) } }
        return JSONObject()
            .put("version", VERSION)
            .put("exportedAt", java.time.Instant.now().toString())
            .put("pets", rows(snapshot.pets.map { JSONObject().put("id", it.id).put("name", it.name).put("species", it.species).put("birthDate", it.birthDate).put("createdAt", it.createdAt).put("breed", it.breed).put("weightKg", it.weightKg).put("sterilized", it.sterilized).put("microchip", it.microchip) }))
            .put("vaccines", rows(snapshot.vaccines.map { JSONObject().put("id", it.id).put("petId", it.petId).put("name", it.name).put("appliedAt", it.appliedAt).put("dueAt", it.dueAt) }))
            .put("reminders", rows(snapshot.reminders.map { JSONObject().put("id", it.id).put("petId", it.petId).put("title", it.title).put("dueAt", it.dueAt).put("completed", it.completed) }))
            .put("expenses", rows(snapshot.expenses.map { JSONObject().put("id", it.id).put("petId", it.petId).put("title", it.title).put("amountCents", it.amountCents).put("date", it.date) }))
            .put("photos", rows(snapshot.photos.map { JSONObject().put("id", it.id).put("petId", it.petId).put("uri", it.uri).put("createdAt", it.createdAt) }))
            .toString()
    }
    /** Decode backups v1 and v2. Invalid records fail before any database writes. */
    fun decode(json: String): BackupSnapshot {
        require(json.length <= 5_000_000) { "El respaldo excede 5 MB" }
        val root = JSONObject(json)
        val version = root.getInt("version")
        require(version in 1..VERSION) { "Versión de respaldo no compatible" }

        fun records(key: String): List<JSONObject> {
            val arr = if (version == 1 && key != "pets") JSONArray() else root.getJSONArray(key)
            require(arr.length() <= 5000) { "Demasiados registros en $key" }
            return (0 until arr.length()).map { arr.getJSONObject(it) }
        }
        fun JSONObject.required(key: String, limit: Int = 500): String =
            getString(key).also { require(it.isNotBlank() && it.length <= limit) { "Campo $key inválido" } }
        fun JSONObject.optional(key: String): String? =
            if (!has(key) || isNull(key)) null else getString(key)
        fun date(value: String?): String? = value?.also { LocalDate.parse(it) }
        fun <T> unique(items: List<T>, id: (T) -> String): List<T> {
            require(items.map(id).toSet().size == items.size) { "Identificadores duplicados en respaldo" }
            return items
        }

        val pets = unique(records("pets").map {
            val species = it.required("species")
            require(species in setOf("CAT", "DOG", "OTHER"))
            PetEntity(it.required("id", 80), it.required("name", 80), species,
                date(it.optional("birthDate")), if (it.has("createdAt")) it.getLong("createdAt") else System.currentTimeMillis(),
                it.optional("breed"),
                if (it.has("weightKg") && !it.isNull("weightKg")) it.getDouble("weightKg").toFloat() else null,
                if (it.has("sterilized") && !it.isNull("sterilized")) it.getBoolean("sterilized") else null,
                it.optional("microchip"))
        }) { it.id }
        val vaccines = unique(records("vaccines").map {
            VaccineEntity(it.required("id", 80), it.required("petId", 80),
                it.required("name", 120), date(it.optional("appliedAt")), date(it.optional("dueAt")))
        }) { it.id }
        val reminders = unique(records("reminders").map {
            ReminderEntity(it.required("id", 80), it.required("petId", 80),
                it.required("title", 160), date(it.required("dueAt"))!!, it.getBoolean("completed"))
        }) { it.id }
        val expenses = unique(records("expenses").map {
            val cents = it.getLong("amountCents")
            require(cents > 0)
            ExpenseEntity(it.required("id", 80), it.required("petId", 80),
                it.required("title", 160), cents, date(it.required("date"))!!)
        }) { it.id }
        val photos = unique(records("photos").map {
            val uri = it.required("uri", 2500)
            require(uri.startsWith("content://")) { "URI de imagen no admitida" }
            PhotoEntity(it.required("id", 80), it.required("petId", 80), uri, it.getLong("createdAt"))
        }) { it.id }
        return BackupSnapshot(pets, vaccines, reminders, expenses, photos)
    }

}
