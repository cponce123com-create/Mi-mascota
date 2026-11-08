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
import pe.mimascota.app.data.ModerationItem
import pe.mimascota.app.data.communityToken

@Composable
fun ModerationScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val configured = BuildConfig.API_BASE_URL.startsWith("https://")
    val api = remember { CommunityApi(BuildConfig.API_BASE_URL) }
    var loading by remember { mutableStateOf(false) }
    var items by remember { mutableStateOf<List<ModerationItem>>(emptyList()) }
    var message by remember { mutableStateOf("Solo usuarios autorizados en el servidor pueden moderar.") }
    var pending by remember { mutableStateOf<Pair<ModerationItem, Boolean>?>(null) }

    fun refresh() {
        if (loading || !configured) return
        loading = true
        scope.launch {
            runCatching {
                val token = communityToken() ?: throw IllegalStateException("Inicia sesión con Google.")
                api.moderationQueue(token)
            }.onSuccess { items = it; message = "Pendientes: ${it.size}" }
                .onFailure { message = "No se pudo consultar la cola: ${it.message}" }
            loading = false
        }
    }
    LaunchedEffect(configured) { if (configured) refresh() }

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA))) {
        Column(Modifier.fillMaxWidth().background(Color(0xFF006574)).padding(16.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("🛡️ Moderación", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(message)
            if (!configured) Text("Primero configura el servidor HTTPS.")
            else {
                OutlinedButton(onClick = { refresh() }, enabled = !loading) { Text("↻ Actualizar cola") }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.kind + it.id }) { item ->
                        ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("${item.kind.uppercase()} · ${item.title}", style = MaterialTheme.typography.titleMedium)
                                Text(item.details)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { pending = item to true }) { Text("Aprobar") }
                                    OutlinedButton(onClick = { pending = item to false }) { Text("Rechazar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    pending?.let { (item, approve) ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text(if (approve) "¿Aprobar contenido?" else "¿Rechazar contenido?") },
            text = { Text("${item.title}\nEsta decisión se registrará en el servidor.") },
            confirmButton = { Button(onClick = {
                pending = null
                loading = true
                scope.launch {
                    runCatching {
                        val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                        api.moderate(item, approve, token)
                    }.onSuccess {
                        items = items.filterNot { it.id == item.id && it.kind == item.kind }
                        message = "Decisión aplicada."
                    }.onFailure { message = "No se aplicó: ${it.message}" }
                    loading = false
                }
            }) { Text("Confirmar") } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancelar") } }
        )
    }
}
