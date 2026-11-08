package pe.mimascota.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

data class BackupSnapshot(
    val pets: List<PetEntity>,
    val vaccines: List<VaccineEntity>,
    val reminders: List<ReminderEntity>,
    val expenses: List<ExpenseEntity>,
    val photos: List<PhotoEntity>
)

@Dao
abstract class BackupDao {
    @Query("SELECT * FROM pets ORDER BY createdAt")
    abstract suspend fun pets(): List<PetEntity>
    @Query("SELECT * FROM vaccines")
    abstract suspend fun vaccines(): List<VaccineEntity>
    @Query("SELECT * FROM reminders")
    abstract suspend fun reminders(): List<ReminderEntity>
    @Query("SELECT * FROM expenses")
    abstract suspend fun expenses(): List<ExpenseEntity>
    @Query("SELECT * FROM photos")
    abstract suspend fun photos(): List<PhotoEntity>

    @Transaction
    open suspend fun snapshot(): BackupSnapshot =
        BackupSnapshot(pets(), vaccines(), reminders(), expenses(), photos())
}
