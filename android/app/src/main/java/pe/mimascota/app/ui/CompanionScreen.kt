package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign

/** Lightweight offline virtual companion. No medical claims or paid currency. */
@Composable
fun CompanionScreen(onBack: () -> Unit) {
    var energy by rememberSaveable { mutableIntStateOf(70) }
    var happiness by rememberSaveable { mutableIntStateOf(70) }
    var taps by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Mi compañero virtual", style = MaterialTheme.typography.headlineSmall)
        Text(if (happiness > 40) "🐶 🐾" else "🐶 💤", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = MaterialTheme.typography.displayLarge)
        Text("Energía: $energy/100")
        LinearProgressIndicator(progress = { energy / 100f }, modifier = Modifier.fillMaxWidth())
        Text("Alegría: $happiness/100")
        LinearProgressIndicator(progress = { happiness / 100f }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { energy = (energy + 20).coerceAtMost(100); happiness = (happiness + 5).coerceAtMost(100) }) { Text("Descansar") }
            Button(onClick = { if (energy >= 10) { energy -= 10; happiness = (happiness + 15).coerceAtMost(100); taps++ } }, enabled = energy >= 10) { Text("Jugar") }
        }
        Text("Minijuego: jugaste $taps veces. ¡Cuida su energía!")
        Text("Compañero recreativo; no reemplaza los cuidados de una mascota real.", style = MaterialTheme.typography.bodySmall)
    }
}
