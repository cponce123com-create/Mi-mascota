package pe.mimascota.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.auth.GoogleSignInManager
import pe.mimascota.app.data.CommunityApi
import pe.mimascota.app.data.communityToken
import pe.mimascota.app.data.PushSubscription

@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onModeration: () -> Unit,
    onBackup: () -> Unit,
    onEraseLocal: suspend () -> Unit,
    preferredMethod: String = "GOOGLE",
    onSettings: () -> Unit = {},
    onAbout: () -> Unit = {}
) {
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val configured = FirebaseApp.getApps(activity).isNotEmpty()
    val googleConfigured = configured && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    val apiConfigured = BuildConfig.API_BASE_URL.startsWith("https://")
    var uid by remember { mutableStateOf(if (configured) FirebaseAuth.getInstance().currentUser?.uid else null) }
    var busy by remember { mutableStateOf(false) }
    var method by remember(preferredMethod) { mutableStateOf(preferredMethod) }
    var accountEmail by remember { mutableStateOf("") }
    var accountPassword by remember { mutableStateOf("") }
    var registering by remember { mutableStateOf(false) }
    val avatarPrefs = remember { activity.getSharedPreferences("mi_mascota_ui", Activity.MODE_PRIVATE) }
    var avatarUri by remember { mutableStateOf(avatarPrefs.getString("account_avatar", null)) }
    val avatarPicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                activity.contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
                avatarUri = uri.toString()
                avatarPrefs.edit().putString("account_avatar", avatarUri).apply()
            }.onFailure { message = "No se pudo seleccionar la imagen de perfil." }
        }
    }
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
            Text("👤 Mi cuenta", style = MaterialTheme.typography.headlineMedium,
                color = Color.White, fontWeight = FontWeight.Bold)
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(85.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center) {
                    if (avatarUri == null) Text("👤", fontSize = 53.sp)
                    else AndroidView(factory = { ctx -> ImageView(ctx).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    } }, update = { view ->
                        runCatching { view.setImageURI(Uri.parse(avatarUri)) }
                            .onFailure { view.setImageDrawable(null) }
                    }, modifier = Modifier.fillMaxSize().clip(CircleShape))
                }
                TextButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }) {
                    Text("📷 Cambiar foto", color = Color.White)
                }
                Text(accountName, style = MaterialTheme.typography.titleLarge,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Text(email ?: "Tu espacio de cuidado animal", color = Color.White)
            }
        }
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔐 Acceso", style = MaterialTheme.typography.titleMedium)
                    if (!configured) {
                        Text("Acceso en línea todavía no configurado. Puedes usar Mi Mascota sin cuenta.")
                        Text("Añade google-services.json y activa el proveedor correspondiente en Firebase.",
                            style = MaterialTheme.typography.bodySmall)
                    } else if (uid == null) {
                        Row(Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = method == "GOOGLE",
                                onClick = { method = "GOOGLE"; message = null },
                                label = { Text("🌐 Google") })
                            FilterChip(selected = method == "EMAIL",
                                onClick = { method = "EMAIL"; message = null },
                                label = { Text("✉️ Correo") })
                        }
                        if (method == "GOOGLE") {
                            if (googleConfigured) {
                                Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                                    busy = true
                                    scope.launch {
                                        val result = GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signIn()
                                        result.onSuccess { uid = it; message = "Sesión iniciada." }
                                            .onFailure { message = "No se pudo iniciar sesión con Google." }
                                        busy = false
                                    }
                                }) { Text(if (busy) "Conectando..." else "Continuar con Google") }
                            } else Text("Configura GOOGLE_WEB_CLIENT_ID para iniciar sesión con Google.")
                        } else {
                            OutlinedTextField(accountEmail, { accountEmail = it.trim().take(254) },
                                modifier = Modifier.fillMaxWidth(), label = { Text("Correo electrónico") },
                                singleLine = true)
                            OutlinedTextField(accountPassword, { accountPassword = it },
                                modifier = Modifier.fillMaxWidth(), label = { Text("Contraseña") },
                                visualTransformation = PasswordVisualTransformation(), singleLine = true)
                            Button(enabled = !busy && accountEmail.contains("@") && accountPassword.length >= 6,
                                modifier = Modifier.fillMaxWidth(), onClick = {
                                    busy = true
                                    scope.launch {
                                        runCatching {
                                            val auth = FirebaseAuth.getInstance()
                                            val result = if (registering)
                                                auth.createUserWithEmailAndPassword(accountEmail, accountPassword).await()
                                            else auth.signInWithEmailAndPassword(accountEmail, accountPassword).await()
                                            result.user?.uid ?: error("Sin usuario")
                                        }.onSuccess {
                                            uid = it
                                            accountPassword = ""
                                            message = if (registering)
                                                "Cuenta creada. Verifica tu correo cuando Firebase lo solicite."
                                            else "Sesión iniciada."
                                        }.onFailure { message = "No se pudo completar el acceso. Verifica tus datos y que el método esté habilitado en Firebase." }
                                        busy = false
                                    }
                                }) { Text(if (registering) "Crear cuenta" else "Entrar con correo") }
                            TextButton(onClick = { registering = !registering; message = null }) {
                                Text(if (registering) "Ya tengo cuenta" else "Crear una cuenta")
                            }
                            Text("El acceso con correo requiere activar Email/Password en Firebase.",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text("Sesión iniciada")
                        OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !busy, onClick = {
                            busy = true
                            scope.launch {
                                val wasEnabled = PushSubscription.isEnabled(activity)
                                val revoked = if (wasEnabled) PushSubscription.disable(activity) else true
                                GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signOut()
                                uid = null
                                message = if (revoked) "Sesión cerrada."
                                    else "Sesión cerrada; no se pudo confirmar la revocación de avisos del servidor."
                                busy = false
                            }
                        }) { Text("Cerrar sesión") }
                    }
                }
            }
            ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("☁️ Datos y respaldos", style = MaterialTheme.typography.titleMedium)
                    PetInfoRow("Sincronización en la nube", "No activada", "☁️")
                    Text("Los datos locales no se sincronizan automáticamente con Google.")
                    OutlinedButton(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
                        Text("📦 Respaldar y restaurar")
                    }
                    OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("⚙️ Configuración")
                    }
                    OutlinedButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
                        Text("ℹ️ Acerca de")
                    }
                    HorizontalDivider()
                    Text("🛡️ Privacidad y seguridad", style = MaterialTheme.typography.titleMedium)
                    Text("Puedes borrar todos los datos que guarda Mi Mascota en este dispositivo.")
                    OutlinedButton(onClick = { eraseTarget = "LOCAL" },
                        modifier = Modifier.fillMaxWidth()) { Text("Borrar datos del dispositivo") }
                    if (configured && uid != null && apiConfigured) {
                        Text("También puedes eliminar tus registros del servidor y tu identidad Firebase cuando sea posible.")
                        PushSettings()
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
                            runCatching { PushSubscription.disable(activity) }
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
