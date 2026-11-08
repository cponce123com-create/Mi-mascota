package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CommunityScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Comunidad Mascotera", style = MaterialTheme.typography.headlineSmall)
        Text("Mascotas perdidas y encontradas")
        Text("Campañas de vacunación y esterilización")
        Text("Adopciones responsables")
        Text("Veterinarias cercanas")
        Text("Estas secciones requieren conexión, moderación y servicios externos. No se publican datos sin consentimiento.")
    }
}
