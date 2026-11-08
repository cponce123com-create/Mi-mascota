package pe.mimascota.app.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.mimascota.app.data.BackupDao

/** SAF export: no broad storage or Google Drive permissions. */
class BackupExporter(private val context: Context, private val dao: BackupDao) {
    suspend fun export(uri: Uri) = withContext(Dispatchers.IO) {
        val json = BackupManifest.encode(dao.snapshot())
        val stream = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("No se pudo abrir el archivo")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
    }
}
