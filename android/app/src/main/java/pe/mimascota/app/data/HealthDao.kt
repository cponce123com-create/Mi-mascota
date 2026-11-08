package pe.mimascota.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY dueAt")
    fun vaccines(petId: String): Flow<List<VaccineEntity>>
    @Insert suspend fun addVaccine(vaccine: VaccineEntity)
    @Query("SELECT * FROM reminders WHERE petId = :petId ORDER BY dueAt")
    fun reminders(petId: String): Flow<List<ReminderEntity>>
    @Insert suspend fun addReminder(reminder: ReminderEntity)
    @Query("UPDATE reminders SET completed = :completed WHERE id = :id")
    suspend fun completeReminder(id: String, completed: Boolean)
    @Query("SELECT * FROM photos WHERE petId = :petId ORDER BY createdAt DESC")
    fun photos(petId: String): Flow<List<PhotoEntity>>
    @Insert suspend fun addPhoto(photo: PhotoEntity)
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhoto(id: String)
}
