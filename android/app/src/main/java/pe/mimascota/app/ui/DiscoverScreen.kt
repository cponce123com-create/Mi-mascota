package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.mimascota.app.BuildConfig
import pe.mimascota.app.data.*

private val DiscoverBg = Color(0xFFF0F8FA)
private val DiscoverTeal = Color(0xFF006574)

@Composable
fun DiscoverScreen(
    kind: String,
    pets: List<PetEntity>,
    onBack: () -> Unit,
    onCommunity: () -> Unit,
    onVets: () -> Unit,
    onAccount: () -> Unit
) {
    val adoption = kind == "adoption"
    val configured = BuildConfig.API_BASE_URL.startsWith("https://")
    val api = remember { CommunityApi(BuildConfig.API_BASE_URL) }
    val scope = rememberCoroutineScope()
    var selected by remember { mutableIntStateOf(0) }
    var listings by remember { mutableStateOf<List<AdoptionListing>>(emptyList()) }
    var lost by remember { mutableStateOf<List<LostPetListing>>(emptyList()) }
    var owned by remember { mutableStateOf<List<OwnedListing>>(emptyList()) }
    var applications by remember { mutableStateOf<List<OwnedListing>>(emptyList()) }
    var requestList by remember { mutableStateOf<List<AdoptionRequest>>(emptyList()) }
    var reviewingId by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var applyTo by remember { mutableStateOf<AdoptionListing?>(null) }
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("DOG") }
    var district by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chosenPetId by remember { mutableStateOf<String?>(null) }
    var applicationMessage by remember { mutableStateOf("") }

    fun refresh() {
        if (!configured || loading) return
        loading = true
        scope.launch {
            runCatching {
                if (selected == 0) {
                    if (adoption) listings = api.adoptions() else lost = api.lostPets()
                } else {
                    val token = communityToken() ?: throw IllegalStateException("Inicia sesión desde Mi cuenta.")
                    if (adoption) {
                        val mine = api.myAdoptions(token)
                        owned = mine.first
                        applications = mine.second
                    } else owned = api.myLost(token)
                }
            }.onFailure { notice = it.message ?: "No se pudieron cargar los datos." }
                .onSuccess { notice = null }
            loading = false
        }
    }
    LaunchedEffect(kind, configured, selected) { if (configured) refresh() }

    Column(Modifier.fillMaxSize().background(DiscoverBg)) {
        Column(Modifier.fillMaxWidth().background(DiscoverTeal).padding(16.dp)) {
            TextButton(onClick = onBack) { Text("← Volver", color = Color.White) }
            Text(if (adoption) "🏡 Adopción responsable" else "📢 Mascotas perdidas",
                color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(if (adoption) "Un hogar cambia una vida 🐾" else "Ayudemos a que vuelvan a casa 💚",
                color = Color.White)
        }
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selected == 0, onClick = { selected = 0 },
                    label = { Text(if (adoption) "En adopción" else "Buscados") })
                FilterChip(selected = selected == 1, onClick = { selected = 1 },
                    label = { Text(if (adoption) "Mis solicitudes" else "Mis alertas") })
            }
            if (!configured) {
                Text("Aún no hay servidor HTTPS configurado para consultar o publicar. No se mostrarán datos ficticios.")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showCreate = true }, modifier = Modifier.weight(1f)) {
                        Text(if (adoption) "➕ Ofrecer adopción" else "➕ Reportar pérdida")
                    }
                    OutlinedButton(onClick = { refresh() }, enabled = !loading) { Text("↻") }
                }
                if (loading || sending) LinearProgressIndicator(Modifier.fillMaxWidth())
                notice?.let { Text(it, color = DiscoverTeal) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (selected == 0 && adoption) {
                        if (listings.isEmpty() && !loading) item { Text("No hay adopciones aprobadas publicadas todavía.") }
                        items(listings, key = { it.id }) { listing ->
                            ElevatedCard(shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("🐾 ${listing.name}", style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold)
                                    Text("${listing.species} · ${listing.district ?: "Distrito no indicado"}")
                                    Text(listing.description)
                                    Button(onClick = { applyTo = listing }) { Text("Solicitar adopción") }
                                }
                            }
                        }
                    } else if (selected == 0) {
                        if (lost.isEmpty() && !loading) item { Text("No hay reportes aprobados activos.") }
                        items(lost) { item ->
                            ElevatedCard(shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("🔎 ${item.name}", style = MaterialTheme.typography.titleLarge)
                                    Text("${item.species} · ${item.district ?: "Distrito no indicado"}")
                                }
                            }
                        }
                    } else {
                        if (owned.isEmpty() && applications.isEmpty() && !loading) item {
                            Text("No tienes registros o solicitudes disponibles.")
                        }
                        items(owned, key = { it.id }) { item ->
                            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("🐾 ${item.title}", style = MaterialTheme.typography.titleMedium)
                                    Text("Estado: ${item.status}")
                                    if (adoption) TextButton(enabled = !sending, onClick = {
                                        sending = true
                                        scope.launch {
                                            runCatching {
                                                val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                                                api.applicationsFor(item.id, token)
                                            }.onSuccess { requestList = it; reviewingId = item.id }
                                                .onFailure { notice = it.message }
                                            sending = false
                                        }
                                    }) { Text("Ver solicitudes recibidas") }
                                    if ((adoption && item.status != "CLOSED") || (!adoption && !item.status.startsWith("RESOLVED"))) {
                                        TextButton(onClick = {
                                            sending = true
                                            scope.launch {
                                                runCatching {
                                                    val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                                                    if (adoption) api.closeAdoption(item.id, token)
                                                    else api.resolveLost(item.id, token)
                                                }.onSuccess {
                                                    notice = if (adoption) "Publicación cerrada." else "Mascota marcada como encontrada."
                                                    refresh()
                                                }.onFailure { notice = it.message }
                                                sending = false
                                            }
                                        }) { Text(if (adoption) "Cerrar adopción" else "Marcar como encontrada") }
                                    }
                                }
                            }
                        }
                        if (adoption) items(applications, key = { it.id }) { item ->
                            ListItem(headlineContent = { Text("Solicitud: ${item.title}") },
                                supportingContent = { Text("Estado: ${item.status}") })
                        }
                    }
                }
            }
            OutlinedButton(onClick = onCommunity, modifier = Modifier.fillMaxWidth()) {
                Text("👥 Abrir comunidad")
            }
            TextButton(onClick = onVets) { Text("📍 Buscar veterinarias") }
        }
    }

    if (showCreate) AlertDialog(
        onDismissRequest = { if (!sending) showCreate = false },
        title = { Text(if (adoption) "🏡 Ofrecer en adopción" else "📢 Reportar pérdida") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (adoption) {
                    OutlinedTextField(petName, { petName = it }, label = { Text("Nombre") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = species == "DOG", onClick = { species = "DOG" }, label = { Text("Perro") })
                        FilterChip(selected = species == "CAT", onClick = { species = "CAT" }, label = { Text("Gato") })
                        FilterChip(selected = species == "OTHER", onClick = { species = "OTHER" }, label = { Text("Otro") })
                    }
                } else {
                    Text("Selecciona tu mascota registrada:")
                    pets.forEach { pet ->
                        FilterChip(selected = chosenPetId == pet.id, onClick = { chosenPetId = pet.id },
                            label = { Text(pet.name) })
                    }
                    if (pets.isEmpty()) Text("Primero registra una mascota en Inicio.")
                }
                OutlinedTextField(district, { district = it }, label = { Text("Distrito (sin dirección exacta)") })
                OutlinedTextField(description, { description = it }, minLines = 2,
                    label = { Text(if (adoption) "Descripción y requisitos (mín. 20 caracteres)" else "Detalles (opcional)") })
                Text("Se enviará para revisión antes de ser visible públicamente.",
                    style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(
            enabled = !sending && configured && district.trim().length in 2..100 &&
                (if (adoption) petName.trim().length in 1..80 && description.trim().length in 20..1200
                 else chosenPetId != null && description.length <= 1200),
            onClick = {
                sending = true
                scope.launch {
                    runCatching {
                        val token = communityToken() ?: throw IllegalStateException("Inicia sesión con Google desde Mi cuenta.")
                        if (adoption) api.submitAdoption(petName.trim(), species, description.trim(), district.trim(), token)
                        else {
                            val pet = pets.firstOrNull { it.id == chosenPetId } ?: throw IllegalStateException("Elige una mascota.")
                            api.submitLost(pet, district.trim(), description.trim(), token)
                        }
                    }.onSuccess {
                        showCreate = false
                        notice = "Solicitud enviada para revisión. No será pública hasta que se apruebe."
                        petName = ""; district = ""; description = ""; chosenPetId = null
                    }.onFailure { notice = it.message ?: "No se pudo enviar." }
                    sending = false
                }
            }
        ) { Text(if (sending) "Enviando..." else "Enviar a revisión") } },
        dismissButton = {
            TextButton(enabled = !sending, onClick = { showCreate = false; onAccount() }) { Text("Mi cuenta") }
            TextButton(enabled = !sending, onClick = { showCreate = false }) { Text("Cancelar") }
        }
    )

    applyTo?.let { listing ->
        AlertDialog(
            onDismissRequest = { if (!sending) applyTo = null },
            title = { Text("Solicitar adopción de ${listing.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tu solicitud será privada y revisada por quien publicó la adopción.")
                    OutlinedTextField(applicationMessage, { applicationMessage = it },
                        minLines = 3, label = { Text("Cuéntanos por qué deseas adoptar") })
                }
            },
            confirmButton = { Button(enabled = !sending && applicationMessage.trim().length in 20..1000,
                onClick = {
                    sending = true
                    scope.launch {
                        runCatching {
                            val token = communityToken() ?: throw IllegalStateException("Inicia sesión con Google.")
                            api.apply(listing.id, applicationMessage.trim(), token)
                        }.onSuccess {
                            notice = "Solicitud enviada. Puedes revisar su estado en Mis solicitudes."
                            applicationMessage = ""
                            applyTo = null
                        }.onFailure { notice = it.message }
                        sending = false
                    }
                }) { Text("Solicitar") } },
            dismissButton = { TextButton(enabled = !sending, onClick = { applyTo = null }) { Text("Cancelar") } }
        )
    }
    reviewingId?.let {
        AlertDialog(
            onDismissRequest = { if (!sending) reviewingId = null },
            title = { Text("Solicitudes recibidas") },
            text = {
                Column(Modifier.heightIn(max = 390.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (requestList.isEmpty()) Text("Aún no hay solicitudes para este anuncio.")
                    requestList.forEach { request ->
                        HorizontalDivider()
                        Text(request.message)
                        Text("Estado: ${request.status}")
                        if (request.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(enabled = !sending, onClick = {
                                    sending = true
                                    scope.launch {
                                        runCatching {
                                            val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                                            api.respondApplication(request.id, true, token)
                                        }.onSuccess {
                                            requestList = requestList.map { item ->
                                                if (item.id == request.id) item.copy(status = "ACCEPTED") else item
                                            }
                                        }.onFailure { notice = it.message }
                                        sending = false
                                    }
                                }) { Text("Aceptar") }
                                OutlinedButton(enabled = !sending, onClick = {
                                    sending = true
                                    scope.launch {
                                        runCatching {
                                            val token = communityToken() ?: throw IllegalStateException("Inicia sesión.")
                                            api.respondApplication(request.id, false, token)
                                        }.onSuccess {
                                            requestList = requestList.map { item ->
                                                if (item.id == request.id) item.copy(status = "REJECTED") else item
                                            }
                                        }.onFailure { notice = it.message }
                                        sending = false
                                    }
                                }) { Text("Rechazar") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(enabled = !sending, onClick = { reviewingId = null }) { Text("Cerrar") } }
        )
    }

}
