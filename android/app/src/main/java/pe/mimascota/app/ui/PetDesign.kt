package pe.mimascota.app.ui

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/** Shared design system based on the approved Mi Mascota mockup. */
object PetPalette {
    val DeepTeal = Color(0xFF004D5A)
    val Ocean = Color(0xFF006B79)
    val Teal = Color(0xFF009D87)
    val Green = Color(0xFF07A447)
    val Mist = Color(0xFFEAF8FA)
    val Mint = Color(0xFFE7F7ED)
    val Ink = Color(0xFF18313B)
    val Muted = Color(0xFF5B7079)
    val Blue = Color(0xFF087BD8)
    val Violet = Color(0xFF7544C6)
    val Rose = Color(0xFFE93272)
    val Orange = Color(0xFFDF880A)
}

@Composable
fun PetHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    endAction: (@Composable () -> Unit)? = null
) {
    Box(
        Modifier.fillMaxWidth().background(
            Brush.linearGradient(
                listOf(PetPalette.DeepTeal, PetPalette.Ocean, Color(0xFF008D8C))
            )
        )
    ) {
        Canvas(Modifier.matchParentSize()) {
            val faded = Color.White.copy(alpha = 0.10f)
            val cx = size.width * 0.89f
            val cy = size.height * 0.55f
            drawOval(faded, topLeft = Offset(cx - 29.dp.toPx(), cy),
                size = Size(60.dp.toPx(), 42.dp.toPx()))
            drawCircle(faded, 11.dp.toPx(), Offset(cx - 27.dp.toPx(), cy - 15.dp.toPx()))
            drawCircle(faded, 11.dp.toPx(), Offset(cx - 10.dp.toPx(), cy - 31.dp.toPx()))
            drawCircle(faded, 11.dp.toPx(), Offset(cx + 12.dp.toPx(), cy - 30.dp.toPx()))
            drawCircle(faded, 11.dp.toPx(), Offset(cx + 31.dp.toPx(), cy - 14.dp.toPx()))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                        Text("‹", color = Color.White, fontSize = 36.sp, lineHeight = 36.sp)
                        Spacer(Modifier.width(7.dp))
                    }
                }
                Text(title, modifier = Modifier.weight(1f, fill = false), color = Color.White,
                    fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                endAction?.invoke()
            }
            if (subtitle != null) {
                Text(subtitle, color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp, lineHeight = 19.sp, maxLines = 3)
            }
        }
    }
}

@Composable
fun PetAvatar(uri: String?, species: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(Color(0xFFDDEFF2)),
        contentAlignment = Alignment.Center) {
        if (uri.isNullOrBlank()) {
            Text(when (species) { "DOG" -> "🐶"; "CAT" -> "🐱"; else -> "🐾" },
                style = MaterialTheme.typography.headlineLarge)
        } else {
            AndroidView(
                factory = { context -> ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                } },
                update = { image ->
                    runCatching { image.setImageURI(Uri.parse(uri)) }
                        .onFailure { image.setImageDrawable(null) }
                },
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}

@Composable
fun PetActionTile(
    emoji: String,
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.heightIn(min = 90.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.fillMaxWidth().heightIn(min = 90.dp).padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp, lineHeight = 28.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(title, textAlign = TextAlign.Center, color = PetPalette.Ink,
                fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun PetInfoRow(label: String, value: String, emoji: String) {
    Row(Modifier.fillMaxWidth().heightIn(min = 45.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(PetPalette.Mist),
            contentAlignment = Alignment.Center) { Text(emoji, fontSize = 15.sp) }
        Text(label, Modifier.weight(1f), fontSize = 13.sp,
            lineHeight = 17.sp, color = PetPalette.Ink, maxLines = 2)
        Text(value, Modifier.weight(0.9f), fontSize = 12.sp,
            lineHeight = 16.sp, fontWeight = FontWeight.SemiBold,
            color = PetPalette.Ink, textAlign = TextAlign.End,
            maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PetRoundedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
