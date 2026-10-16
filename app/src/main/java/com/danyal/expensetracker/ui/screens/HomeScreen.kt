package com.danyal.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danyal.expensetracker.ui.components.EmptyState
import com.danyal.expensetracker.ui.components.GradientHeader
import com.danyal.expensetracker.ui.components.MonthSwitcher
import com.danyal.expensetracker.ui.components.SectionTitle
import com.danyal.expensetracker.ui.components.TransactionRow
import com.danyal.expensetracker.ui.components.budgetColor
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.money
import com.danyal.expensetracker.viewmodel.BudgetStatus
import com.danyal.expensetracker.viewmodel.MainViewModel
import java.time.YearMonth

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onEdit: (Long) -> Unit,
    onSeeAll: () -> Unit,
    onOpenBudgets: () -> Unit
) {
    val month by vm.month.collectAsStateWithLifecycle()
    val summary by vm.summary.collectAsStateWithLifecycle()
    val totalBalance by vm.totalBalance.collectAsStateWithLifecycle()
    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val statuses by vm.budgetStatuses.collectAsStateWithLifecycle()
    val currency = LocalCurrency.current

    val alerts = statuses.filter { it.ratio >= 0.8f }
    val recent = transactions.take(6)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ----- gradient header with the balance card -----
        item {
            GradientHeader {
                Text(
                    "Welcome back",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Your finances",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(4.dp))
                MonthSwitcher(
                    month = month,
                    canGoNext = month < YearMonth.now(),
                    onPrevious = vm::previousMonth,
                    onNext = vm::nextMonth
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(18.dp)
                ) {
                    Text("Total balance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        totalBalance.money(currency),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth()) {
                        SummaryPill("Income", summary.income.money(currency), Icons.Rounded.ArrowDownward)
                        Spacer(Modifier.width(12.dp))
                        SummaryPill("Expenses", summary.expense.money(currency), Icons.Rounded.ArrowUpward)
                    }
                }
            }
        }

        // ----- budget alerts -----
        if (alerts.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    SectionTitle("Budget alerts", actionText = "Budgets", onAction = onOpenBudgets)
                }
            }
            items(alerts, key = { "alert_" + it.budget.categoryId }) { status ->
                AlertCard(status, Modifier.padding(horizontal = 20.dp), onClick = onOpenBudgets)
            }
        }

        // ----- recent transactions -----
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                SectionTitle("Recent transactions", actionText = if (recent.isNotEmpty()) "See all" else null, onAction = onSeeAll)
            }
        }
        if (recent.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "No transactions yet",
                    message = "Tap the Add button to record your first income or expense."
                )
            }
        } else {
            items(recent, key = { it.id }) { tx ->
                TransactionRow(
                    tx = tx,
                    showDate = true,
                    onClick = { onEdit(tx.id) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

/** Small "Income / Expenses" block shown inside the balance card. */
@Composable
private fun RowScope.SummaryPill(label: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
            Text(amount, color = Color.White, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun AlertCard(status: BudgetStatus, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val currency = LocalCurrency.current
    val color = budgetColor(status.ratio)
    val over = status.ratio >= 1f
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Warning, contentDescription = null, tint = color)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (over) "${status.label} exceeded" else "${status.label} almost used",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${status.spent.money(currency)} of ${status.budget.limitAmount.money(currency)} (${(status.ratio * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
