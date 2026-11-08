package pe.mimascota.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PetEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
}
