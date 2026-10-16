package com.danyal.expensetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.data.isIncome
import com.danyal.expensetracker.model.Categories
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.ui.theme.LocalCurrency
import com.danyal.expensetracker.util.money
import com.danyal.expensetracker.util.pretty
import com.danyal.expensetracker.viewmodel.MonthBar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

// ---------------------------------------------------------------------------
// Layout building blocks
// ---------------------------------------------------------------------------

/** Indigo-to-teal header with soft decorative circles. Sits under the status bar. */
@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brand.HeaderBrush)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.width * 0.38f, center = Offset(size.width, 0f))
                drawCircle(Color.White.copy(alpha = 0.06f), radius = size.width * 0.26f, center = Offset(0f, size.height))
            }
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        content = content
    )
}

/** White card with rounded corners used for grouped content. */
@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, actionText: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        if (actionText != null) {
            Text(
                actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(4.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Full-width gradient button. */
@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val brush: Brush = if (enabled) Brand.ButtonBrush else SolidColor(MaterialTheme.colorScheme.outline)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(brush)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

// ---------------------------------------------------------------------------
// Controls
// ---------------------------------------------------------------------------

/** "‹  October 2026  ›" switcher, designed to sit on the gradient header. */
@Composable
fun MonthSwitcher(
    month: YearMonth,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month", tint = Color.White)
        }
        Text(
            month.format(formatter),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 150.dp)
        )
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = "Next month",
                tint = Color.White.copy(alpha = if (canGoNext) 1f else 0.35f)
            )
        }
    }
}

/** Pill-shaped segmented control, e.g. Expense / Income. */
@Composable
fun PillToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onGradient: Boolean = false
) {
    val track = if (onGradient) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
    val activeBackground = if (onGradient) Color.White else MaterialTheme.colorScheme.primary
    val activeText = if (onGradient) Brand.Indigo700 else MaterialTheme.colorScheme.onPrimary
    val inactiveText = if (onGradient) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(track)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (index == selected) activeBackground else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == selected) activeText else inactiveText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Transaction row
// ---------------------------------------------------------------------------

@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false
) {
    val category = Categories.byId(tx.categoryId)
    val currency = LocalCurrency.current
    val income = tx.isIncome

    val subtitle = buildString {
        append(category.name)
        if (showDate) append("  •  ").append(LocalDate.ofEpochDay(tx.dateEpochDay).pretty())
        if (tx.note.isNotBlank()) append("  •  ").append(tx.note)
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = category.color)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tx.title.ifBlank { category.name },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                (if (income) "+ " else "- ") + tx.amount.money(currency),
                color = if (income) Brand.Income else Brand.Expense,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/** Green when safe, amber when close to the limit, red when exceeded. */
fun budgetColor(ratio: Float): Color = when {
    ratio >= 1f -> Brand.Danger
    ratio >= 0.8f -> Brand.Amber
    else -> Brand.Teal
}

// ---------------------------------------------------------------------------
// Charts (drawn with Canvas, no external library)
// ---------------------------------------------------------------------------

/** Animated donut chart. [slices] is a list of colour to value. */
@Composable
fun DonutChart(
    slices: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 26.dp,
    center: @Composable () -> Unit = {}
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(900),
        label = "donut"
    )
    val track = MaterialTheme.colorScheme.surfaceVariant
    val total = slices.sumOf { it.second.toDouble() }.toFloat()

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val stroke = strokeWidth.toPx()
            if (total <= 0f) {
                drawArc(track, 0f, 360f, useCenter = false, style = Stroke(width = stroke))
            } else {
                var start = -90f
                val gap = if (slices.size > 1) 2.5f else 0f
                slices.forEach { (color, value) ->
                    val sweep = value / total * 360f * progress
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = max(sweep - gap, 0.1f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                    start += sweep
                }
            }
        }
        center()
    }
}

/** Animated grouped bar chart: income (teal) and expenses (coral) for each month. */
@Composable
fun MonthlyBarChart(data: List<MonthBar>, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(800),
        label = "bars"
    )
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val maxValue = max(data.maxOfOrNull { max(it.income, it.expense) } ?: 0.0, 1.0).toFloat()

    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            for (i in 0..3) {
                val y = size.height * (i / 3f)
                drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }
            if (data.isEmpty()) return@Canvas
            val groupWidth = size.width / data.size
            val barWidth = groupWidth * 0.28f
            val gap = 2.dp.toPx()
            val radius = CornerRadius(6.dp.toPx())

            data.forEachIndexed { index, bar ->
                val centerX = groupWidth * index + groupWidth / 2
                val incomeHeight = size.height * (bar.income.toFloat() / maxValue) * progress
                val expenseHeight = size.height * (bar.expense.toFloat() / maxValue) * progress
                if (incomeHeight > 0f) {
                    drawRoundRect(
                        color = Brand.Income,
                        topLeft = Offset(centerX - barWidth - gap, size.height - incomeHeight),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = radius
                    )
                }
                if (expenseHeight > 0f) {
                    drawRoundRect(
                        color = Brand.Expense,
                        topLeft = Offset(centerX + gap, size.height - expenseHeight),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = radius
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            data.forEach { bar ->
                Text(
                    bar.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LegendDot(Brand.Income, "Income")
            Spacer(Modifier.width(20.dp))
            LegendDot(Brand.Expense, "Expenses")
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
