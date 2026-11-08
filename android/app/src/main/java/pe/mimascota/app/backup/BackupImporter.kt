package pe.mimascota.app.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.mimascota.app.data.BackupDao
import pe.mimascota.app.data.BackupSnapshot
import java.io.ByteArrayOutputStream

/** User-selected SAF import. Never reads more than 5 MB into memory. */
class BackupImporter(private val context: Context, private val dao: BackupDao) {
    suspend fun read(uri: Uri): BackupSnapshot = withContext(Dispatchers.IO) {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        context.contentResolver.openInputStream(uri)?.use { input ->
            while (true) {
                val count = input.read(buffer)
                if (count == -1) break
                require(output.size() + count <= 5_000_000) { "El respaldo supera 5 MB" }
                output.write(buffer, 0, count)
            }
        } ?: error("No se pudo abrir el respaldo")
        BackupManifest.decode(output.toString(Charsets.UTF_8.name()))
    }

    suspend fun restore(snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        dao.restore(snapshot)
    }
}
