package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import android.widget.ImageView
import pe.mimascota.app.data.PhotoEntity
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Ocean = Color(0xFF006574)
private val Teal = Color(0xFF009E85)
private val Mint = Color(0xFFE8F8F5)
private val Canvas = Color(0xFFF0F8FA)

@Composable
private fun DashboardTile(icon: String, label: String, tint: Color,
    modifier: Modifier = Modifier, onClick: () -> Unit) {
    PetActionTile(icon, label, tint, modifier, onClick)
}

@Composable
fun PetsScreen(
    viewModel: PetViewModel,
    photos: List<PhotoEntity>,
    selectedId: String?,
    onSelectPet: (String?) -> Unit,
    openPetsToken: Int,
    openAddToken: Int,
    onHealth: (String) -> Unit,
    onVaccines: (String) -> Unit,
    onReminders: (String) -> Unit,
    onPhotos: (String) -> Unit,
    onExpenses: (String) -> Unit,
    onCommunity: () -> Unit,
    onCompanion: () -> Unit,
    onBackup: () -> Unit,
    onVets: () -> Unit,
    onAccount: () -> Unit,
    onAdoption: () -> Unit,
    onLost: () -> Unit,
    onProfile: (String) -> Unit,
    onTips: () -> Unit
) {
    val pets by viewModel.pets.collectAsStateWithLifecycle()
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
    LaunchedEffect(openPetsToken) { if (openPetsToken > 0) showPets = true }
    LaunchedEffect(openAddToken) { if (openAddToken > 0) showAdd = true }
    fun withPet(action: (String) -> Unit) {
        val id = activePet?.id
        if (id != null) action(id) else showAdd = true
    }

    Scaffold(containerColor = Canvas) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth().background(
                    Brush.verticalGradient(listOf(Color(0xFF004A58),
                        Color(0xFF007E89), Color(0xFFAEDDE6)))
                ).padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("🐾 Mi Mascota", modifier = Modifier.weight(1f),
                            fontSize = 24.sp, lineHeight = 29.sp,
                            color = Color.White, fontWeight = FontWeight.ExtraBold)
                        IconButton(onClick = { withPet(onReminders) }) {
                            Text("🔔", fontSize = 24.sp)
                        }
                    }
                    Spacer(Modifier.height(11.dp))
                    Text("¡Hola! 👋", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Cuida, organiza y comparte la vida de tus mascotas.",
                        fontSize = 14.sp, lineHeight = 19.sp, color = Color.White,
                        modifier = Modifier.fillMaxWidth(0.88f))
                    Spacer(Modifier.height(16.dp))
                    ElevatedCard(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFF3FBFF)),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            pets.forEach { pet ->
                                val selected = activePet?.id == pet.id
                                val uri = photos.firstOrNull { it.petId == pet.id }?.uri
                                Column(Modifier.width(78.dp).clickable {
                                    onSelectPet(pet.id)
                                }, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(Modifier.size(64.dp).clip(CircleShape)
                                        .border(if (selected) 3.dp else 1.dp,
                                            if (selected) Color(0xFF0EAF61) else Color(0xFFD2E6EB),
                                            CircleShape).padding(4.dp)) {
                                        PetAvatar(uri, pet.species, Modifier.fillMaxSize())
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(pet.name, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp, lineHeight = 15.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                            Column(Modifier.width(78.dp).clickable { showAdd = true },
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(64.dp).clip(CircleShape)
                                    .background(Color(0xFFD6EAFB)),
                                    contentAlignment = Alignment.Center) {
                                    Text("+", fontSize = 34.sp, color = PetPalette.Ocean)
                                }
                                Spacer(Modifier.height(3.dp))
                                Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (activePet == null) "Empieza registrando tu mascota" else "Servicios para ${activePet.name} 💚",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ocean)
                    if (activePet != null) {
                        TextButton(onClick = { onProfile(activePet.id) }) { Text("🐾 Ver ficha completa de ${activePet.name} ›") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DashboardTile("💉", "Vacunas", Color(0xFF2465C5), Modifier.weight(1f)) { withPet(onVaccines) }
                        DashboardTile("💗", "Salud", Color(0xFFD72672), Modifier.weight(1f)) { withPet(onHealth) }
                        DashboardTile("📅", "Recordatorios", Color(0xFF7952C7), Modifier.weight(1f)) { withPet(onReminders) }
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
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(
                            Color(0xFF075B35), Color(0xFF1F9B4C), Color(0xFF50BA65))))
                        .clickable(onClick = onTips)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PetAvatar(photos.firstOrNull { it.petId == activePet?.id }?.uri,
                                activePet?.species ?: "DOG", Modifier.size(64.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Consejos de cuidado", fontSize = 16.sp,
                                    lineHeight = 20.sp, color = Color.White,
                                    fontWeight = FontWeight.Bold)
                                Text("Guías y recomendaciones para su bienestar.",
                                    fontSize = 11.sp, lineHeight = 15.sp, color = Color.White)
                            }
                            Text("›", fontSize = 30.sp, color = Color.White)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCompanion, modifier = Modifier.weight(1f)) {
                            Text("🎮 Juegos", maxLines = 1, fontSize = 12.sp)
                        }
                        OutlinedButton(onClick = onVets, modifier = Modifier.weight(1f)) {
                            Text("📍 Veterinarias", maxLines = 1, fontSize = 12.sp)
                        }
                    }
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
                        onClick = { onSelectPet(pet.id); showPets = false },
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
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            onSelectPet(null)
            pendingDelete = null
        }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } }
    )
}
