package com.danyal.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.data.isExpense
import com.danyal.expensetracker.data.isIncome
import com.danyal.expensetracker.model.Categories
import com.danyal.expensetracker.ui.components.EmptyState
import com.danyal.expensetracker.ui.components.GradientHeader
import com.danyal.expensetracker.ui.components.PillToggle
import com.danyal.expensetracker.ui.components.TransactionRow
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.money
import com.danyal.expensetracker.util.pretty
import com.danyal.expensetracker.viewmodel.MainViewModel
import java.time.LocalDate

@Composable
fun HistoryScreen(
    vm: MainViewModel,
    onEdit: (Long) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val all by vm.transactions.collectAsStateWithLifecycle()
    val currency = LocalCurrency.current

    var query by remember { mutableStateOf("") }
    var typeIndex by remember { mutableIntStateOf(0) } // 0 all, 1 income, 2 expenses
    var categoryId by remember { mutableStateOf<String?>(null) }

    val filtered = remember(all, query, typeIndex, categoryId) {
        all.filter { tx ->
            val typeOk = typeIndex == 0 || (typeIndex == 1 && tx.isIncome) || (typeIndex == 2 && tx.isExpense)
            val categoryOk = categoryId == null || tx.categoryId == categoryId
            val queryOk = query.isBlank() ||
                tx.title.contains(query, ignoreCase = true) ||
                tx.note.contains(query, ignoreCase = true) ||
                Categories.byId(tx.categoryId).name.contains(query, ignoreCase = true)
            typeOk && categoryOk && queryOk
        }
    }
    val grouped = remember(filtered) { filtered.groupBy { it.dateEpochDay }.toList() }

    val chipCategories = when (typeIndex) {
        1 -> Categories.income
        2 -> Categories.expense
        else -> Categories.all
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GradientHeader {
                Text("Transactions", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                SearchField(query = query, onChange = { query = it })
                Spacer(Modifier.height(12.dp))
                PillToggle(
                    options = listOf("All", "Income", "Expenses"),
                    selected = typeIndex,
                    onSelect = { typeIndex = it; categoryId = null },
                    onGradient = true
                )
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = categoryId == null,
                        onClick = { categoryId = null },
                        label = { Text("All categories") }
                    )
                }
                items(chipCategories, key = { it.id }) { cat ->
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = { categoryId = if (categoryId == cat.id) null else cat.id },
                        label = { Text(cat.name) },
                        leadingIcon = {
                            Icon(cat.icon, contentDescription = null, tint = cat.color)
                        }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.ReceiptLong,
                    title = if (all.isEmpty()) "Nothing here yet" else "No matches",
                    message = if (all.isEmpty()) "Your transactions will show up here."
                    else "Try a different search or filter."
                )
            }
        }

        grouped.forEach { (epochDay, dayList) ->
            item(key = "header_$epochDay") {
                val net = dayList.sumOf { if (it.isIncome) it.amount else -it.amount }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        LocalDate.ofEpochDay(epochDay).pretty(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        (if (net >= 0) "+ " else "- ") + kotlin.math.abs(net).money(currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(dayList, key = { it.id }) { tx ->
                SwipeToDeleteRow(
                    tx = tx,
                    onClick = { onEdit(tx.id) },
                    onDelete = { onDelete(tx) }
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search title, note or category") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.16f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.16f),
            focusedBorderColor = Color.White.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.Transparent,
            cursorColor = Color.White,
            focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.White,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.White
        )
    )
}

/** Swipe a transaction to the left to delete it (the app offers Undo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteRow(tx: TransactionEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = state,
        modifier = Modifier.padding(horizontal = 20.dp),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brand.Danger),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 22.dp)
                )
            }
        }
    ) {
        TransactionRow(tx = tx, onClick = onClick)
    }
}
