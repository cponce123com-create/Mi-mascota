package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.data.CommunityApi
import pe.mimascota.app.data.PrivateMessage
import pe.mimascota.app.data.communityToken

/** Messages are read on request, not a promise of real-time delivery. */
@Composable
fun AdoptionChatScreen(applicationId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val api = remember { CommunityApi(BuildConfig.API_BASE_URL) }
    val configured = BuildConfig.API_BASE_URL.startsWith("https://")
    var messages by remember(applicationId) { mutableStateOf<List<PrivateMessage>>(emptyList()) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun refresh() {
        if (busy || !configured) return
        busy = true
        scope.launch {
            runCatching {
                val bearer = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                api.chatMessages(applicationId, bearer)
            }.onSuccess { messages = it; error = null }
                .onFailure { error = "No se pudo cargar la conversación: ${it.message}" }
            busy = false
        }
    }
    LaunchedEffect(applicationId, configured) { if (configured) refresh() }

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA))) {
        Column(Modifier.fillMaxWidth().background(Color(0xFF006574)).padding(16.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("💬 Conversación de adopción", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Disponible solo para participantes de una solicitud aceptada.", color = Color.White,
                style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!configured) Text("El servidor de mensajes no está configurado.")
            else {
                OutlinedButton(onClick = { refresh() }, enabled = !busy) { Text("↻ Actualizar mensajes") }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (messages.isEmpty() && !busy) item {
                        Text("No hay mensajes todavía. Evita compartir contraseñas o datos sensibles.")
                    }
                    items(messages, key = { it.id }) { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement =
                            if (item.mine) Arrangement.End else Arrangement.Start) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(0.85f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (item.mine) Color(0xFFD8F5E8) else Color.White)
                            ) {
                                Text(item.body, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
                OutlinedTextField(input, { input = it.take(1000) },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Mensaje") }, maxLines = 4)
                Button(enabled = !busy && input.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        busy = true
                        scope.launch {
                            runCatching {
                                val bearer = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                                api.sendChatMessage(applicationId, input.trim(), bearer)
                                api.chatMessages(applicationId, bearer)
                            }.onSuccess {
                                input = ""; messages = it; error = null
                            }.onFailure { error = "No se pudo enviar: ${it.message}" }
                            busy = false
                        }
                    }) { Text("Enviar mensaje") }
            }
        }
    }
}
