package pe.mimascota.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    val expenses by dao.observe(petId).collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val total = expenses.fold(0L) { acc, item -> acc + item.amountCents }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        Text("Gastos de mi mascota", style = MaterialTheme.typography.headlineSmall)
        Text("Total: S/ ${BigDecimal.valueOf(total, 2).toPlainString()}")
        OutlinedTextField(title, { title = it }, label = { Text("Concepto") })
        OutlinedTextField(amount, { amount = it }, label = { Text("Importe en soles (ej. 25.50)") })
        Button(onClick = {
            val cents = runCatching {
                BigDecimal(amount.trim()).setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact()
            }.getOrNull()
            if (title.isBlank() || cents == null || cents <= 0) error = "Ingresa concepto e importe positivo con máximo dos decimales"
            else {
                error = null
                scope.launch {
                    dao.insert(ExpenseEntity(UUID.randomUUID().toString(), petId, title.trim(), cents, LocalDate.now().toString()))
                    title = ""; amount = ""
                }
            }
        }) { Text("Registrar gasto") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LazyColumn {
            items(expenses, key = { it.id }) { item ->
                ListItem(headlineContent = { Text(item.title) },
                    supportingContent = { Text("${item.date} · S/ ${BigDecimal.valueOf(item.amountCents, 2).toPlainString()}") },
                    trailingContent = { TextButton(onClick = { scope.launch { dao.delete(item.id) } }) { Text("Eliminar") } })
            }
        }
    }
}
