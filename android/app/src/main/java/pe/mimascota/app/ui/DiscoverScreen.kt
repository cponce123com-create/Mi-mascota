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

private val Backdrop = Color(0xFFF0F8FA)
private val TealText = Color(0xFF006574)

@Composable
fun DiscoverScreen(
    kind: String,
    onBack: () -> Unit,
    onCommunity: () -> Unit,
    onVets: () -> Unit
) {
    val adoption = kind == "adoption"
    var selected by remember { mutableIntStateOf(0) }
    val heading = if (adoption) "Adopción responsable" else "Mascotas perdidas"
    val subtitle = if (adoption) "Un hogar cambia una vida 🐾" else "Ayudemos a que vuelvan a casa 💚"
    Column(Modifier.fillMaxSize().background(Backdrop)) {
        Column(Modifier.fillMaxWidth().background(TealText).padding(18.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text(if (adoption) "🏡 $heading" else "📢 $heading",
                color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color.White)
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selected == 0, onClick = { selected = 0 },
                        label = { Text(if (adoption) "En adopción" else "Buscados") })
                    FilterChip(selected = selected == 1, onClick = { selected = 1 },
                        label = { Text(if (adoption) "Mis solicitudes" else "Encontrados") })
                }
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (adoption) "🐶 Encuentra a tu compañero" else "🔎 Ayuda a encontrar una mascota",
                            style = MaterialTheme.typography.titleLarge, color = TealText)
                        Text(if (adoption)
                            "Próximamente podrás consultar fichas verificadas, conocer requisitos y enviar solicitudes de adopción."
                            else "Consulta las alertas de la comunidad. El registro de nuevas alertas desde Android está en preparación.")
                        Button(onClick = onCommunity, modifier = Modifier.fillMaxWidth()) {
                            Text(if (adoption) "Ver publicaciones de la comunidad" else "Ver alertas publicadas")
                        }
                    }
                }
            }
            item {
                ElevatedCard(shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📋 ${if (adoption) "Antes de adoptar" else "Qué hacer si se pierde"}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (adoption)
                            "Verifica las condiciones del refugio, considera los cuidados veterinarios y prepara un espacio seguro."
                            else "Comparte una fotografía reciente, ubicación aproximada y un contacto seguro. Evita publicar tu dirección exacta.")
                        OutlinedButton(onClick = onVets) { Text("📍 Buscar veterinarias") }
                    }
                }
            }
            item {
                Text("La publicación y gestión de solicitudes todavía no están disponibles. No se muestran animales ficticios.",
                    style = MaterialTheme.typography.bodySmall, color = TealText)
            }
        }
    }
}
