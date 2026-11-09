package pe.mimascota.app.ui

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.mimascota.app.data.HealthDao
import pe.mimascota.app.data.PhotoEntity
import java.util.UUID

@Composable
fun PhotoScreen(petId: String, dao: HealthDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photos by remember(petId) { dao.photos(petId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                scope.launch {
                    runCatching { dao.addPhoto(PhotoEntity(UUID.randomUUID().toString(),
                        petId, uri.toString(), System.currentTimeMillis())) }
                        .onFailure { error = "No se pudo guardar la foto." }
                }
            } catch (_: SecurityException) {
                error = "No se pudo conservar el permiso de la imagen."
            }
        }
    }
    Box(Modifier.fillMaxSize().background(PetPalette.Mist)) {
        Column(Modifier.fillMaxSize()) {
            PetHeader("🖼️ Mi galería", onBack, "Los mejores momentos de tu compañero.")
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("${photos.size} fotografías", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = { picker.launch(arrayOf("image/*")) }) {
                    Text("Añadir foto")
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
            if (photos.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                    PetRoundedCard {
                        Text("📷 Tu álbum está vacío",
                            style = MaterialTheme.typography.titleLarge)
                        Text("Añade una imagen de tu mascota. Se conservará una referencia al archivo original.")
                        Button(onClick = { picker.launch(arrayOf("image/*")) },
                            modifier = Modifier.fillMaxWidth()) { Text("Elegir mi primera foto") }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 145.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        ElevatedCard(shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                            Column(Modifier.padding(8.dp)) {
                                AndroidView(
                                    factory = { ctx ->
                                        ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                        }
                                    },
                                    update = { img ->
                                        runCatching { img.setImageURI(Uri.parse(photo.uri)) }
                                            .onFailure { img.setImageDrawable(null) }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(145.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Text(java.text.DateFormat.getDateInstance().format(
                                    java.util.Date(photo.createdAt)), fontSize = 11.sp,
                                    color = PetPalette.Muted)
                                Row(Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(Uri.parse(photo.uri), "image/*")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        runCatching { context.startActivity(intent) }
                                            .onFailure { error = "No se puede abrir la imagen." }
                                    }, modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 2.dp)) {
                                        Text("Ver", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = { pendingDelete = photo.id },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 2.dp)) {
                                        Text("Quitar", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { picker.launch(arrayOf("image/*")) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = PetPalette.Green, contentColor = Color.White
        ) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("Quitar foto") },
        text = { Text("Solo se quita del álbum. El archivo original no se borra.") },
        confirmButton = { Button(onClick = {
            pendingDelete?.let { id ->
                scope.launch {
                    runCatching { dao.deletePhoto(id) }
                        .onFailure { error = "No se pudo quitar la foto." }
                }
            }
            pendingDelete = null
        }) { Text("Quitar") } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) {
            Text("Cancelar")
        } }
    )
}
