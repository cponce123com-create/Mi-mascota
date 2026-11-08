package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
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
    var vaccine by remember { mutableStateOf("") }
    var reminder by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver a mis mascotas") }
        Text("Salud y recordatorios", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(vaccine, { vaccine = it }, label = { Text("Vacuna aplicada o pendiente") })
        Button(enabled = vaccine.isNotBlank(), onClick = {
            scope.launch {
                dao.addVaccine(VaccineEntity(UUID.randomUUID().toString(), petId, vaccine.trim(), null, null))
                vaccine = ""
            }
        }) { Text("Registrar vacuna") }
        OutlinedTextField(reminder, { reminder = it }, label = { Text("Recordatorio") })
        OutlinedTextField(due, { due = it }, label = { Text("Fecha AAAA-MM-DD") })
        Button(enabled = reminder.isNotBlank() && due.isNotBlank(), onClick = {
            val date = runCatching { LocalDate.parse(due) }.getOrNull()
            if (date == null) error = "Fecha inválida"
            else {
                error = null
                scope.launch {
                    dao.addReminder(ReminderEntity(UUID.randomUUID().toString(), petId, reminder.trim(), date.toString()))
                    reminder = ""; due = ""
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
                        scope.launch { dao.completeReminder(item.id, checked) }
                    })
                    Text("${item.title} — ${item.dueAt}", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
