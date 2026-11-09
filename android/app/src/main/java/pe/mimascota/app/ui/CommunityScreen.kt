package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.data.CommunityApi
import pe.mimascota.app.data.CommunityPost
import pe.mimascota.app.data.CommunityComment
import pe.mimascota.app.data.LostPetListing
import pe.mimascota.app.data.communityToken

@Composable
fun CommunityScreen(onBack: () -> Unit, onAccount: () -> Unit = {},
    onAdoption: () -> Unit = {}, onLost: () -> Unit = {}, onTips: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val api = remember { CommunityApi(BuildConfig.API_BASE_URL) }
    val configured = BuildConfig.API_BASE_URL.startsWith("https://")
    var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
    var lost by remember { mutableStateOf<List<LostPetListing>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var composing by remember { mutableStateOf(false) }
    var viewingPost by remember { mutableStateOf<CommunityPost?>(null) }
    var comments by remember { mutableStateOf<List<CommunityComment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }
    var commentMessage by remember { mutableStateOf<String?>(null) }
    var commentsBusy by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    fun loadComments(id: String) {
        commentsBusy = true
        scope.launch {
            runCatching { api.comments(id) }
                .onSuccess { comments = it; commentMessage = null }
                .onFailure { commentMessage = it.message }
            commentsBusy = false
        }
    }
    fun refresh() {
        if (!configured || loading) return
        loading = true
        scope.launch {
            runCatching {
                val newPosts = api.posts()
                val newLost = api.lostPets()
                posts = newPosts
                lost = newLost
            }.onFailure { error = "No se pudo cargar la comunidad: ${it.message}" }
                .onSuccess { error = null }
            loading = false
        }
    }
    LaunchedEffect(configured) { if (configured) refresh() }
    Box(Modifier.fillMaxSize().background(PetPalette.Mist)) {
    Column(Modifier.fillMaxSize()) {
        PetHeader("🐾 Comunidad", onBack,
            subtitle = "Historias, apoyo y mascotas que nos unen.")
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Todo") })
                FilterChip(selected = false, onClick = onAdoption, label = { Text("Adopción") })
                FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Perdidos") })
                FilterChip(selected = false, onClick = onTips, label = { Text("Consejos") })
            }
            if (!configured) {
                PetRoundedCard {
                    Text("🌐 Comunidad en línea", style = MaterialTheme.typography.titleMedium)
                    Text("El servidor HTTPS aún no está configurado. Tus datos locales siguen disponibles.")
                    OutlinedButton(onClick = onAdoption) { Text("🏡 Ver adopciones") }
                    OutlinedButton(onClick = onLost) { Text("📢 Mascotas perdidas") }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { composing = true }, modifier = Modifier.weight(1f)) { Text("✍️ Publicar") }
                    OutlinedButton(onClick = { refresh() }, enabled = !loading) { Text("↻ Actualizar") }
                }
                message?.let { Text(it, color = Color(0xFF006574)) }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (tab == 0) {
                        if (posts.isEmpty() && !loading && error == null) item { Text("Aún no hay publicaciones aprobadas.") }
                        items(posts) { post ->
                            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🐾 Comunidad · Publicación aprobada",
                                        color = PetPalette.Muted, style = MaterialTheme.typography.labelSmall)
                                    Text(post.title, style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold)
                                    Text(post.body, maxLines = 6, overflow = TextOverflow.Ellipsis)
                                    TextButton(onClick = {
                                        viewingPost = post
                                        commentText = ""
                                        loadComments(post.id)
                                    }) { Text("💬 Ver comentarios") }
                                }
                            }
                        }
                    } else {
                        item { OutlinedButton(onClick = onLost, modifier = Modifier.fillMaxWidth()) {
                            Text("📢 Reportar mascota perdida")
                        } }
                        if (lost.isEmpty() && !loading && error == null) item { Text("No hay alertas aprobadas activas.") }
                        items(lost) { pet ->
                            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("📢 ${pet.name}", style = MaterialTheme.typography.titleMedium)
                                    Text("${pet.species} · ${pet.district ?: "Distrito sin indicar"}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    FloatingActionButton(onClick = {
        if (configured) composing = true else message = "Primero configura el servidor comunitario."
    }, modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        containerColor = PetPalette.Green, contentColor = Color.White) {
        Text("+", style = MaterialTheme.typography.headlineMedium)
    }
    }
    if (composing) AlertDialog(
        onDismissRequest = { if (!sending) composing = false },
        title = { Text("✍️ Nueva publicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Las publicaciones requieren inicio de sesión y aprobación antes de hacerse públicas.")
                OutlinedTextField(title, { title = it }, label = { Text("Título (3–120 caracteres)") })
                OutlinedTextField(body, { body = it }, label = { Text("Historia (5–3000 caracteres)") },
                    minLines = 3, maxLines = 6)
            }
        },
        confirmButton = { Button(enabled = !sending && configured &&
            title.trim().length in 3..120 && body.trim().length in 5..3000, onClick = {
            sending = true
            scope.launch {
                runCatching {
                    val token = communityToken() ?: throw IllegalStateException("Inicia sesión con Google en Mi cuenta.")
                    api.post(title.trim(), body.trim(), token)
                }.onSuccess {
                    composing = false
                    title = ""; body = ""
                    message = "Publicación recibida. Se mostrará después de la revisión."
                }.onFailure { message = it.message ?: "No fue posible publicar." }
                sending = false
            }
        }) { Text(if (sending) "Enviando..." else "Enviar a revisión") } },
        dismissButton = {
            TextButton(enabled = !sending, onClick = { composing = false; onAccount() }) { Text("Mi cuenta") }
            TextButton(enabled = !sending, onClick = { composing = false }) { Text("Cancelar") }
        }
    )
    viewingPost?.let { post ->
        AlertDialog(
            onDismissRequest = { if (!commentsBusy) viewingPost = null },
            title = { Text("💬 ${post.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(post.body, maxLines = 4, overflow = TextOverflow.Ellipsis)
                    if (commentsBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
                    commentMessage?.let { Text(it) }
                    LazyColumn(Modifier.heightIn(max = 220.dp)) {
                        if (comments.isEmpty()) item { Text("No hay comentarios aprobados.") }
                        items(comments, key = { it.id }) { comment ->
                            Text(comment.body, modifier = Modifier.padding(vertical = 6.dp))
                            HorizontalDivider()
                        }
                    }
                    OutlinedTextField(commentText, { commentText = it },
                        label = { Text("Tu comentario") }, minLines = 2, maxLines = 4)
                    Text("Los comentarios nuevos se muestran después de la moderación.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { Button(enabled = !commentsBusy && commentText.trim().length in 2..1000,
                onClick = {
                    commentsBusy = true
                    scope.launch {
                        runCatching {
                            val token = communityToken() ?: throw IllegalStateException("Inicia sesión con Google.")
                            api.submitComment(post.id, commentText.trim(), token)
                        }.onSuccess {
                            commentText = ""
                            commentMessage = "Comentario enviado a revisión."
                        }.onFailure { commentMessage = it.message }
                        commentsBusy = false
                    }
                }) { Text("Comentar") } },
            dismissButton = { TextButton(enabled = !commentsBusy,
                onClick = { viewingPost = null }) { Text("Cerrar") } }
        )
    }

}
