package pe.mimascota.app.backup

import android.content.Context
import android.net.Uri
import pe.mimascota.app.data.PetDao
import kotlinx.coroutines.flow.first

/** User-directed export using Android's document picker; no Drive permission required. */
class BackupExporter(private val context: Context, private val dao: PetDao) {
    suspend fun export(uri: Uri) {
        val json = BackupManifest.encode(dao.observePets().first())
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("No se pudo abrir el archivo")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
    }
}
