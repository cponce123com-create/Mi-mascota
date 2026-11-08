package pe.mimascota.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PetEntity::class, VaccineEntity::class, ReminderEntity::class, PhotoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun healthDao(): HealthDao
}
