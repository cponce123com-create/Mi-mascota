package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Ocean = Color(0xFF006574)
private val Teal = Color(0xFF009E85)
private val Mint = Color(0xFFE8F8F5)
private val Canvas = Color(0xFFF0F8FA)

@Composable
private fun DashboardTile(icon: String, label: String, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ElevatedCard(
        modifier = modifier.height(105.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(5.dp))
            Text(label, color = tint, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
    onAccount: () -> Unit,
    onAdoption: () -> Unit,
    onLost: () -> Unit
) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    var selectedId by remember { mutableStateOf<String?>(null) }
    val activePet = pets.firstOrNull { it.id == selectedId } ?: pets.firstOrNull()
    var showAdd by remember { mutableStateOf(false) }
    var showPets by remember { mutableStateOf(false) }
    var editingId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("CAT") }
    var birthDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    fun withPet(action: (String) -> Unit) {
        val id = activePet?.id
        if (id != null) action(id) else showAdd = true
    }

    Scaffold(
        containerColor = Canvas,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(selected = true, onClick = {}, icon = { Text("🏠") }, label = { Text("Inicio") })
                NavigationBarItem(selected = false, onClick = { showPets = true }, icon = { Text("🐾") }, label = { Text("Mascotas") })
                NavigationBarItem(selected = false, onClick = { showAdd = true }, icon = { Text("➕") }, label = { Text("Agregar") })
                NavigationBarItem(selected = false, onClick = onCommunity, icon = { Text("👥") }, label = { Text("Comunidad") })
                NavigationBarItem(selected = false, onClick = onAccount, icon = { Text("👤") }, label = { Text("Cuenta") })
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(
                    Modifier.fillMaxWidth().background(
                        Brush.verticalGradient(listOf(Ocean, Color(0xFF00949B), Canvas))
                    ).padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 25.dp)
                ) {
                    Text("🐾 Mi Mascota", style = MaterialTheme.typography.headlineMedium,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text("¡Hola! 👋", style = MaterialTheme.typography.headlineSmall,
                        color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Cuida, organiza y comparte la vida de tus mascotas.",
                        color = Color.White)
                    Spacer(Modifier.height(18.dp))
                    ElevatedCard(shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF5FCFF))) {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            pets.forEach { pet ->
                                val chosen = activePet?.id == pet.id
                                Column(Modifier.width(80.dp).clickable { selectedId = pet.id },
                                    horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(58.dp).clip(CircleShape)
                                        .background(if (chosen) Color(0xFFB5F4C6) else Color(0xFFDCECF7)),
                                        contentAlignment = Alignment.Center) {
                                        Text(if (pet.species == "DOG") "🐶" else if (pet.species == "CAT") "🐱" else "🐾",
                                            style = MaterialTheme.typography.headlineLarge)
                                    }
                                    Text(pet.name, maxLines = 1, fontWeight = if (chosen) FontWeight.Bold else FontWeight.Normal)
                                    if (chosen) Text("● Activa", color = Teal, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Column(Modifier.width(80.dp).clickable { showAdd = true },
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(58.dp).clip(CircleShape).background(Color(0xFFD8E9FA)),
                                    contentAlignment = Alignment.Center) {
                                    Text("+", style = MaterialTheme.typography.headlineLarge, color = Ocean)
                                }
                                Text("Agregar", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (activePet == null) "Empieza registrando tu mascota" else "Cuidando a ${activePet.name} 💚",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ocean)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardTile("💉", "Vacunas", Color(0xFF2465C5), Modifier.weight(1f)) { withPet(onHealth) }
                        DashboardTile("💗", "Salud", Color(0xFFD72672), Modifier.weight(1f)) { withPet(onHealth) }
                        DashboardTile("📅", "Recordatorios", Color(0xFF7952C7), Modifier.weight(1f)) { withPet(onHealth) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardTile("🪙", "Gastos", Color(0xFFB97800), Modifier.weight(1f)) { withPet(onExpenses) }
                        DashboardTile("🖼️", "Galería", Color(0xFF0878C5), Modifier.weight(1f)) { withPet(onPhotos) }
                        DashboardTile("👥", "Comunidad", Color(0xFF6940AE), Modifier.weight(1f), onCommunity)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardTile("🏡", "Adopción", Color(0xFF168E61), Modifier.weight(1f), onAdoption)
                        DashboardTile("📢", "Perdidos", Color(0xFFC94936), Modifier.weight(1f), onLost)
                        DashboardTile("☁️", "Respaldos", Color(0xFF1177C2), Modifier.weight(1f), onBackup)
                    }
                    ElevatedCard(shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFDCF4E5))) {
                        Row(Modifier.fillMaxWidth().clickable(onClick = onCompanion).padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("🐕", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Compañero virtual", fontWeight = FontWeight.Bold, color = Color(0xFF165B39))
                                Text("Juega y cuida a tu mascota", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("›", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                    OutlinedButton(onClick = onVets, modifier = Modifier.fillMaxWidth()) { Text("📍 Buscar veterinarias cercanas") }
                    if (activePet != null) {
                        OutlinedButton(onClick = {
                            editingId = activePet.id
                            name = activePet.name
                            species = activePet.species
                            birthDate = activePet.birthDate.orEmpty()
                            error = null
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("✏️ Editar perfil de ${activePet.name}")
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (showPets) AlertDialog(
        onDismissRequest = { showPets = false },
        title = { Text("🐾 Mis mascotas") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (pets.isEmpty()) Text("Aún no has registrado mascotas.")
                pets.forEach { pet ->
                    OutlinedButton(
                        onClick = { selectedId = pet.id; showPets = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${if (pet.species == "DOG") "🐶" else "🐱"} ${pet.name}")
                    }
                }
                Button(onClick = { showPets = false; showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("➕ Registrar nueva mascota")
                }
            }
        },
        confirmButton = { TextButton(onClick = { showPets = false }) { Text("Cerrar") } }
    )
    if (editingId != null) AlertDialog(
        onDismissRequest = { if (!saving) editingId = null },
        title = { Text("✏️ Editar mascota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = species == "CAT", onClick = { species = "CAT" }, label = { Text("Gato") })
                    FilterChip(selected = species == "DOG", onClick = { species = "DOG" }, label = { Text("Perro") })
                    FilterChip(selected = species == "OTHER", onClick = { species = "OTHER" }, label = { Text("Otro") })
                }
                OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Nacimiento AAAA-MM-DD") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(enabled = !saving && name.isNotBlank(), onClick = {
            val id = editingId ?: return@Button
            saving = true
            viewModel.update(id, name, species, birthDate) { failure ->
                saving = false
                error = failure
                if (failure == null) editingId = null
            }
        }) { Text("Guardar cambios") } },
        dismissButton = {
            TextButton(enabled = !saving, onClick = {
                editingId = null
                pendingDelete = activePet?.id
            }) { Text("Eliminar") }
            TextButton(enabled = !saving, onClick = { editingId = null }) { Text("Cancelar") }
        }
    )
    if (showAdd) AlertDialog(
        onDismissRequest = { if (!saving) showAdd = false },
        title = { Text("🐾 Nueva mascota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = species == "CAT", onClick = { species = "CAT" }, label = { Text("🐱 Gato") })
                    FilterChip(selected = species == "DOG", onClick = { species = "DOG" }, label = { Text("🐶 Perro") })
                    FilterChip(selected = species == "OTHER", onClick = { species = "OTHER" }, label = { Text("Otro") })
                }
                OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Nacimiento AAAA-MM-DD (opcional)") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank() && !saving, onClick = {
                saving = true
                viewModel.add(name, species, birthDate) { result ->
                    error = result
                    saving = false
                    if (result == null) {
                        name = ""; birthDate = ""; showAdd = false
                    }
                }
            }) { Text(if (saving) "Guardando..." else "Guardar") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = { showAdd = false }) { Text("Cancelar") } }
    )
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("Eliminar mascota") },
        text = { Text("Se eliminarán los datos locales asociados a esta mascota. Esta acción no se puede deshacer.") },
        confirmButton = { TextButton(onClick = {
            pendingDelete?.let { id -> viewModel.remove(id) { failure -> error = failure } }
            selectedId = null
            pendingDelete = null
        }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
    )
}
