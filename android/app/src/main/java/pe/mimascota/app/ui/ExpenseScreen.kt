package pe.mimascota.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import pe.mimascota.app.data.ExpenseDao
import pe.mimascota.app.data.ExpenseEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.UUID

@Composable
fun ExpenseScreen(petId: String, dao: ExpenseDao, onBack: () -> Unit) {
    val expenses by remember(petId) { dao.observe(petId) }.collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val total = expenses.fold(0L) { acc, item -> acc + item.amountCents }
    Box(Modifier.fillMaxSize().background(PetPalette.Mist)) {
        Column(Modifier.fillMaxSize()) {
            PetHeader("🪙 Gastos", onBack,
                subtitle = "Lleva un registro claro de los cuidados de tu mascota.")
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp,
                    end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PetRoundedCard {
                        Text("💰 Total registrado", color = PetPalette.Muted)
                        Text("S/ ${BigDecimal.valueOf(total, 2).toPlainString()}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold, color = PetPalette.Ocean)
                        Text("${expenses.size} movimientos", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("➕ Registrar un gasto")
                        }
                    }
                }
                item {
                    Text("Historial de gastos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                if (expenses.isEmpty()) item {
                    PetRoundedCard {
                        Text("No hay gastos registrados.")
                        Text("Añade una compra, consulta veterinaria o medicamento.",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                items(expenses, key = { it.id }) { item ->
                    ElevatedCard(shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold, maxLines = 2)
                                Text(item.date, color = PetPalette.Muted,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("S/ ${BigDecimal.valueOf(item.amountCents, 2).toPlainString()}",
                                fontWeight = FontWeight.Bold, color = PetPalette.Ocean,
                                style = MaterialTheme.typography.labelMedium)
                            IconButton(onClick = { pendingDelete = item.id }) { Text("✕") }
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = PetPalette.Green, contentColor = Color.White) {
            Text("+", style = MaterialTheme.typography.headlineMedium)
        }
    }
    if (showAdd) AlertDialog(
        onDismissRequest = { if (!busy) showAdd = false },
        title = { Text("Registrar gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it.take(160) },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Concepto") })
                OutlinedTextField(amount, { amount = it },
                    modifier = Modifier.fillMaxWidth(), label = { Text("Soles (ej. 25.50)") })
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(enabled = !busy && title.isNotBlank(), onClick = {
                val cents = runCatching {
                    BigDecimal(amount.trim()).setScale(2, RoundingMode.UNNECESSARY)
                        .movePointRight(2).longValueExact()
                }.getOrNull()
                if (title.isBlank() || cents == null || cents <= 0) {
                    error = "Ingresa un importe positivo con máximo dos decimales."
                } else {
                    busy = true
                    scope.launch {
                        runCatching { dao.insert(ExpenseEntity(UUID.randomUUID().toString(),
                            petId, title.trim(), cents, LocalDate.now().toString())) }
                            .onSuccess {
                                title = ""; amount = ""; error = null; showAdd = false
                            }.onFailure { error = "No se pudo registrar el gasto." }
                        busy = false
                    }
                }
            }) { Text(if (busy) "Guardando..." else "Guardar") }
        },
        dismissButton = { TextButton(enabled = !busy, onClick = { showAdd = false }) {
            Text("Cancelar")
        } }
    )
    if (pendingDelete != null) AlertDialog(
        onDismissRequest = { pendingDelete = null },
        title = { Text("Eliminar gasto") },
        text = { Text("Este registro de gasto se quitará del historial.") },
        confirmButton = { Button(onClick = {
            pendingDelete?.let { id ->
                scope.launch {
                    runCatching { dao.delete(id) }.onFailure {
                        error = "No se pudo eliminar el gasto."
                    }
                }
            }
            pendingDelete = null
        }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { pendingDelete = null }) {
            Text("Cancelar")
        } }
    )
}
