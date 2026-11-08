package pe.mimascota.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import pe.mimascota.app.ui.HealthScreen
import pe.mimascota.app.ui.PhotoScreen
import pe.mimascota.app.ui.CommunityScreen
import pe.mimascota.app.ui.ExpenseScreen
import pe.mimascota.app.ui.CompanionScreen
import pe.mimascota.app.ui.BackupScreen
import pe.mimascota.app.ui.VetMapScreen
import pe.mimascota.app.ui.AccountScreen
import pe.mimascota.app.ui.DiscoverScreen
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.mimascota.app.data.AppDatabase
import pe.mimascota.app.data.PetRepository
import pe.mimascota.app.ui.PetViewModel
import pe.mimascota.app.ui.PetViewModelFactory
import pe.mimascota.app.ui.PetsScreen
import pe.mimascota.app.ui.PetProfileScreen
import pe.mimascota.app.ui.ModerationScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pets ADD COLUMN breed TEXT")
        db.execSQL("ALTER TABLE pets ADD COLUMN weightKg REAL")
        db.execSQL("ALTER TABLE pets ADD COLUMN sterilized INTEGER")
        db.execSQL("ALTER TABLE pets ADD COLUMN microchip TEXT")
    }
}
private sealed interface Destination {
    data object Pets : Destination
    data object Community : Destination
    data object Companion : Destination
    data object Backup : Destination
    data object Vets : Destination
    data object Account : Destination
    data object Moderation : Destination
    data object Adoption : Destination
    data object Lost : Destination
    data class Profile(val petId: String) : Destination
    data class Health(val petId: String, val tab: Int = 0) : Destination
    data class Photos(val petId: String) : Destination
    data class Expenses(val petId: String) : Destination
}

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mi-mascota.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ViewModelProvider(this, PetViewModelFactory(PetRepository(database.petDao())))[PetViewModel::class.java]
        setContent {
            var destination by remember { mutableStateOf<Destination>(Destination.Pets) }
            var selectedPetId by rememberSaveable { mutableStateOf<String?>(null) }
            var petsRequest by remember { mutableIntStateOf(0) }
            var addRequest by remember { mutableIntStateOf(0) }
            LaunchedEffect(destination) {
                if (destination != Destination.Pets) { petsRequest = 0; addRequest = 0 }
            }
            val allPets by vm.pets.collectAsStateWithLifecycle()
            val allPhotos by database.healthDao().allPhotos().collectAsStateWithLifecycle(emptyList())
            val goBack = { destination = Destination.Pets }
            androidx.activity.compose.BackHandler(destination != Destination.Pets) { goBack() }

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF008E77),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFD8F5E8),
                    secondary = Color(0xFF006A79),
                    background = Color(0xFFF0F8FA),
                    surface = Color.White,
                    error = Color(0xFFB3261E)
                )
            ) {
                Scaffold(
                    containerColor = Color(0xFFF0F8FA),
                    bottomBar = {
                        NavigationBar(containerColor = Color.White) {
                            NavigationBarItem(
                                selected = destination == Destination.Pets,
                                onClick = { destination = Destination.Pets },
                                icon = { Text("🏠") }, label = { Text("Inicio") }
                            )
                            NavigationBarItem(
                                selected = destination is Destination.Profile,
                                onClick = { petsRequest++; destination = Destination.Pets },
                                icon = { Text("🐾") }, label = { Text("Mascotas") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { addRequest++; destination = Destination.Pets },
                                icon = { Text("➕") }, label = { Text("Agregar") }
                            )
                            NavigationBarItem(
                                selected = destination == Destination.Community,
                                onClick = { destination = Destination.Community },
                                icon = { Text("👥") }, label = { Text("Comunidad") }
                            )
                            NavigationBarItem(
                                selected = destination == Destination.Account || destination == Destination.Moderation,
                                onClick = { destination = Destination.Account },
                                icon = { Text("👤") }, label = { Text("Cuenta") }
                            )
                        }
                    }
                ) { screenPadding ->
                    Box(Modifier.fillMaxSize().padding(screenPadding)) {
                        when (val page = destination) {
                            Destination.Pets -> PetsScreen(vm,
                                photos = allPhotos, selectedId = selectedPetId,
                                onSelectPet = { selectedPetId = it },
                                openPetsToken = petsRequest, openAddToken = addRequest,
                                onHealth = { destination = Destination.Health(it) },
                                onVaccines = { destination = Destination.Health(it, 0) },
                                onReminders = { destination = Destination.Health(it, 1) },
                                onPhotos = { destination = Destination.Photos(it) },
                                onExpenses = { destination = Destination.Expenses(it) },
                                onCommunity = { destination = Destination.Community },
                                onCompanion = { destination = Destination.Companion },
                                onBackup = { destination = Destination.Backup },
                                onVets = { destination = Destination.Vets },
                                onAccount = { destination = Destination.Account },
                                onAdoption = { destination = Destination.Adoption },
                                onLost = { destination = Destination.Lost },
                                onProfile = { destination = Destination.Profile(it) })
                            Destination.Community -> CommunityScreen(onBack = goBack,
                                onAccount = { destination = Destination.Account })
                            Destination.Companion -> CompanionScreen(onBack = goBack)
                            Destination.Backup -> BackupScreen(database.backupDao(), onBack = goBack)
                            Destination.Account -> AccountScreen(onBack = goBack,
                                onModeration = { destination = Destination.Moderation })
                            Destination.Moderation -> ModerationScreen(
                                onBack = { destination = Destination.Account })
                            Destination.Adoption -> DiscoverScreen("adoption", pets = allPets, onBack = goBack,
                                onCommunity = { destination = Destination.Community },
                                onVets = { destination = Destination.Vets },
                                onAccount = { destination = Destination.Account })
                            Destination.Lost -> DiscoverScreen("lost", pets = allPets, onBack = goBack,
                                onCommunity = { destination = Destination.Community },
                                onVets = { destination = Destination.Vets },
                                onAccount = { destination = Destination.Account })
                            Destination.Vets -> VetMapScreen(onBack = goBack)
                            is Destination.Profile -> PetProfileScreen(
                                allPets.firstOrNull { it.id == page.petId },
                                database.healthDao(), database.expenseDao(), vm, onBack = goBack,
                                onHealth = { destination = Destination.Health(page.petId) },
                                onPhotos = { destination = Destination.Photos(page.petId) },
                                onExpenses = { destination = Destination.Expenses(page.petId) })
                            is Destination.Health -> HealthScreen(page.petId, database.healthDao(), onBack = goBack,
                                petName = allPets.firstOrNull { it.id == page.petId }?.name.orEmpty(), initialTab = page.tab)
                            is Destination.Photos -> PhotoScreen(page.petId, database.healthDao(), onBack = goBack)
                            is Destination.Expenses -> ExpenseScreen(page.petId, database.expenseDao(), onBack = goBack)
                        }
                    }
                }
            }
        }
    }
}
