package pe.mimascota.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

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

    @Upsert abstract suspend fun putPets(items: List<PetEntity>)
    @Upsert abstract suspend fun putVaccines(items: List<VaccineEntity>)
    @Upsert abstract suspend fun putReminders(items: List<ReminderEntity>)
    @Upsert abstract suspend fun putExpenses(items: List<ExpenseEntity>)
    @Upsert abstract suspend fun putPhotos(items: List<PhotoEntity>)

    /** One atomic import. Existing matching IDs are updated; unrelated local data remains. */
    @Transaction
    open suspend fun restore(snapshot: BackupSnapshot) {
        putPets(snapshot.pets)
        putVaccines(snapshot.vaccines)
        putReminders(snapshot.reminders)
        putExpenses(snapshot.expenses)
        putPhotos(snapshot.photos)
    }

    @Transaction
    open suspend fun snapshot(): BackupSnapshot =
        BackupSnapshot(pets(), vaccines(), reminders(), expenses(), photos())
}
