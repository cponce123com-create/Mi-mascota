package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Practical, offline guidance; not a replacement for veterinary advice. */
@Composable
fun CareTipsScreen(onBack: () -> Unit, onHealth: () -> Unit, onVets: () -> Unit) {
    var species by remember { mutableStateOf("DOG") }
    Column(Modifier.fillMaxSize().background(PetPalette.Mist)) {
        PetHeader("🐾 Consejos de cuidado", onBack,
            subtitle = "Pequeños hábitos que mejoran su bienestar.")
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                PetRoundedCard {
                    Text("¿A quién cuidamos?", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = species == "DOG", onClick = { species = "DOG" },
                            label = { Text("🐶 Perros") })
                        FilterChip(selected = species == "CAT", onClick = { species = "CAT" },
                            label = { Text("🐱 Gatos") })
                    }
                }
            }
            item {
                PetRoundedCard {
                    Text("💧 Agua y alimentación", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("Agua limpia disponible y alimentos adecuados a la edad, especie y condición. " +
                         "Ante cambios bruscos en el apetito, consulta con un profesional.")
                }
            }
            item {
                PetRoundedCard {
                    Text("🏠 Un entorno seguro", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(if (species == "CAT")
                        "Ofrece escondites, rascadores, juego diario y protección en balcones y ventanas."
                        else "Procura ejercicio adecuado, paseos seguros, identificación y un lugar cómodo para descansar.")
                }
            }
            item {
                PetRoundedCard {
                    Text("🧼 Higiene y prevención", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("Limpia sus espacios y mantén las vacunas, antiparasitarios y controles " +
                        "según el plan individual que indique su veterinario.")
                    Button(onClick = onHealth, modifier = Modifier.fillMaxWidth()) {
                        Text("💉 Abrir historial y recordatorios")
                    }
                }
            }
            item {
                PetRoundedCard {
                    Text("🩺 Cuándo acudir al veterinario", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text("Si tiene dificultad para respirar, convulsiones, dolor intenso o cambios " +
                        "graves de comportamiento, busca atención veterinaria cuanto antes.")
                    OutlinedButton(onClick = onVets, modifier = Modifier.fillMaxWidth()) {
                        Text("📍 Buscar una veterinaria")
                    }
                }
            }
            item {
                Text("Información general; no es un diagnóstico ni reemplaza una consulta veterinaria.",
                    color = PetPalette.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
