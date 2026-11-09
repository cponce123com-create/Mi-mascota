package pe.mimascota.app.ui

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * First-launch welcome matching the reference layout.
 * Real pet photos are used when the owner already has an accessible picture;
 * the fallback is an illustration, never a fictitious pet account.
 */
@Composable
fun WelcomeScreen(
    photoUri: String?,
    onGoogle: () -> Unit,
    onEmail: () -> Unit,
    onGuest: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(PetPalette.DeepTeal).safeDrawingPadding()) {
        Box(Modifier.fillMaxWidth().weight(1f).background(
            Brush.verticalGradient(listOf(Color(0xFF102B31), Color(0xFF28684B),
                Color(0xFF134C50))))) {
            Canvas(Modifier.fillMaxSize()) {
                val light = Color.White.copy(alpha = 0.075f)
                repeat(12) { i ->
                    val x = size.width * (((i * 37) % 97) / 100f)
                    val y = size.height * (((i * 23 + 11) % 93) / 100f)
                    drawCircle(light, (10 + i % 4 * 6).dp.toPx(), Offset(x, y))
                }
            }
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.fillMaxWidth().padding(top = 15.dp, start = 12.dp, end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(68.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center) {
                        Text("🐾", fontSize = 47.sp)
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("Mi Mascota", fontSize = 34.sp, lineHeight = 39.sp,
                        fontWeight = FontWeight.ExtraBold, color = Color.White,
                        textAlign = TextAlign.Center)
                    Text("Salud · Cuidado · Comunidad", color = Color.White,
                        fontSize = 13.sp, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center) {
                    if (!photoUri.isNullOrBlank()) {
                        AndroidView(factory = { context ->
                            ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
                        }, update = { image -> runCatching { image.setImageURI(Uri.parse(photoUri)) }
                            .onFailure { image.setImageDrawable(null) } },
                            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight()
                                .clip(RoundedCornerShape(28.dp)))
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("🐶", fontSize = 105.sp)
                            Text("🐱", fontSize = 90.sp)
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(Brush.verticalGradient(
            listOf(Color(0xFF00A8B1), Color(0xFF0871B0), Color(0xFF063D80))))
            .padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onGoogle, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White,
                    contentColor = Color(0xFF18303B)), shape = RoundedCornerShape(28.dp)) {
                Text("🌐  Continuar con Google", fontWeight = FontWeight.Bold)
            }
            Button(onClick = onEmail, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PetPalette.Green),
                shape = RoundedCornerShape(28.dp)) {
                Text("✉️  Iniciar con correo", color = Color.White, fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(onClick = onGuest, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(28.dp)) {
                Text("Usar sin cuenta", fontWeight = FontWeight.SemiBold)
            }
            Text("🔒 Tus datos, seguros y privados", fontSize = 11.sp,
                color = Color.White, textAlign = TextAlign.Center)
        }
    }
}
