package com.danyal.expensetracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danyal.expensetracker.model.Categories
import com.danyal.expensetracker.ui.components.AppCard
import com.danyal.expensetracker.ui.components.EmptyState
import com.danyal.expensetracker.ui.components.GradientButton
import com.danyal.expensetracker.ui.components.GradientHeader
import com.danyal.expensetracker.ui.components.MonthSwitcher
import com.danyal.expensetracker.ui.components.budgetColor
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.money
import com.danyal.expensetracker.viewmodel.BudgetStatus
import com.danyal.expensetracker.viewmodel.MainViewModel
import java.time.YearMonth

private val amountRegex = Regex("^\\d{0,9}([.]\\d{0,2})?$")

@Composable
fun BudgetsScreen(vm: MainViewModel) {
    val month by vm.month.collectAsStateWithLifecycle()
    val statuses by vm.budgetStatuses.collectAsStateWithLifecycle()

    var editing by remember { mutableStateOf<BudgetStatus?>(null) }
    var showAdd by remember { mutableStateOf(false) }

    // Categories that do not have a budget yet.
    val usedIds = statuses.map { it.budget.categoryId }.toSet()
    val available: List<Pair<String, String>> =
        (listOf(Categories.ALL_BUDGET to "Overall budget") + Categories.expense.map { it.id to it.name })
            .filter { it.first !in usedIds }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader {
            Text("Budgets", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(
                "Set monthly limits and we will warn you before you overspend.",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            MonthSwitcher(
                month = month,
                canGoNext = month < YearMonth.now(),
                onPrevious = vm::previousMonth,
                onNext = vm::nextMonth
            )
        }

        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GradientButton(
                text = if (available.isEmpty()) "All budgets are set" else "Add budget",
                onClick = { showAdd = true },
                enabled = available.isNotEmpty()
            )

            if (statuses.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.Savings,
                    title = "No budgets yet",
                    message = "Create an overall budget or one per category, like Food or Transport."
                )
            } else {
                statuses.forEach { status ->
                    BudgetCard(status, onClick = { editing = status })
                }
            }
        }
    }

    if (showAdd) {
        BudgetDialog(
            existing = null,
            available = available,
            onSave = { id, limit ->
                vm.saveBudget(id, limit)
                showAdd = false
            },
            onDelete = {},
            onDismiss = { showAdd = false }
        )
    }

    editing?.let { status ->
        BudgetDialog(
            existing = status,
            available = emptyList(),
            onSave = { id, limit ->
                vm.saveBudget(id, limit)
                editing = null
            },
            onDelete = { id ->
                vm.deleteBudget(id)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun BudgetCard(status: BudgetStatus, onClick: () -> Unit) {
    val currency = LocalCurrency.current
    val color = budgetColor(status.ratio)
    val iconColor = status.category?.color ?: Brand.Indigo500
    val remaining = status.budget.limitAmount - status.spent

    AppCard(Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    status.category?.icon ?: Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = iconColor
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(status.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${status.spent.money(currency, decimals = false)} of ${status.budget.limitAmount.money(currency, decimals = false)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${(status.ratio * 100).toInt()}%",
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { status.ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.18f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (remaining >= 0) "${remaining.money(currency, decimals = false)} left this month"
            else "Over budget by ${(-remaining).money(currency, decimals = false)}",
            style = MaterialTheme.typography.bodySmall,
            color = if (remaining >= 0) MaterialTheme.colorScheme.onSurfaceVariant else Brand.Danger
        )
    }
}

/** Dialog used both to create a budget (existing == null) and to edit or delete one. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetDialog(
    existing: BudgetStatus?,
    available: List<Pair<String, String>>,
    onSave: (String, Double) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currency = LocalCurrency.current
    var selected by remember { mutableStateOf(existing?.budget?.categoryId ?: available.firstOrNull()?.first ?: "") }
    var limit by remember {
        mutableStateOf(
            existing?.budget?.limitAmount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: ""
        )
    }
    val limitValue = limit.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New budget" else "Edit budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existing == null) {
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        available.forEach { (id, name) ->
                            FilterChip(
                                selected = selected == id,
                                onClick = { selected = id },
                                label = { Text(name) }
                            )
                        }
                    }
                } else {
                    Text(existing.label, style = MaterialTheme.typography.titleMedium)
                }
                OutlinedTextField(
                    value = limit,
                    onValueChange = { if (amountRegex.matches(it)) limit = it },
                    label = { Text("Monthly limit") },
                    prefix = { Text("$currency ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = limitValue > 0 && selected.isNotEmpty(),
                onClick = { onSave(selected, limitValue) }
            ) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (existing != null) {
                    TextButton(onClick = { onDelete(existing.budget.categoryId) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
