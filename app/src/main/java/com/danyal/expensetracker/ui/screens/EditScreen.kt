package com.danyal.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.model.Categories
import com.danyal.expensetracker.model.Category
import com.danyal.expensetracker.model.TxType
import com.danyal.expensetracker.ui.components.GradientButton
import com.danyal.expensetracker.ui.components.GradientHeader
import com.danyal.expensetracker.ui.components.PillToggle
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.full
import com.danyal.expensetracker.viewmodel.MainViewModel
import java.time.LocalDate

private val amountRegex = Regex("^\\d{0,9}([.]\\d{0,2})?$")
private const val DAY_MILLIS = 86_400_000L

/**
 * Screen used both to add a new transaction ([transactionId] == 0)
 * and to edit or delete an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    vm: MainViewModel,
    transactionId: Long,
    onClose: () -> Unit,
    onDeleted: (TransactionEntity) -> Unit
) {
    val currency = LocalCurrency.current

    var existing by remember { mutableStateOf<TransactionEntity?>(null) }
    var type by remember { mutableStateOf(TxType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(Categories.expense.first().id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var showPicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    // When editing, load the saved transaction once.
    LaunchedEffect(transactionId) {
        if (transactionId > 0) {
            vm.getTransaction(transactionId)?.let { tx ->
                existing = tx
                type = TxType.valueOf(tx.type)
                amountText = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString()
                title = tx.title
                categoryId = tx.categoryId
                date = LocalDate.ofEpochDay(tx.dateEpochDay)
                note = tx.note
            }
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val categories: List<Category> = Categories.forType(type)

    Column(Modifier.fillMaxSize()) {
        // ----- header: back button, type toggle and the big amount field -----
        GradientHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    if (existing == null) "Add transaction" else "Edit transaction",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(Modifier.height(8.dp))
            PillToggle(
                options = listOf("Expense", "Income"),
                selected = if (type == TxType.EXPENSE) 0 else 1,
                onSelect = { index ->
                    val newType = if (index == 0) TxType.EXPENSE else TxType.INCOME
                    if (newType != type) {
                        type = newType
                        categoryId = Categories.forType(newType).first().id
                    }
                },
                onGradient = true
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = amountText,
                onValueChange = { if (amountRegex.matches(it)) amountText = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White),
                prefix = {
                    Text(
                        "$currency ",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text("0.00", color = Color.White.copy(alpha = 0.5f), fontSize = 38.sp, fontWeight = FontWeight.Bold)
                },
                singleLine = true,
                isError = showError && amount <= 0,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.White.copy(alpha = 0.7f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                    errorIndicatorColor = Brand.Coral,
                    cursorColor = Color.White
                )
            )
            if (showError && amount <= 0) {
                Text("Please enter an amount greater than 0", color = Brand.Coral, style = MaterialTheme.typography.bodySmall)
            }
        }

        // ----- form -----
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                categories.forEach { cat ->
                    CategoryChip(cat, selected = cat.id == categoryId, onClick = { categoryId = cat.id })
                }
            }

            OutlinedButton(
                onClick = { showPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(date.full(), style = MaterialTheme.typography.titleSmall)
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            GradientButton(
                text = if (existing == null) "Save transaction" else "Save changes",
                onClick = {
                    if (amount <= 0) {
                        showError = true
                    } else {
                        val entity = TransactionEntity(
                            id = existing?.id ?: 0L,
                            title = title.trim(),
                            amount = amount,
                            type = type.name,
                            categoryId = categoryId,
                            dateEpochDay = date.toEpochDay(),
                            note = note.trim()
                        )
                        vm.save(entity) { onClose() }
                    }
                }
            )

            if (existing != null) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * DAY_MILLIS)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { date = LocalDate.ofEpochDay(it / DAY_MILLIS) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    existing?.let { onDeleted(it) }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .clip(shape)
            .background(if (selected) category.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) category.color else MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            category.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
