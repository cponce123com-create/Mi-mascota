package pe.mimascota.app.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.notifications.ReminderNotifications

/** Local settings and transparency screen. Never advertises remote sync as enabled. */
@Composable
fun AppSettingsScreen(
    mode: String,
    onBack: () -> Unit,
    onBackup: () -> Unit,
    onTips: () -> Unit
) {
    val context = LocalContext.current
    val about = mode == "ABOUT"
    var notifications by remember { mutableStateOf(ReminderNotifications.hasPermission(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notifications = it
    }
    Column(Modifier.fillMaxSize().background(PetPalette.Mist)) {
        PetHeader(if (about) "ℹ️ Acerca de" else "⚙️ Configuración", onBack,
            subtitle = if (about) "Información de la aplicación." else "Preferencias y ayuda.")
        LazyColumn(contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!about) {
                item {
                    PetRoundedCard {
                        Text("🔔 Notificaciones", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        PetInfoRow("Permiso de notificaciones",
                            if (notifications) "Concedido" else "Pendiente", "🔔")
                        if (!notifications && Build.VERSION.SDK_INT >= 33) {
                            Button(onClick = {
                                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Autorizar notificaciones")
                            }
                        }
                        Text("Los recordatorios locales funcionan con las fechas guardadas; " +
                            "los avisos de comunidad requieren cuenta y servidor configurados.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    PetRoundedCard {
                        Text("☁️ Datos y almacenamiento",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        PetInfoRow("Sincronización automática", "No disponible", "☁️")
                        Button(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
                            Text("Respaldar y restaurar")
                        }
                    }
                }
                item {
                    PetRoundedCard {
                        Text("🐾 Ayuda", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = onTips, modifier = Modifier.fillMaxWidth()) {
                            Text("Consejos para el cuidado")
                        }
                    }
                }
            } else {
                item {
                    PetRoundedCard {
                        Text("🐾 Mi Mascota", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        PetInfoRow("Versión", BuildConfig.VERSION_NAME, "📱")
                        PetInfoRow("Plataforma", "Android", "🤖")
                        Text("Mi Mascota ayuda a registrar cuidados, recuerdos y gastos. " +
                            "Algunos servicios comunitarios requieren conexión.",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("Este software se encuentra en desarrollo; no sustituye " +
                            "la atención de un profesional veterinario.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                item {
                    PetRoundedCard {
                        Text("👩‍💻 Proyecto y soporte",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/cponce123com-create/Mi-mascota")))
                        }) {
                            Text("Ver código y reportar problemas")
                        }
                    }
                }
            }
        }
    }
}
