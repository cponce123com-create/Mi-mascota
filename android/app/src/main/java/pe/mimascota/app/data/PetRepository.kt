package pe.mimascota.app.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class PetRepository(private val dao: PetDao) {
    val pets: Flow<List<PetEntity>> = dao.observePets()

    suspend fun add(name: String, species: String, birthDate: String?) {
        val cleanName = name.trim()
        require(cleanName.isNotBlank() && cleanName.length <= 80) { "Nombre inválido" }
        require(species in setOf("CAT", "DOG")) { "Especie inválida" }
        val date = birthDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
        require(date == null || !date.isAfter(LocalDate.now())) { "Fecha futura" }
        dao.insert(PetEntity(UUID.randomUUID().toString(), cleanName, species, date?.toString(), System.currentTimeMillis()))
    }

    suspend fun remove(id: String) = dao.delete(id)
}
