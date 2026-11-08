package pe.mimascota.app.ui

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.mimascota.app.data.ExpenseDao
import pe.mimascota.app.data.HealthDao
import pe.mimascota.app.data.PetEntity
import java.time.LocalDate
import java.time.Period

private val Ink = Color(0xFF006574)

@Composable
fun PetProfileScreen(
    pet: PetEntity?,
    healthDao: HealthDao,
    expenses: ExpenseDao,
    viewModel: PetViewModel,
    onBack: () -> Unit,
    onHealth: () -> Unit,
    onPhotos: () -> Unit,
    onExpenses: () -> Unit
) {
    if (pet == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Esta mascota ya no está registrada.")
        }
        return
    }
    val photos by remember(pet.id) { healthDao.photos(pet.id) }.collectAsStateWithLifecycle(emptyList())
    val vaccines by remember(pet.id) { healthDao.vaccines(pet.id) }.collectAsStateWithLifecycle(emptyList())
    val reminders by remember(pet.id) { healthDao.reminders(pet.id) }.collectAsStateWithLifecycle(emptyList())
    val expenseRows by remember(pet.id) { expenses.observe(pet.id) }.collectAsStateWithLifecycle(emptyList())
    var editing by remember { mutableStateOf(false) }
    var breed by remember(pet.id, editing) { mutableStateOf(pet.breed.orEmpty()) }
    var weight by remember(pet.id, editing) { mutableStateOf(pet.weightKg?.toString().orEmpty()) }
    var chip by remember(pet.id, editing) { mutableStateOf(pet.microchip.orEmpty()) }
    var sterilized by remember(pet.id, editing) { mutableIntStateOf(
        when (pet.sterilized) { true -> 1; false -> 2; null -> 0 }
    ) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val age = pet.birthDate?.let { runCatching { Period.between(LocalDate.parse(it), LocalDate.now()).years }.getOrNull() }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA)).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(285.dp)) {
            val imageUri = photos.firstOrNull()?.uri
            if (imageUri != null) {
                AndroidView(factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
                    update = { image -> runCatching { image.setImageURI(Uri.parse(imageUri)) }
                        .onFailure { image.setImageDrawable(null) } },
                    modifier = Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
                    Text(if (pet.species == "DOG") "🐶" else if (pet.species == "CAT") "🐱" else "🐾",
                        style = MaterialTheme.typography.displayLarge)
                }
            }
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x80000000), Color.Transparent, Color(0xB8000000)))))
            Column(Modifier.align(Alignment.TopStart).padding(14.dp)) {
                TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                Text(pet.name, color = Color.White, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge)
                Text("${when (pet.species) { "DOG" -> "Perro"; "CAT" -> "Gato"; else -> "Otra especie" }} · ${pet.breed ?: "Raza sin indicar"}${age?.let { " · $it años" } ?: ""}",
                    color = Color.White)
            }
        }
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedCard(shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileAction("💗", "Salud", Modifier.weight(1f), onHealth)
                    ProfileAction("💉", "Vacunas", Modifier.weight(1f), onHealth)
                    ProfileAction("🖼️", "Fotos", Modifier.weight(1f), onPhotos)
                    ProfileAction("🪙", "Gastos", Modifier.weight(1f), onExpenses)
                }
            }
            ElevatedCard(shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Información general", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    ProfileRow("⚖️ Peso", pet.weightKg?.let { "$it kg" } ?: "Sin indicar")
                    HorizontalDivider()
                    ProfileRow("📅 Nacimiento", pet.birthDate ?: "Sin indicar")
                    HorizontalDivider()
                    ProfileRow("✂️ Esterilizado", when (pet.sterilized) { true -> "Sí"; false -> "No"; null -> "Sin indicar" })
                    HorizontalDivider()
                    ProfileRow("🔖 Microchip", pet.microchip?.takeIf { it.isNotBlank() } ?: "Sin indicar")
                    HorizontalDivider()
                    ProfileRow("💉 Vacunas", "${vaccines.size} registradas")
                    HorizontalDivider()
                    ProfileRow("🔔 Recordatorios", "${reminders.count { !it.completed }} pendientes")
                    HorizontalDivider()
                    ProfileRow("💰 Gastos", "${expenseRows.size} movimientos")
                }
            }
            Button(onClick = { editing = true; error = null }, modifier = Modifier.fillMaxWidth()) {
                Text("✏️ Editar datos del perfil")
            }
            OutlinedButton(onClick = onPhotos, modifier = Modifier.fillMaxWidth()) {
                Text("📷 Cambiar fotografía")
            }
        }
    }
    if (editing) {
        AlertDialog(onDismissRequest = { if (!busy) editing = false },
            title = { Text("Editar información") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(breed, { breed = it }, label = { Text("Raza (opcional)") })
                    OutlinedTextField(weight, { weight = it }, label = { Text("Peso en kg (opcional)") })
                    Text("¿Está esterilizado?")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = sterilized == 0, onClick = { sterilized = 0 }, label = { Text("N/D") })
                        FilterChip(selected = sterilized == 1, onClick = { sterilized = 1 }, label = { Text("Sí") })
                        FilterChip(selected = sterilized == 2, onClick = { sterilized = 2 }, label = { Text("No") })
                    }
                    OutlinedTextField(chip, { chip = it }, label = { Text("Microchip (opcional)") })
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { Button(enabled = !busy, onClick = {
                val kg = weight.replace(',', '.').takeIf { it.isNotBlank() }?.toFloatOrNull()
                if (weight.isNotBlank() && kg == null) { error = "Peso inválido"; return@Button }
                busy = true
                viewModel.updateProfile(pet.id, breed, kg,
                    when (sterilized) { 1 -> true; 2 -> false; else -> null }, chip) { result ->
                    busy = false
                    error = result
                    if (result == null) editing = false
                }
            }) { Text("Guardar") } },
            dismissButton = { TextButton(enabled = !busy, onClick = { editing = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileAction(emoji: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(50.dp)) { Text(emoji) }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
