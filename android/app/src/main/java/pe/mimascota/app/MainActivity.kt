package pe.mimascota.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import pe.mimascota.app.data.AppDatabase
import pe.mimascota.app.data.PetRepository
import pe.mimascota.app.ui.PetViewModel
import pe.mimascota.app.ui.PetViewModelFactory
import pe.mimascota.app.ui.PetsScreen

class MainActivity : ComponentActivity() {
    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "mi-mascota.db").build()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = ViewModelProvider(this, PetViewModelFactory(PetRepository(database.petDao())))[PetViewModel::class.java]
        setContent { MaterialTheme { PetsScreen(vm) } }
    }
}
