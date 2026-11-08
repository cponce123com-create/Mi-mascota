package pe.mimascota.app.backup

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.data.BackupDao
import pe.mimascota.app.data.BackupSnapshot
import pe.mimascota.app.data.PhotoEntity
import java.io.InputStream
import java.io.OutputStream
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class PreparedPhotoArchive(
    val snapshot: BackupSnapshot,
    val stagingDir: File,
    val images: Map<String, File>
) {
    fun discard() { stagingDir.deleteRecursively() }
}

/**
 * Portable ZIP backup with real image bytes; limits protect against oversized and malicious files.
 * It uses user-selected Android documents, without requesting broad storage access.
 */
class BackupPhotoArchive(private val context: Context, private val dao: BackupDao) {
    companion object {
        const val MAX_IMAGE_BYTES = 10_000_000L
        const val MAX_ARCHIVE_BYTES = 100_000_000L
        private val VALID_ID = Regex("[0-9a-fA-F-]{36}")
        private val PHOTO_PATH = Regex("photos/([0-9a-fA-F-]{36})\\.bin")
    }

    suspend fun export(uri: Uri): Int = withContext(Dispatchers.IO) {
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("No se puede abrir el destino ZIP")
        writeArchive(output)
    }

    suspend fun exportEncrypted(uri: Uri, password: String): Int = withContext(Dispatchers.IO) {
        require(password.length >= 8) { "Usa una contraseña de al menos 8 caracteres" }
        val output = context.contentResolver.openOutputStream(uri, "wt")
            ?: error("No se puede abrir el destino cifrado")
        writeArchive(BackupCrypto.encrypt(output, password))
    }

    private suspend fun writeArchive(output: OutputStream): Int {
        val data = dao.snapshot()
        require(data.photos.size <= 300) { "El álbum supera 300 fotos" }
        var total = 0L
        val included = mutableListOf<PhotoEntity>()
        ZipOutputStream(output.buffered()).use { archive ->
            data.photos.forEach { photo ->
                if (!VALID_ID.matches(photo.id)) return@forEach
                val input = runCatching { context.contentResolver.openInputStream(Uri.parse(photo.uri)) }
                    .getOrNull() ?: return@forEach
                input.use { image ->
                    archive.putNextEntry(ZipEntry("photos/${photo.id}.bin"))
                    var photoSize = 0L
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = image.read(buffer)
                        if (count == -1) break
                        photoSize += count
                        total += count
                        require(photoSize <= MAX_IMAGE_BYTES && total <= MAX_ARCHIVE_BYTES) {
                            "Fotografías demasiado grandes para respaldar"
                        }
                        archive.write(buffer, 0, count)
                    }
                    archive.closeEntry()
                    included.add(photo)
                }
            }
            archive.putNextEntry(ZipEntry("manifest.json"))
            archive.write(BackupManifest.encode(data.copy(photos = included)).toByteArray(Charsets.UTF_8))
            archive.closeEntry()
        }
        included.size
    }

    suspend fun prepare(uri: Uri): PreparedPhotoArchive = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri) ?: error("No se puede abrir el ZIP")
        readArchive(input)
    }

    suspend fun prepareEncrypted(uri: Uri, password: String): PreparedPhotoArchive = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri)
            ?: error("No se puede abrir el respaldo cifrado")
        val temporary = File(context.cacheDir, "verified-${UUID.randomUUID()}.zip")
        try {
            // Read the *entire* authenticated stream before parsing ZIP or writing Room.
            // This validates the GCM tag even if ZipInputStream would stop at central directory.
            BackupCrypto.decrypt(input, password).use { decrypted ->
                temporary.outputStream().buffered().use { out ->
                    val buffer = ByteArray(8192)
                    var size = 0L
                    while (true) {
                        val count = decrypted.read(buffer)
                        if (count == -1) break
                        size += count
                        require(size <= MAX_ARCHIVE_BYTES + 5_000_000L) {
                            "Respaldo cifrado demasiado grande"
                        }
                        out.write(buffer, 0, count)
                    }
                }
            }
            readArchive(temporary.inputStream())
        } finally {
            temporary.delete()
        }
    }

    private fun readArchive(input: InputStream): PreparedPhotoArchive {
        val folder = File(context.cacheDir, "mi-mascota-restore-${UUID.randomUUID()}")
        require(folder.mkdirs()) { "No se pudo preparar el respaldo" }
        val images = mutableMapOf<String, File>()
        var manifest: String? = null
        var total = 0L
        return try {
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(!entry.isDirectory) { "Carpeta no admitida en el ZIP" }
                    if (entry.name == "manifest.json") {
                        require(manifest == null) { "Manifiesto duplicado" }
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = zip.read(buffer)
                            if (count == -1) break
                            require(output.size() + count <= 5_000_000) { "Manifiesto demasiado grande" }
                            output.write(buffer, 0, count)
                        }
                        manifest = output.toString(Charsets.UTF_8.name())
                    } else {
                        val id = PHOTO_PATH.matchEntire(entry.name)?.groupValues?.get(1)
                            ?: error("Entrada ZIP no permitida: ${entry.name}")
                        require(!images.containsKey(id) && images.size < 300) { "Foto duplicada o exceso de fotografías" }
                        val staged = File(folder, "$id.bin")
                        staged.outputStream().buffered().use { out ->
                            val buffer = ByteArray(8192)
                            var size = 0L
                            while (true) {
                                val count = zip.read(buffer)
                                if (count == -1) break
                                size += count
                                total += count
                                require(size <= MAX_IMAGE_BYTES && total <= MAX_ARCHIVE_BYTES) {
                                    "Fotografías demasiado grandes en el ZIP"
                                }
                                out.write(buffer, 0, count)
                            }
                        }
                        images[id] = staged
                    }
                    zip.closeEntry()
                }
            }
            val snapshot = BackupManifest.decode(manifest ?: error("ZIP sin manifiesto"))
            val validIds = snapshot.photos.map { it.id }.toSet()
            require(images.keys.all(validIds::contains)) { "Fotos ajenas al manifiesto" }
            PreparedPhotoArchive(snapshot, folder, images)
        } catch (failure: Exception) {
            folder.deleteRecursively()
            throw failure
        }
    }

    suspend fun restore(prepared: PreparedPhotoArchive) = withContext(Dispatchers.IO) {
        val directory = File(context.filesDir, "photos")
        require(directory.exists() || directory.mkdirs()) { "No se puede crear el álbum interno" }
        val created = mutableListOf<File>()
        try {
            val restored = prepared.snapshot.photos.map { photo ->
                val source = prepared.images[photo.id]
                if (source == null) photo else {
                    val destination = File(directory, "${UUID.randomUUID()}.bin")
                    source.copyTo(destination)
                    created.add(destination)
                    val uri = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".fileprovider", destination)
                    photo.copy(uri = uri.toString())
                }
            }
            dao.restore(prepared.snapshot.copy(photos = restored))
        } catch (failure: Exception) {
            created.forEach { it.delete() }
            throw failure
        } finally {
            prepared.discard()
        }
    }
}
