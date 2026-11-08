package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import pe.mimascota.app.notifications.ReminderNotifications
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.mimascota.app.data.HealthDao
import pe.mimascota.app.data.ReminderEntity
import pe.mimascota.app.data.VaccineEntity
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun HealthScreen(petId: String, dao: HealthDao, onBack: () -> Unit) {
    val vaccines by dao.vaccines(petId).collectAsStateWithLifecycle(initialValue = emptyList())
    val reminders by dao.reminders(petId).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(ReminderNotifications.hasPermission(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsEnabled = it
    }
    LaunchedEffect(reminders, notificationsEnabled) {
        if (notificationsEnabled) reminders.forEach { ReminderNotifications.schedule(context, it) }
    }
    var vaccine by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver a mis mascotas") }
        Text("💉 Salud y recordatorios", style = MaterialTheme.typography.headlineSmall)
        if (!notificationsEnabled && Build.VERSION.SDK_INT >= 33) {
            OutlinedButton(onClick = {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }) { Text("🔔 Activar avisos de recordatorios") }
            Text("Los recordatorios se guardan sin permiso; los avisos necesitan autorización.",
                style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(vaccine, { vaccine = it }, label = { Text("Vacuna aplicada o pendiente") })
        Button(enabled = vaccine.isNotBlank() && !busy, onClick = {
            busy = true
            scope.launch {
                runCatching { dao.addVaccine(VaccineEntity(UUID.randomUUID().toString(), petId, vaccine.trim(), null, null)) }
                    .onSuccess { vaccine = ""; error = null }
                    .onFailure { error = "No se pudo guardar la vacuna" }
                busy = false
            }
        }) { Text("Registrar vacuna") }
        OutlinedTextField(reminder, { reminder = it }, label = { Text("Recordatorio") })
        OutlinedTextField(due, { due = it }, label = { Text("Fecha AAAA-MM-DD") })
        Button(enabled = reminder.isNotBlank() && due.isNotBlank() && !busy, onClick = {
            val date = runCatching { LocalDate.parse(due) }.getOrNull()
            if (date == null) error = "Fecha inválida"
            else {
                error = null
                busy = true
                scope.launch {
                    runCatching { dao.addReminder(ReminderEntity(UUID.randomUUID().toString(), petId, reminder.trim(), date.toString())) }
                        .onSuccess { reminder = ""; due = "" }
                        .onFailure { error = "No se pudo guardar el recordatorio" }
                    busy = false
                }
            }
        }) { Text("Guardar recordatorio") }
        if (error != null) Text(error ?: "", color = MaterialTheme.colorScheme.error)
        HorizontalDivider()
        LazyColumn {
            item { Text("Vacunas", style = MaterialTheme.typography.titleMedium) }
            items(vaccines, key = { it.id }) { Text("• ${it.name}", modifier = Modifier.padding(8.dp)) }
            item { Text("Recordatorios", style = MaterialTheme.typography.titleMedium) }
            items(reminders, key = { it.id }) { item ->
                Row {
                    Checkbox(checked = item.completed, onCheckedChange = { checked ->
                        scope.launch {
                            runCatching { dao.completeReminder(item.id, checked) }
                                .onFailure { error = "No se pudo actualizar el recordatorio" }
                        }
                    })
                    Text("${item.title} — ${item.dueAt}", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
