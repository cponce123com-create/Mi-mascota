package pe.mimascota.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.backup.BackupExporter
import pe.mimascota.app.backup.BackupImporter
import pe.mimascota.app.backup.BackupPhotoArchive
import pe.mimascota.app.backup.PreparedPhotoArchive
import pe.mimascota.app.data.BackupDao
import pe.mimascota.app.data.BackupSnapshot

@Composable
fun BackupScreen(dao: BackupDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val importer = remember(context, dao) { BackupImporter(context, dao) }
    val photoArchive = remember(context, dao) { BackupPhotoArchive(context, dao) }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<BackupSnapshot?>(null) }
    var pendingZip by remember { mutableStateOf<PreparedPhotoArchive?>(null) }
    var secretMode by remember { mutableStateOf<String?>(null) }
    var secretPassword by remember { mutableStateOf("") }
    var secretConfirm by remember { mutableStateOf("") }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            message = runCatching { BackupExporter(context, dao).export(uri) }
                .fold({ "Respaldo exportado correctamente." }, { "Error al exportar: ${it.message}" })
            busy = false
        }
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            runCatching { importer.read(uri) }
                .onSuccess { pending = it; message = "Archivo validado. Confirma la restauración." }
                .onFailure { message = "No se pudo leer el respaldo: ${it.message}" }
            busy = false
        }
    }
    val zipExportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            message = runCatching { photoArchive.export(uri) }.fold(
                { count -> "Respaldo ZIP guardado con $count fotografías." },
                { "Error al respaldar fotos: ${it.message}" }
            )
            busy = false
        }
    }
    val zipImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            runCatching { photoArchive.prepare(uri) }
                .onSuccess { pendingZip?.discard(); pendingZip = it; message = "ZIP validado." }
                .onFailure { message = "ZIP no válido: ${it.message}" }
            busy = false
        }
    }
    val encryptedExportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val password = secretPassword
        secretPassword = ""
        secretConfirm = ""
        if (uri != null && password.length >= 8) scope.launch {
            busy = true
            message = runCatching { photoArchive.exportEncrypted(uri, password) }
                .fold({ count -> "Copia cifrada guardada con $count fotografías." },
                    { "Error al cifrar: ${it.message}" })
            busy = false
        }
    }
    val encryptedImportPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val password = secretPassword
        secretPassword = ""
        secretConfirm = ""
        if (uri != null && password.isNotBlank()) scope.launch {
            busy = true
            runCatching { photoArchive.prepareEncrypted(uri, password) }
                .onSuccess { pendingZip?.discard(); pendingZip = it; message = "Respaldo cifrado verificado." }
                .onFailure { message = "Contraseña incorrecta o archivo dañado: ${it.message}" }
            busy = false
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("☁️ Respaldar y restaurar", style = MaterialTheme.typography.headlineSmall)
        ElevatedCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🔐 Copia protegida con contraseña", style = MaterialTheme.typography.titleMedium)
                Text("Cifra mascotas, gastos, salud y fotografías con AES-256-GCM. Recomendado para datos personales.")
                Button(enabled = !busy, onClick = { secretMode = "EXPORT" }, modifier = Modifier.fillMaxWidth()) {
                    Text("Crear respaldo cifrado")
                }
                OutlinedButton(enabled = !busy, onClick = { secretMode = "IMPORT" },
                    modifier = Modifier.fillMaxWidth()) { Text("Restaurar respaldo cifrado") }
            }
        }
        Text("Respaldo completo con fotografías", style = MaterialTheme.typography.titleMedium)
        ElevatedCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ZIP con fotografías", style = MaterialTheme.typography.titleMedium)
                Text("Incluye los registros y las imágenes accesibles en el dispositivo, hasta 300 fotos y 100 MB.")
                Button(enabled = !busy, onClick = { zipExportPicker.launch("mi-mascota-completo.zip") },
                    modifier = Modifier.fillMaxWidth()) { Text("📦 Exportar ZIP completo") }
                OutlinedButton(enabled = !busy, onClick = { zipImportPicker.launch(arrayOf("application/zip", "application/octet-stream")) },
                    modifier = Modifier.fillMaxWidth()) { Text("📂 Restaurar ZIP con fotos") }
            }
        }
        ElevatedCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Guardar una copia JSON", style = MaterialTheme.typography.titleMedium)
                Text("Exporta mascotas, vacunas, recordatorios, gastos y referencias fotográficas en un archivo JSON.")
                Button(enabled = !busy, onClick = { exportPicker.launch("mi-mascota-respaldo.json") },
                    modifier = Modifier.fillMaxWidth()) { Text("💾 Exportar respaldo") }
            }
        }
        ElevatedCard {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Recuperar mis datos", style = MaterialTheme.typography.titleMedium)
                Text("Importa un respaldo v1, v2 o v3. Los IDs coincidentes se actualizan; se conservan los demás datos locales.")
                OutlinedButton(enabled = !busy, onClick = { importPicker.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.fillMaxWidth()) { Text("📂 Seleccionar archivo") }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (message.isNotBlank()) Text(message)
        Text("Privacidad: los archivos ZIP y JSON no están cifrados. Guárdalos en un lugar privado. La restauración ZIP copia las fotos al almacenamiento interno; el respaldo JSON no copia los archivos.",
            style = MaterialTheme.typography.bodySmall)
    }
    pending?.let { snapshot ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("¿Restaurar datos?") },
            text = { Text("Se importarán ${snapshot.pets.size} mascotas, ${snapshot.vaccines.size} vacunas, ${snapshot.reminders.size} recordatorios, ${snapshot.expenses.size} gastos y ${snapshot.photos.size} referencias fotográficas. Los registros con el mismo ID se actualizarán.") },
            confirmButton = { Button(onClick = {
                pending = null
                scope.launch {
                    busy = true
                    message = runCatching { importer.restore(snapshot) }
                        .fold({ "Datos restaurados. Revisa que las imágenes sigan disponibles." },
                            { "Error al restaurar: ${it.message}. No se aplicó una restauración parcial." })
                    busy = false
                }
            }) { Text("Restaurar") } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } }
        )
    }
    pendingZip?.let { prepared ->
        if (!busy) AlertDialog(
            onDismissRequest = { prepared.discard(); pendingZip = null },
            title = { Text("¿Restaurar ZIP completo?") },
            text = { Text("Se restaurarán ${prepared.snapshot.pets.size} mascotas y ${prepared.images.size} fotografías, además de salud y gastos. Los registros con el mismo ID se actualizarán.") },
            confirmButton = { Button(onClick = {
                busy = true
                scope.launch {
                    message = runCatching { photoArchive.restore(prepared) }
                        .fold({ "Respaldo ZIP restaurado con sus fotografías." },
                            { "Error en la restauración ZIP: ${it.message}" })
                    pendingZip = null
                    busy = false
                }
            }) { Text("Restaurar todo") } },
            dismissButton = { TextButton(onClick = {
                prepared.discard()
                pendingZip = null
            }) { Text("Cancelar") } }
        )
    }

    secretMode?.let { mode ->
        AlertDialog(
            onDismissRequest = { secretMode = null; secretPassword = ""; secretConfirm = "" },
            title = { Text(if (mode == "EXPORT") "Proteger respaldo" else "Abrir respaldo protegido") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("La contraseña no se almacena. Si la pierdes, no podrás recuperar el archivo.")
                    OutlinedTextField(secretPassword, { secretPassword = it },
                        label = { Text("Contraseña (mínimo 8 caracteres)") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
                    if (mode == "EXPORT") {
                        OutlinedTextField(secretConfirm, { secretConfirm = it },
                            label = { Text("Repetir contraseña") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
                    }
                }
            },
            confirmButton = { Button(enabled = secretPassword.length in 8..256 &&
                (mode != "EXPORT" || secretPassword == secretConfirm), onClick = {
                secretMode = null
                if (mode == "EXPORT") encryptedExportPicker.launch("mi-mascota-protegido.mmpet")
                else encryptedImportPicker.launch(arrayOf("application/octet-stream", "*/*"))
            }) { Text("Continuar") } },
            dismissButton = { TextButton(onClick = {
                secretMode = null; secretPassword = ""; secretConfirm = ""
            }) { Text("Cancelar") } }
        )
    }

}
