package pe.mimascota.app.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.data.PushSubscription

/** A user-controlled feature; no background registration or unrequested consent. */
@Composable
fun PushSettings() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(PushSubscription.isEnabled(context)) }
    var busy by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    fun subscribe() {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { PushSubscription.enable(context) }
                .onSuccess {
                    enabled = true
                    note = "Avisos de comunidad activados en este dispositivo."
                }
                .onFailure { note = "No fue posible activar avisos: ${it.message}" }
            busy = false
        }
    }
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) subscribe() else note = "Permiso no concedido. Puedes activarlo más adelante."
    }
    Text("🔔 Avisos de comunidad", style = MaterialTheme.typography.titleMedium)
    Text("Recibe avisos de tus solicitudes de adopción y contenidos aprobados. Requiere Firebase y conexión.",
        style = MaterialTheme.typography.bodySmall)
    if (enabled) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !busy, onClick = {
            busy = true
            scope.launch {
                val success = PushSubscription.disable(context)
                enabled = false
                note = if (success) "Avisos desactivados."
                    else "Avisos desactivados localmente. No se pudo confirmar la revocación remota; vuelve a conectarte."
                busy = false
            }
        }) { Text("Desactivar avisos") }
        if (context.getSharedPreferences("mi_mascota_push", android.content.Context.MODE_PRIVATE)
                .getBoolean("needs_reregister", false)) {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !busy,
                onClick = { subscribe() }) { Text("Actualizar registro de avisos") }
        }
    } else {
        Button(modifier = Modifier.fillMaxWidth(), enabled = !busy, onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !PushSubscription.hasPermission(context)) {
                permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else subscribe()
        }) { Text(if (busy) "Configurando..." else "Activar avisos (opcional)") }
    }
    note?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
}
