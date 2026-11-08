package pe.mimascota.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY createdAt DESC")
    fun observePets(): Flow<List<PetEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(pet: PetEntity)

    @Query("UPDATE pets SET name = :name, species = :species, birthDate = :birthDate WHERE id = :id")
    suspend fun updateDetails(id: String, name: String, species: String, birthDate: String?)

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun delete(id: String)
}
