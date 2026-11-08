package pe.mimascota.app.backup

import org.json.JSONArray
import org.json.JSONObject
import pe.mimascota.app.data.BackupSnapshot

/** Portable JSON metadata; references to photos are not photo contents. */
object BackupManifest {
    const val VERSION = 2
    fun encode(snapshot: BackupSnapshot): String {
        fun rows(items: List<JSONObject>): JSONArray = JSONArray().apply { items.forEach { put(it) } }
        return JSONObject()
            .put("version", VERSION)
            .put("exportedAt", java.time.Instant.now().toString())
            .put("pets", rows(snapshot.pets.map { JSONObject().put("id", it.id).put("name", it.name).put("species", it.species).put("birthDate", it.birthDate).put("createdAt", it.createdAt) }))
            .put("vaccines", rows(snapshot.vaccines.map { JSONObject().put("id", it.id).put("petId", it.petId).put("name", it.name).put("appliedAt", it.appliedAt).put("dueAt", it.dueAt) }))
            .put("reminders", rows(snapshot.reminders.map { JSONObject().put("id", it.id).put("petId", it.petId).put("title", it.title).put("dueAt", it.dueAt).put("completed", it.completed) }))
            .put("expenses", rows(snapshot.expenses.map { JSONObject().put("id", it.id).put("petId", it.petId).put("title", it.title).put("amountCents", it.amountCents).put("date", it.date) }))
            .put("photos", rows(snapshot.photos.map { JSONObject().put("id", it.id).put("petId", it.petId).put("uri", it.uri).put("createdAt", it.createdAt) }))
            .toString()
    }
}
