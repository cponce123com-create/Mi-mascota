package pe.mimascota.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import pe.mimascota.app.ui.HealthScreen
import pe.mimascota.app.ui.PhotoScreen
import pe.mimascota.app.ui.CommunityScreen
import pe.mimascota.app.ui.ExpenseScreen
import pe.mimascota.app.ui.CompanionScreen
import pe.mimascota.app.ui.BackupScreen
import pe.mimascota.app.ui.VetMapScreen
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.mimascota.app.data.AppDatabase
import pe.mimascota.app.data.PetRepository
import pe.mimascota.app.ui.PetViewModel
import pe.mimascota.app.ui.PetViewModelFactory
import pe.mimascota.app.ui.PetsScreen

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS expenses (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, title TEXT NOT NULL, amountCents INTEGER NOT NULL, date TEXT NOT NULL, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_petId ON expenses(petId)")
    }
}
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS vaccines (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, name TEXT NOT NULL, appliedAt TEXT, dueAt TEXT, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_vaccines_petId ON vaccines(petId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS reminders (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, title TEXT NOT NULL, dueAt TEXT NOT NULL, completed INTEGER NOT NULL, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_petId ON reminders(petId)")
        db.execSQL("CREATE TABLE IF NOT EXISTS photos (id TEXT NOT NULL PRIMARY KEY, petId TEXT NOT NULL, uri TEXT NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(petId) REFERENCES pets(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_photos_petId ON photos(petId)")
    }
}
class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mi-mascota.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ViewModelProvider(this, PetViewModelFactory(PetRepository(database.petDao())))[PetViewModel::class.java]
        setContent {
            var selectedPet by remember { mutableStateOf<String?>(null) }
            var section by remember { mutableStateOf("pets") }
            MaterialTheme {
                when (section) {
                    "backup" -> BackupScreen(database.petDao(), onBack = { section = "pets" })
                    "vets" -> VetMapScreen(onBack = { section = "pets" })
                    "companion" -> CompanionScreen(onBack = { section = "pets" })
                    "community" -> CommunityScreen(onBack = { section = "pets" })
                    "photos" -> PhotoScreen(selectedPet!!, database.healthDao(), onBack = { section = "pets"; selectedPet = null })
                    "expenses" -> ExpenseScreen(selectedPet!!, database.expenseDao(), onBack = { section = "pets"; selectedPet = null })
                    "health" -> HealthScreen(selectedPet!!, database.healthDao(), onBack = { section = "pets"; selectedPet = null })
                    else -> PetsScreen(vm, onHealth = { selectedPet = it; section = "health" },
                        onPhotos = { selectedPet = it; section = "photos" },
                        onExpenses = { selectedPet = it; section = "expenses" },
                        onCommunity = { section = "community" }, onCompanion = { section = "companion" },
                        onBackup = { section = "backup" }, onVets = { section = "vets" })
                }
            }
        }
    }
}
