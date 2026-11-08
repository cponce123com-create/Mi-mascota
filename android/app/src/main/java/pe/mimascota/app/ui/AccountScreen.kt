package pe.mimascota.app.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
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
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Mi cuenta", style = MaterialTheme.typography.headlineSmall)
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
        message?.let { Text(it) }
        Text("Tu cuenta de Google no sincroniza automáticamente los datos locales.", style = MaterialTheme.typography.bodySmall)
    }
}
