package com.danyal.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danyal.expensetracker.data.isExpense
import com.danyal.expensetracker.ui.components.AppCard
import com.danyal.expensetracker.ui.components.DonutChart
import com.danyal.expensetracker.ui.components.EmptyState
import com.danyal.expensetracker.ui.components.GradientHeader
import com.danyal.expensetracker.ui.components.MonthSwitcher
import com.danyal.expensetracker.ui.components.MonthlyBarChart
import com.danyal.expensetracker.ui.components.PillToggle
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.money
import com.danyal.expensetracker.viewmodel.CategoryTotal
import com.danyal.expensetracker.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun StatsScreen(vm: MainViewModel) {
    val month by vm.month.collectAsStateWithLifecycle()
    val summary by vm.summary.collectAsStateWithLifecycle()
    val expenseByCategory by vm.expenseByCategory.collectAsStateWithLifecycle()
    val incomeByCategory by vm.incomeByCategory.collectAsStateWithLifecycle()
    val trend by vm.monthlyTrend.collectAsStateWithLifecycle()
    val monthTransactions by vm.monthTransactions.collectAsStateWithLifecycle()
    val currency = LocalCurrency.current

    var typeIndex by remember { mutableIntStateOf(0) } // 0 expenses, 1 income
    val showIncome = typeIndex == 1
    val breakdown = if (showIncome) incomeByCategory else expenseByCategory
    val breakdownTotal = breakdown.sumOf { it.amount }

    // ----- insights -----
    val daysCounted = if (month == YearMonth.now()) LocalDate.now().dayOfMonth else month.lengthOfMonth()
    val averagePerDay = if (daysCounted > 0) summary.expense / daysCounted else 0.0
    val topCategory = expenseByCategory.firstOrNull()
    val biggestExpense = monthTransactions.filter { it.isExpense }.maxByOrNull { it.amount }
    val savingsRate = if (summary.income > 0) (summary.income - summary.expense) / summary.income * 100 else null

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GradientHeader {
            Text("Statistics", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            MonthSwitcher(
                month = month,
                canGoNext = month < YearMonth.now(),
                onPrevious = vm::previousMonth,
                onNext = vm::nextMonth
            )
            PillToggle(
                options = listOf("Expenses", "Income"),
                selected = typeIndex,
                onSelect = { typeIndex = it },
                onGradient = true
            )
        }

        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ----- donut + legend -----
            AppCard {
                Text(
                    if (showIncome) "Income by category" else "Spending by category",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (breakdown.isEmpty()) {
                    EmptyState(
                        icon = Icons.Rounded.PieChart,
                        title = "No data for this month",
                        message = "Add some transactions and your chart will appear here."
                    )
                } else {
                    Spacer(Modifier.height(16.dp))
                    key(showIncome, month, breakdown.size) {
                        DonutChart(
                            slices = breakdown.map { it.category.color to it.amount.toFloat() },
                            modifier = Modifier
                                .width(220.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (showIncome) "Earned" else "Spent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    breakdownTotal.money(currency, decimals = false),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    breakdown.forEach { item ->
                        CategoryBreakdownRow(item, breakdownTotal)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // ----- 6 month trend -----
            AppCard {
                Text(
                    "Last 6 months",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                key(month) { MonthlyBarChart(trend) }
            }

            // ----- quick insights -----
            Text(
                "Insights",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightTile(
                    icon = Icons.Rounded.CalendarToday,
                    color = Brand.Indigo500,
                    label = "Average per day",
                    value = averagePerDay.money(currency, decimals = false)
                )
                InsightTile(
                    icon = Icons.Rounded.PieChart,
                    color = topCategory?.category?.color ?: Brand.Teal,
                    label = "Top category",
                    value = topCategory?.category?.name ?: "-"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    color = Brand.Coral,
                    label = "Biggest expense",
                    value = biggestExpense?.amount?.money(currency, decimals = false) ?: "-"
                )
                InsightTile(
                    icon = Icons.Rounded.Savings,
                    color = Brand.Teal,
                    label = "Savings rate",
                    value = savingsRate?.let { "${it.toInt()}%" } ?: "-"
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(item: CategoryTotal, total: Double) {
    val currency = LocalCurrency.current
    val fraction = if (total > 0) (item.amount / total).toFloat() else 0f
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(item.category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.category.icon, contentDescription = null, tint = item.category.color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                item.category.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.amount.money(currency),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${(fraction * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = item.category.color,
            trackColor = item.category.color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun RowScope.InsightTile(icon: ImageVector, color: Color, label: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
