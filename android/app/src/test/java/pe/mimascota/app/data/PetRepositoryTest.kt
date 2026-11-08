package pe.mimascota.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class PetRepositoryTest {
    private class FakePetDao : PetDao {
        val rows = MutableStateFlow<List<PetEntity>>(emptyList())
        override fun observePets(): Flow<List<PetEntity>> = rows
        override suspend fun insert(pet: PetEntity) {
            rows.value = rows.value + pet
        }
        override suspend fun delete(id: String) {
            rows.value = rows.value.filterNot { it.id == id }
        }
        override suspend fun updateDetails(id: String, name: String, species: String, birthDate: String?) {
            rows.value = rows.value.map {
                if (it.id == id) it.copy(name = name, species = species, birthDate = birthDate) else it
            }
        }
        override suspend fun updateProfile(id: String, breed: String?, weightKg: Float?, sterilized: Boolean?, microchip: String?) {
            rows.value = rows.value.map {
                if (it.id == id) it.copy(breed = breed, weightKg = weightKg, sterilized = sterilized, microchip = microchip) else it
            }
        }
    }

    @Test fun allowsOtherSpeciesAndTrimsNames() = runBlocking {
        val dao = FakePetDao()
        val repository = PetRepository(dao)
        repository.add("  Copito  ", "OTHER", null)
        assertEquals("Copito", dao.rows.value.single().name)
        assertEquals("OTHER", dao.rows.value.single().species)
    }

    @Test fun rejectsFutureBirthDate() {
        val repository = PetRepository(FakePetDao())
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.add("Luna", "CAT", LocalDate.now().plusDays(1).toString()) }
        }
    }

    @Test fun validatesProfileWeight() {
        val repository = PetRepository(FakePetDao())
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { repository.updateProfile("id", null, Float.NaN, null, null) }
        }
    }

    @Test fun updatesProfileWithoutChangingIdentity() = runBlocking {
        val dao = FakePetDao()
        val repository = PetRepository(dao)
        repository.add("Tomy", "DOG", null)
        val id = dao.rows.value.single().id
        repository.updateProfile(id, "Labrador", 25f, true, "CHIP")
        val pet = dao.rows.value.single()
        assertEquals(id, pet.id)
        assertEquals("Labrador", pet.breed)
        assertEquals(25f, pet.weightKg ?: 0f, 0.01f)
    }
}
