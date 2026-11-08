package pe.mimascota.app.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.mimascota.app.data.HealthDao
import pe.mimascota.app.data.ReminderEntity
import pe.mimascota.app.data.VaccineEntity
import pe.mimascota.app.notifications.ReminderNotifications
import java.time.LocalDate
import java.util.UUID

@Composable
fun HealthScreen(petId: String, dao: HealthDao, onBack: () -> Unit, petName: String = "", initialTab: Int = 0) {
    val vaccines by remember(petId) { dao.vaccines(petId) }.collectAsStateWithLifecycle(emptyList())
    val reminders by remember(petId) { dao.reminders(petId) }.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var tab by remember(petId, initialTab) { mutableIntStateOf(initialTab) }
    var showAdd by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var vaccine by remember { mutableStateOf("") }
    var vaccineApplied by remember { mutableStateOf("") }
    var vaccineDue by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(ReminderNotifications.hasPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsEnabled = it
    }
    LaunchedEffect(reminders, notificationsEnabled) {
        if (notificationsEnabled) reminders.forEach { ReminderNotifications.schedule(context, it) }
    }
    val nextVaccine = vaccines.filter { !it.dueAt.isNullOrBlank() && it.dueAt >= LocalDate.now().toString() }
        .minByOrNull { it.dueAt.orEmpty() }

    Box(Modifier.fillMaxSize().background(Color(0xFFF0F8FA))) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().background(Color(0xFF006574)).padding(16.dp)) {
                TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
                Text(if (tab == 0) "💉 Vacunas" else "📅 Recordatorios",
                    color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ElevatedCard(shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🐾  ${petName.ifBlank { "Mi mascota" }}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("💉 Vacunas") })
                    FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("📅 Recordatorios") })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (tab == 1 && !notificationsEnabled && Build.VERSION.SDK_INT >= 33) {
                    OutlinedButton(onClick = {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text("🔔 Activar notificaciones") }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)) {
                    if (tab == 0) {
                        item {
                            ElevatedCard(shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFDDF5E8))) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🛡️ Próxima vacuna", style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    Text(nextVaccine?.name ?: "Sin vacunas próximas registradas")
                                    nextVaccine?.dueAt?.let { Text("📅 $it") }
                                }
                            }
                        }
                        item { Text("Historial de vacunas (${vaccines.size})", style = MaterialTheme.typography.titleMedium) }
                        if (vaccines.isEmpty()) item { Text("Registra una vacuna con el botón +.") }
                        items(vaccines, key = { it.id }) { item ->
                            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                                ListItem(headlineContent = { Text("💉 ${item.name}") },
                                    supportingContent = {
                                        Text("Aplicada: ${item.appliedAt ?: "Sin indicar"} · Próxima: ${item.dueAt ?: "Sin indicar"}")
                                    })
                            }
                        }
                    } else {
                        item {
                            Text("Pendientes: ${reminders.count { !it.completed }}",
                                style = MaterialTheme.typography.titleMedium)
                        }
                        if (reminders.isEmpty()) item { Text("Todavía no hay recordatorios. Toca + para agregar.") }
                        items(reminders, key = { it.id }) { item ->
                            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                                ListItem(
                                    headlineContent = { Text(item.title) },
                                    supportingContent = { Text("📅 ${item.dueAt}") },
                                    leadingContent = { Text("🔔") },
                                    trailingContent = {
                                        Switch(checked = !item.completed, onCheckedChange = { active ->
                                            scope.launch {
                                                runCatching { dao.completeReminder(item.id, !active) }
                                                    .onFailure { error = "No se pudo cambiar el recordatorio" }
                                            }
                                        })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAdd = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
    if (showAdd) AlertDialog(
        onDismissRequest = { if (!busy) showAdd = false },
        title = { Text(if (tab == 0) "Registrar vacuna" else "Nuevo recordatorio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tab == 0) {
                    OutlinedTextField(vaccine, { vaccine = it }, label = { Text("Nombre de vacuna") })
                    OutlinedTextField(vaccineApplied, { vaccineApplied = it }, label = { Text("Fecha aplicada AAAA-MM-DD (opcional)") })
                    OutlinedTextField(vaccineDue, { vaccineDue = it }, label = { Text("Próxima dosis AAAA-MM-DD (opcional)") })
                } else {
                    OutlinedTextField(reminder, { reminder = it }, label = { Text("Recordatorio") })
                    OutlinedTextField(due, { due = it }, label = { Text("Fecha AAAA-MM-DD") })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(enabled = !busy && (if (tab == 0) vaccine.isNotBlank() else reminder.isNotBlank()),
            onClick = {
                val appliedDate = runCatching { vaccineApplied.takeIf { it.isNotBlank() }?.let(LocalDate::parse) }.getOrNull()
                val nextDate = runCatching { vaccineDue.takeIf { it.isNotBlank() }?.let(LocalDate::parse) }.getOrNull()
                val date = runCatching { due.takeIf { it.isNotBlank() }?.let(LocalDate::parse) }.getOrNull()
                val valid = if (tab == 0) (vaccineApplied.isBlank() || appliedDate != null) &&
                    (vaccineDue.isBlank() || nextDate != null) else date != null
                if (!valid) { error = "Usa fechas con formato AAAA-MM-DD"; return@Button }
                busy = true
                scope.launch {
                    runCatching {
                        if (tab == 0) dao.addVaccine(VaccineEntity(UUID.randomUUID().toString(), petId,
                            vaccine.trim(), appliedDate?.toString(), nextDate?.toString()))
                        else dao.addReminder(ReminderEntity(UUID.randomUUID().toString(), petId,
                            reminder.trim(), date!!.toString()))
                    }.onSuccess {
                        vaccine = ""; vaccineApplied = ""; vaccineDue = ""; reminder = ""; due = ""
                        error = null
                        showAdd = false
                    }.onFailure { error = "No se pudo guardar: ${it.message}" }
                    busy = false
                }
            }
        ) { Text("Guardar") } },
        dismissButton = { TextButton(enabled = !busy, onClick = { showAdd = false }) { Text("Cancelar") } }
    )
}
