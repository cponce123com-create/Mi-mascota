package pe.mimascota.app.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.auth.GoogleSignInManager
import pe.mimascota.app.data.CommunityApi
import pe.mimascota.app.data.communityToken

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onModeration: () -> Unit,
    onBackup: () -> Unit,
    onEraseLocal: suspend () -> Unit
) {
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val configured = FirebaseApp.getApps(activity).isNotEmpty() && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    val apiConfigured = BuildConfig.API_BASE_URL.startsWith("https://")
    var uid by remember { mutableStateOf(if (configured) FirebaseAuth.getInstance().currentUser?.uid else null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var eraseTarget by remember { mutableStateOf<String?>(null) }
    val accountName = if (configured && uid != null)
        FirebaseAuth.getInstance().currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Mi cuenta"
    else "Invitado"
    val email = if (configured && uid != null) FirebaseAuth.getInstance().currentUser?.email
        else null

    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA)).verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(Color(0xFF005766), Color(0xFF009E9A)))
        ).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("👤 Mi cuenta", style = MaterialTheme.typography.headlineMedium, color = Color.White,
                fontWeight = FontWeight.Bold)
            Text("🐾 $accountName", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(email ?: "Tu espacio de cuidado animal", color = Color.White)
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔐 Acceso", style = MaterialTheme.typography.titleMedium)
                    if (!configured) {
                        Text("Google Sign-In no está configurado. La app sigue funcionando offline.")
                        Text("Para activarlo añade google-services.json y GOOGLE_WEB_CLIENT_ID en tu entorno de desarrollo.",
                            style = MaterialTheme.typography.bodySmall)
                    } else if (uid == null) {
                        Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                            busy = true
                            scope.launch {
                                val result = GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signIn()
                                result.onSuccess { uid = it; message = "Sesión iniciada." }
                                    .onFailure { message = "Error de acceso: ${it.localizedMessage}" }
                                busy = false
                            }
                        }) { Text(if (busy) "Conectando..." else "Continuar con Google") }
                    } else {
                        Text("Sesión iniciada")
                        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signOut()
                            uid = null
                            message = "Sesión cerrada."
                        }) { Text("Cerrar sesión") }
                    }
                }
            }
            ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("☁️ Datos y respaldos", style = MaterialTheme.typography.titleMedium)
                    Text("Los datos locales no se sincronizan automáticamente con Google.")
                    OutlinedButton(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
                        Text("📦 Respaldar y restaurar")
                    }
                    HorizontalDivider()
                    Text("🛡️ Privacidad y seguridad", style = MaterialTheme.typography.titleMedium)
                    Text("Puedes borrar todos los datos que guarda Mi Mascota en este dispositivo.")
                    OutlinedButton(onClick = { eraseTarget = "LOCAL" },
                        modifier = Modifier.fillMaxWidth()) { Text("Borrar datos del dispositivo") }
                    if (configured && uid != null && apiConfigured) {
                        Text("También puedes eliminar tus registros del servidor y tu identidad Firebase cuando sea posible.")
                        OutlinedButton(onClick = { eraseTarget = "REMOTE" },
                            modifier = Modifier.fillMaxWidth()) { Text("Eliminar mi cuenta online") }
                        OutlinedButton(onClick = onModeration, modifier = Modifier.fillMaxWidth()) {
                            Text("🛡️ Panel de moderación")
                        }
                        Text("El servidor permite moderar solo a las cuentas autorizadas.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Mi Mascota está en desarrollo. Antes de borrar los datos, guarda una copia si la necesitas.",
                style = MaterialTheme.typography.bodySmall)
        }
    }

    eraseTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!busy) eraseTarget = null },
            title = { Text(if (target == "LOCAL") "Borrar todos los datos locales" else "Eliminar mi cuenta online") },
            text = { Text(if (target == "LOCAL")
                "Se eliminarán las mascotas, vacunas, gastos, fotografías restauradas y juegos guardados en este dispositivo. No se borrarán tus fotos originales de otras aplicaciones."
                else "Se eliminarán publicaciones, mascotas y solicitudes del servidor. Los datos locales del dispositivo permanecerán. Esta operación no se puede deshacer.") },
            confirmButton = { Button(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    if (target == "LOCAL") {
                        runCatching { onEraseLocal() }
                            .onSuccess { message = "Datos locales eliminados." }
                            .onFailure { message = "Error al borrar datos: ${it.message}" }
                    } else {
                        runCatching {
                            val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                            CommunityApi(BuildConfig.API_BASE_URL).deleteRemoteAccount(token)
                        }.onSuccess {
                            val authDelete = runCatching {
                                FirebaseAuth.getInstance().currentUser?.delete()?.await()
                            }
                            message = if (authDelete.isSuccess)
                                "Datos remotos e identidad Firebase eliminados. Los datos locales permanecen."
                            else "Datos remotos eliminados. No se pudo eliminar la identidad Firebase: vuelve a autenticarte."
                            FirebaseAuth.getInstance().signOut()
                            uid = null
                        }.onFailure { message = "No se pudo borrar la cuenta: ${it.message}" }
                    }
                    eraseTarget = null
                    busy = false
                }
            }) { Text("Confirmar eliminación") } },
            dismissButton = { TextButton(enabled = !busy, onClick = { eraseTarget = null }) { Text("Cancelar") } }
        )
    }
}
