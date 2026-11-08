package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PetsScreen(viewModel: PetViewModel, onHealth: (String) -> Unit, onPhotos: (String) -> Unit, onCommunity: () -> Unit) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("CAT") }
    var birthDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Scaffold(topBar = { Text("🐾 Mi Mascota", modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.headlineMedium) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Registra a tus compañeros", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onCommunity) { Text("Comunidad") }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row {
                FilterChip(selected = species == "CAT", onClick = { species = "CAT" }, label = { Text("Gato") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = species == "DOG", onClick = { species = "DOG" }, label = { Text("Perro") })
            }
            OutlinedTextField(value = birthDate, onValueChange = { birthDate = it }, label = { Text("Nacimiento (AAAA-MM-DD), opcional") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                viewModel.add(name, species, birthDate) { result ->
                    error = result
                    if (result == null) { name = ""; birthDate = "" }
                }
            }, enabled = name.isNotBlank()) { Text("Guardar mascota") }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("Mis mascotas (${pets.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(pets, key = { it.id }) { pet ->
                    ListItem(
                        headlineContent = { Text(pet.name) },
                        supportingContent = { Text((if (pet.species == "CAT") "Gato" else "Perro") + (pet.birthDate?.let { " · Nació: $it" } ?: "")) },
                        trailingContent = { Row { TextButton(onClick = { onHealth(pet.id) }) { Text("Salud") }; TextButton(onClick = { onPhotos(pet.id) }) { Text("Fotos") }; TextButton(onClick = { viewModel.remove(pet.id) }) { Text("Eliminar") } } }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
