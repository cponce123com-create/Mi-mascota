package pe.mimascota.app.backup

import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based AES-256-GCM stream encryption.
 * Magic + per-file 16-byte salt + 12-byte random nonce precede ciphertext + tag.
 * Password is never stored by the app and cannot be recovered by the developer.
 */
object BackupCrypto {
    private val MAGIC = "MMPET001".toByteArray(Charsets.US_ASCII)
    private const val ITERATIONS = 210_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12

    private fun key(password: String, salt: ByteArray): SecretKeySpec {
        require(password.length in 8..256) { "La contraseña debe tener entre 8 y 256 caracteres" }
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        try {
            return SecretKeySpec(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,
                "AES"
            )
        } finally {
            spec.clearPassword()
        }
    }

    fun encrypt(output: OutputStream, password: String): OutputStream {
        val salt = ByteArray(SALT_BYTES)
        val iv = ByteArray(IV_BYTES)
        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(iv)
        output.write(MAGIC)
        output.write(salt)
        output.write(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(MAGIC)
        return CipherOutputStream(output, cipher)
    }

    fun decrypt(input: InputStream, password: String): InputStream {
        val header = DataInputStream(input)
        val magic = ByteArray(MAGIC.size)
        header.readFully(magic)
        require(magic.contentEquals(MAGIC)) { "El archivo no es un respaldo cifrado de Mi Mascota" }
        val salt = ByteArray(SALT_BYTES)
        val iv = ByteArray(IV_BYTES)
        header.readFully(salt)
        header.readFully(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(password, salt), GCMParameterSpec(128, iv))
        cipher.updateAAD(MAGIC)
        return CipherInputStream(header, cipher)
    }
}
