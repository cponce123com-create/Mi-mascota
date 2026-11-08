package pe.mimascota.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun VetMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Veterinarias cercanas", style = MaterialTheme.typography.headlineSmall)
        Text("Busca veterinarias en la aplicación de mapas que prefieras. Mi Mascota no recibe tu ubicación.")
        Button(onClick = {
            val uri = Uri.parse("geo:0,0?q=" + Uri.encode("veterinarias cerca de mí"))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            runCatching { context.startActivity(intent) }
        }) { Text("Buscar veterinarias en mapas") }
    }
}
