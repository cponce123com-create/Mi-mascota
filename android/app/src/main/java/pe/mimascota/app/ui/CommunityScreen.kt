package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.data.CommunityApi
import pe.mimascota.app.data.CommunityPost
import pe.mimascota.app.data.LostPetListing

@Composable
fun CommunityScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var lost by remember { mutableStateOf<List<LostPetListing>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    val configured = BuildConfig.API_BASE_URL.startsWith("https://")
    fun refresh() {
        if (!configured || loading) return
        loading = true
        scope.launch {
            runCatching {
                val api = CommunityApi(BuildConfig.API_BASE_URL)
                val newPosts = api.posts()
                val newLost = api.lostPets()
                posts = newPosts
                lost = newLost
            }.onFailure { error = "No se pudo conectar. Comprueba internet o intenta más tarde." }
                .onSuccess { error = null }
            loading = false
        }
    }
    LaunchedEffect(configured) { if (configured) refresh() }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("👥 Comunidad Mascotera", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF006574), fontWeight = FontWeight.Bold)
        Text("Historias, apoyo y mascotas que nos unen")
        ElevatedCard { Column(Modifier.padding(14.dp)) {
            Text("📣 Comparte con la comunidad", style = MaterialTheme.typography.titleMedium)
            Text("Publicaciones, comentarios y reportes nuevos estarán disponibles al integrar autenticación y moderación.")
        } }
        if (!configured) {
            Text("La comunidad en línea aún no está configurada. Las funciones locales siguen disponibles.")
            Text("Configura API_BASE_URL en el proyecto Android para activar las consultas.")
        } else {
            Row {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Publicaciones") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Perdidos") })
            }
            OutlinedButton(onClick = { refresh() }, enabled = !loading) { Text("Actualizar") }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn {
                if (tab == 0) {
                    if (posts.isEmpty() && !loading && error == null) item { Text("No hay publicaciones todavía.") }
                    items(posts) { post ->
                        ListItem(headlineContent = { Text(post.title) }, supportingContent = { Text(post.body) })
                        HorizontalDivider()
                    }
                } else {
                    if (lost.isEmpty() && !loading && error == null) item { Text("No hay alertas activas.") }
                    items(lost) { pet ->
                        ListItem(headlineContent = { Text(pet.name) },
                            supportingContent = { Text("${pet.species} · ${pet.district ?: "Distrito no indicado"}") })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
