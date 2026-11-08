package pe.mimascota.app.backup

import org.json.JSONArray
import org.json.JSONObject
import pe.mimascota.app.data.PetEntity

/** Backup metadata only. Photo bytes must be exported separately with explicit user consent. */
object BackupManifest {
    const val VERSION = 1
    fun encode(pets: List<PetEntity>): String {
        val rows = JSONArray()
        pets.forEach { pet ->
            rows.put(JSONObject().put("id", pet.id).put("name", pet.name)
                .put("species", pet.species).put("birthDate", pet.birthDate))
        }
        return JSONObject().put("version", VERSION).put("pets", rows).toString()
    }
}
