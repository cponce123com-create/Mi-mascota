package pe.mimascota.app.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.auth.GoogleSignInManager

@Composable
fun AccountScreen(onBack: () -> Unit) {
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val configured = FirebaseApp.getApps(activity).isNotEmpty() &&
        BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
    var uid by remember { mutableStateOf(if (configured) FirebaseAuth.getInstance().currentUser?.uid else null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA)), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.verticalGradient(listOf(Color(0xFF005766), Color(0xFF009E9A)))
            ).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("👤 Mi cuenta", style = MaterialTheme.typography.headlineMedium,
                color = Color.White, fontWeight = FontWeight.Bold)
            Text("Gestiona tu acceso y privacidad", color = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!configured) {
            Text("Inicio de sesión no configurado. Puedes seguir utilizando todas las funciones locales.")
            Text("Para habilitarlo configura Firebase y GOOGLE_WEB_CLIENT_ID. No añadas claves privadas al repositorio.")
        } else {
            Text(if (uid == null) "Sin sesión iniciada" else "Sesión iniciada")
            if (uid == null) {
                Button(enabled = !busy, onClick = {
                    busy = true
                    scope.launch {
                        val result = GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signIn()
                        result.onSuccess { uid = it; message = "Sesión iniciada" }
                            .onFailure { message = "No se pudo iniciar sesión: ${it.localizedMessage}" }
                        busy = false
                    }
                }) { Text(if (busy) "Conectando..." else "Continuar con Google") }
            } else {
                Button(onClick = {
                    GoogleSignInManager(activity, BuildConfig.GOOGLE_WEB_CLIENT_ID).signOut()
                    uid = null
                    message = "Sesión cerrada"
                }) { Text("Cerrar sesión") }
            }
        }
            }
        }
        message?.let { Text(it) }
        ElevatedCard(shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("☁️ Sincronización", fontWeight = FontWeight.SemiBold)
                Text("La sincronización automática todavía no está disponible.")
                HorizontalDivider()
                Text("🔒 Privacidad y seguridad", fontWeight = FontWeight.SemiBold)
                Text("Los datos locales permanecen en este dispositivo salvo que los exportes.")
            }
        }
        }
    }
}
