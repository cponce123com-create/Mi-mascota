package pe.mimascota.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val species: String,
    val birthDate: String?,
    val createdAt: Long,
    val breed: String? = null,
    val weightKg: Float? = null,
    val sterilized: Boolean? = null,
    val microchip: String? = null
)
