package pe.mimascota.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.mimascota.app.data.PetRepository

class PetViewModel(private val repository: PetRepository) : ViewModel() {
    val pets = repository.pets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun add(name: String, species: String, birthDate: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val error = runCatching { repository.add(name, species, birthDate) }.exceptionOrNull()?.message
            onResult(error)
        }
    }
    fun update(id: String, name: String, species: String, birthDate: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val error = runCatching { repository.update(id, name, species, birthDate) }.exceptionOrNull()?.message
            onResult(error)
        }
    }

    fun updateProfile(id: String, breed: String?, weight: Float?, sterilized: Boolean?, microchip: String?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val error = runCatching { repository.updateProfile(id, breed, weight, sterilized, microchip) }.exceptionOrNull()?.message
            onResult(error)
        }
    }

    fun remove(id: String, onResult: (String?) -> Unit = {}) {
        viewModelScope.launch {
            val error = runCatching { repository.remove(id) }.exceptionOrNull()?.message
            onResult(error)
        }
    }
}

class PetViewModelFactory(private val repository: PetRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PetViewModel(repository) as T
}
