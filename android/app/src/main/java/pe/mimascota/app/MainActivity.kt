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
private sealed interface Destination {
    data object Pets : Destination
    data object Community : Destination
    data object Companion : Destination
    data object Backup : Destination
    data object Vets : Destination
    data class Health(val petId: String) : Destination
    data class Photos(val petId: String) : Destination
    data class Expenses(val petId: String) : Destination
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
            var destination by remember { mutableStateOf<Destination>(Destination.Pets) }
            val goBack = { destination = Destination.Pets }
            androidx.activity.compose.BackHandler(destination != Destination.Pets) { goBack() }
            MaterialTheme {
                when (val page = destination) {
                    Destination.Pets -> PetsScreen(vm,
                        onHealth = { destination = Destination.Health(it) },
                        onPhotos = { destination = Destination.Photos(it) },
                        onExpenses = { destination = Destination.Expenses(it) },
                        onCommunity = { destination = Destination.Community },
                        onCompanion = { destination = Destination.Companion },
                        onBackup = { destination = Destination.Backup },
                        onVets = { destination = Destination.Vets })
                    Destination.Community -> CommunityScreen(onBack = goBack)
                    Destination.Companion -> CompanionScreen(onBack = goBack)
                    Destination.Backup -> BackupScreen(database.petDao(), onBack = goBack)
                    Destination.Vets -> VetMapScreen(onBack = goBack)
                    is Destination.Health -> HealthScreen(page.petId, database.healthDao(), onBack = goBack)
                    is Destination.Photos -> PhotoScreen(page.petId, database.healthDao(), onBack = goBack)
                    is Destination.Expenses -> ExpenseScreen(page.petId, database.expenseDao(), onBack = goBack)
                }
            }
        }
    }
}
