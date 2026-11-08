package pe.mimascota.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import pe.mimascota.app.ui.HealthScreen
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.mimascota.app.data.AppDatabase
import pe.mimascota.app.data.PetRepository
import pe.mimascota.app.ui.PetViewModel
import pe.mimascota.app.ui.PetViewModelFactory
import pe.mimascota.app.ui.PetsScreen

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS vaccines (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, name TEXT NOT NULL, appliedAt TEXT, dueAt TEXT, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_vaccines_petId ON vaccines(petId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reminders (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, title TEXT NOT NULL, dueAt TEXT NOT NULL, completed INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_petId ON reminders(petId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS photos (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, uri TEXT NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_photos_petId ON photos(petId)")
    }
}
class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mi-mascota.db")
            .addMigrations(MIGRATION_1_2).build()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ViewModelProvider(this, PetViewModelFactory(PetRepository(database.petDao())))[PetViewModel::class.java]
        setContent {
            var selectedPet by remember { mutableStateOf<String?>(null) }
            MaterialTheme {
                if (selectedPet == null) PetsScreen(vm, onHealth = { selectedPet = it })
                else HealthScreen(selectedPet!!, database.healthDao(), onBack = { selectedPet = null })
            }
        }
    }
}
