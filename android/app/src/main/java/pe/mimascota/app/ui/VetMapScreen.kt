package pe.mimascota.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

@Composable
fun VetMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var position by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var message by remember { mutableStateOf("Mapa de referencia del Perú. Activa la ubicación aproximada para centrarlo cerca de ti.") }
    val requestLocation = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val result = runCatching {
                val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
                providers.firstNotNullOfOrNull { provider ->
                    runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
                }
            }.getOrNull()
            if (result != null) {
                position = result.latitude to result.longitude
                message = "Mapa centrado según la última ubicación aproximada disponible."
            } else message = "No hay una ubicación disponible. Puedes navegar en el mapa manualmente."
        } else message = "Permiso no concedido. Puedes usar el mapa sin compartir tu ubicación."
    }
    val url = position?.let { (lat, lon) -> "https://www.openstreetmap.org/#map=14/$lat/$lon" }
        ?: "https://www.openstreetmap.org/#map=5/-9.19/-75.02"
    val webView = remember(context) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            webViewClient = WebViewClient()
        }
    }
    LaunchedEffect(url) { webView.loadUrl(url) }
    DisposableEffect(webView) { onDispose { webView.stopLoading(); webView.destroy() } }
    Column(Modifier.fillMaxSize().background(Color(0xFFF0F8FA))) {
        Column(Modifier.fillMaxWidth().background(Color(0xFF006574)).padding(14.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text("📍 Veterinarias y mapa", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        Text(message, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                requestLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }, modifier = Modifier.weight(1f)) { Text("📍 Ubicación aprox.") }
            Button(onClick = {
                val uri = position?.let { (lat, lon) ->
                    Uri.parse("geo:$lat,$lon?q=" + Uri.encode("veterinarias"))
                } ?: Uri.parse("geo:0,0?q=" + Uri.encode("veterinarias cercanas"))
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    .onFailure { message = "No se encontró una aplicación de mapas compatible." }
            }, modifier = Modifier.weight(1f)) { Text("🔎 Buscar") }
        }
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxWidth().weight(1f))
        Text("Mapa: OpenStreetMap. La búsqueda de clínicas se realiza con tu app de mapas; todavía no hay un directorio veterinario verificado.",
            modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}
