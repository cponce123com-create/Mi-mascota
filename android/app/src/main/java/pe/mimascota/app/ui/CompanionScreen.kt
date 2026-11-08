package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.mimascota.app.data.CompanionRepository
import pe.mimascota.app.data.CompanionStats
import kotlin.random.Random

/** Purely recreational, free and offline. Values persist across app restarts. */
@Composable
fun CompanionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by remember(context) { CompanionRepository.observe(context) }
        .collectAsStateWithLifecycle(initialValue = CompanionStats())
    var target by remember { mutableIntStateOf(Random.nextInt(3)) }
    var result by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun update(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            runCatching { action() }.onFailure { result = "No se pudo guardar la partida." }
            busy = false
        }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA))) {
        Column(Modifier.fillMaxWidth().background(Color(0xFF006574)).padding(18.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("🎮 Mi compañero virtual", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Diviértete sin conexión", color = Color.White)
        }
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (state.happiness > 40) "🐶 🐾" else "🐶 💤",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Text("Energía ${state.energy}/100")
                        LinearProgressIndicator(progress = { state.energy / 100f },
                            modifier = Modifier.fillMaxWidth())
                        Text("Alegría ${state.happiness}/100")
                        LinearProgressIndicator(progress = { state.happiness / 100f },
                            modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(enabled = !busy, onClick = { update { CompanionRepository.rest(context) } }) {
                                Text("💤 Descansar")
                            }
                            Button(enabled = !busy && state.energy >= 10,
                                onClick = { update { CompanionRepository.play(context) } }) {
                                Text("🎾 Jugar")
                            }
                        }
                    }
                }
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🐾 ¿Dónde está la huella?", style = MaterialTheme.typography.titleMedium)
                        Text("Elige una de las tres cajas. ¡Cada acierto suma puntos!")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { index ->
                                FilledTonalButton(enabled = !busy, modifier = Modifier.weight(1f),
                                    onClick = {
                                        val correct = index == target
                                        result = if (correct) "¡Encontraste la huella! +1 punto" else "Sigue intentando"
                                        target = Random.nextInt(3)
                                        update { CompanionRepository.guess(context, correct) }
                                    }) { Text("🎁") }
                            }
                        }
                        Text("Partidas: ${state.plays} · Aciertos: ${state.score}")
                        result?.let { Text(it) }
                    }
                }
            }
            item {
                Text("Las actividades son recreativas y no sustituyen los cuidados de una mascota real.",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
