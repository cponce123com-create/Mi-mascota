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
fun PetsScreen(
    viewModel: PetViewModel,
    onHealth: (String) -> Unit,
    onPhotos: (String) -> Unit,
    onExpenses: (String) -> Unit,
    onCommunity: () -> Unit,
    onCompanion: () -> Unit,
    onBackup: () -> Unit,
    onVets: () -> Unit,
    onAccount: () -> Unit
) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("CAT") }
    var birthDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        Text("🐾 Mi Mascota", modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineMedium)
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Explorar", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onCommunity, modifier = Modifier.weight(1f)) { Text("Comunidad") }
                    OutlinedButton(onClick = onCompanion, modifier = Modifier.weight(1f)) { Text("Juegos") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBackup, modifier = Modifier.weight(1f)) { Text("Exportar") }
                    OutlinedButton(onClick = onVets, modifier = Modifier.weight(1f)) { Text("Veterinarias") }
                OutlinedButton(onClick = onAccount, modifier = Modifier.fillMaxWidth()) { Text("Mi cuenta") }
                }
            }
            item {
                Text("Registrar mascota", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = species == "CAT", onClick = { species = "CAT" }, label = { Text("Gato") })
                    FilterChip(selected = species == "DOG", onClick = { species = "DOG" }, label = { Text("Perro") })
                }
                OutlinedTextField(birthDate, { birthDate = it },
                    label = { Text("Nacimiento AAAA-MM-DD (opcional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank() && !saving,
                    onClick = {
                        saving = true
                        viewModel.add(name, species, birthDate) { result ->
                            error = result
                            saving = false
                            if (result == null) { name = ""; birthDate = "" }
                        }
                    }
                ) { Text(if (saving) "Guardando..." else "Guardar mascota") }
            }
            item { Text("Mis mascotas (${pets.size})", style = MaterialTheme.typography.titleMedium) }
            if (pets.isEmpty()) item {
                Text("Todavía no tienes mascotas registradas. Añade la primera arriba.")
            }
            items(pets, key = { it.id }) { pet ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pet.name, style = MaterialTheme.typography.titleMedium)
                        Text("${if (pet.species == "CAT") "Gato" else if (pet.species == "DOG") "Perro" else "Otra especie"}${pet.birthDate?.let { " · $it" } ?: ""}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onHealth(pet.id) }, modifier = Modifier.weight(1f)) { Text("Salud") }
                            OutlinedButton(onClick = { onPhotos(pet.id) }, modifier = Modifier.weight(1f)) { Text("Fotos") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onExpenses(pet.id) }, modifier = Modifier.weight(1f)) { Text("Gastos") }
                            TextButton(onClick = { pendingDelete = pet.id }, modifier = Modifier.weight(1f)) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("Eliminar mascota") },
        text = { Text("Se eliminarán también sus vacunas, recordatorios, fotos registradas y gastos de este dispositivo.") },
        confirmButton = { TextButton(onClick = {
            pendingDelete?.let { viewModel.remove(it) { failure -> error = failure } }
            pendingDelete = null
        }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
    )
}
