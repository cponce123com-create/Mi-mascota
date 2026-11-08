package pe.mimascota.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.backup.BackupExporter
import pe.mimascota.app.data.PetDao

@Composable
fun BackupScreen(dao: PetDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            message = runCatching { BackupExporter(context, dao).export(uri) }
                .fold({ "Archivo exportado" }, { "Error: ${it.message}" })
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Exportar mis mascotas", style = MaterialTheme.typography.headlineSmall)
        Text("Exporta los nombres, especies y fechas de nacimiento a un archivo JSON. Puedes elegir Google Drive como destino si está disponible en el selector de Android.")
        Text("Este archivo NO incluye fotos, vacunas, recordatorios ni gastos. Todavía no existe restauración automática.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = { launcher.launch("mi-mascota-respaldo.json") }) { Text("Guardar archivo") }
        Text(message)
    }
}
