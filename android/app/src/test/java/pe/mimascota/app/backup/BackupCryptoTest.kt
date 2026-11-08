package pe.mimascota.app.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BackupCryptoTest {
    private val password = "contraseña-mascota-2026"

    @Test
    fun encryptedBackupRoundTripsAndDoesNotContainPlaintext() {
        val plaintext = "Mi Mascota - datos y fotos privados".toByteArray(Charsets.UTF_8)
        val output = ByteArrayOutputStream()
        BackupCrypto.encrypt(output, password).use { it.write(plaintext) }
        val ciphertext = output.toByteArray()
        assertFalse(ciphertext.toString(Charsets.UTF_8).contains("datos y fotos privados"))
        val decoded = BackupCrypto.decrypt(ByteArrayInputStream(ciphertext), password)
            .use { it.readBytes() }
        assertArrayEquals(plaintext, decoded)
    }

    @Test
    fun wrongPasswordDoesNotDecrypt() {
        val output = ByteArrayOutputStream()
        BackupCrypto.encrypt(output, password).use { it.write("privado".toByteArray()) }
        assertThrows(Exception::class.java) {
            BackupCrypto.decrypt(ByteArrayInputStream(output.toByteArray()), "otra-contraseña")
                .use { it.readBytes() }
        }
    }

    @Test
    fun tamperingIsDetected() {
        val output = ByteArrayOutputStream()
        BackupCrypto.encrypt(output, password).use { it.write("veterinaria".toByteArray()) }
        val damaged = output.toByteArray()
        damaged[damaged.lastIndex] = (damaged.last().toInt() xor 0x01).toByte()
        assertThrows(Exception::class.java) {
            BackupCrypto.decrypt(ByteArrayInputStream(damaged), password).use { it.readBytes() }
        }
    }

    @Test
    fun shortPasswordRejected() {
        val output = ByteArrayOutputStream()
        assertThrows(IllegalArgumentException::class.java) {
            BackupCrypto.encrypt(output, "1234")
        }
    }
}
