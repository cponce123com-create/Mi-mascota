package pe.mimascota.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.mimascota.app.data.HealthDao
import pe.mimascota.app.data.PhotoEntity
import java.util.UUID

@Composable
fun PhotoScreen(petId: String, dao: HealthDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photos by dao.photos(petId).collectAsStateWithLifecycle(initialValue = emptyList())
    var error by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                scope.launch { dao.addPhoto(PhotoEntity(UUID.randomUUID().toString(), petId, uri.toString(), System.currentTimeMillis())) }
            } catch (e: SecurityException) { error = "No se pudo conservar el permiso de la imagen" }
        }
    }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Álbum de recuerdos", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = { picker.launch(arrayOf("image/*")) }) { Text("Añadir foto") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Text("Las fotos se conservan en el almacenamiento elegido; esta versión guarda su referencia local.")
        LazyColumn {
            items(photos, key = { it.id }) { photo ->
                ListItem(headlineContent = { Text("Fotografía guardada") },
                    supportingContent = { Text(java.text.DateFormat.getDateInstance().format(java.util.Date(photo.createdAt))) },
                    trailingContent = {
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(photo.uri)).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(intent) }.onFailure { error = "No se puede abrir la imagen" }
                        }) { Text("Ver") }
                    })
            }
        }
    }
}
