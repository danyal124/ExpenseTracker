package com.danyal.expensetracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** A transaction is either money coming in or money going out. */
enum class TxType { INCOME, EXPENSE }

/**
 * A spending/earning category. Categories live in code (not in the database),
 * transactions only store the category [id].
 */
data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: TxType
)

object Categories {
    /** Special budget key meaning "the whole month, all categories together". */
    const val ALL_BUDGET = "ALL"

    val expense: List<Category> = listOf(
        Category("food", "Food & Dining", Icons.Rounded.Restaurant, Color(0xFFF97316), TxType.EXPENSE),
        Category("transport", "Transport", Icons.Rounded.DirectionsCar, Color(0xFF3B82F6), TxType.EXPENSE),
        Category("shopping", "Shopping", Icons.Rounded.ShoppingBag, Color(0xFFEC4899), TxType.EXPENSE),
        Category("bills", "Bills & Utilities", Icons.Rounded.ReceiptLong, Color(0xFF8B5CF6), TxType.EXPENSE),
        Category("entertainment", "Entertainment", Icons.Rounded.Movie, Color(0xFFEF4444), TxType.EXPENSE),
        Category("health", "Health", Icons.Rounded.LocalHospital, Color(0xFF10B981), TxType.EXPENSE),
        Category("education", "Education", Icons.Rounded.School, Color(0xFF06B6D4), TxType.EXPENSE),
        Category("travel", "Travel", Icons.Rounded.Flight, Color(0xFFEAB308), TxType.EXPENSE),
        Category("home", "Rent & Home", Icons.Rounded.Home, Color(0xFF14B8A6), TxType.EXPENSE),
        Category("other_exp", "Other", Icons.Rounded.MoreHoriz, Color(0xFF64748B), TxType.EXPENSE)
    )

    val income: List<Category> = listOf(
        Category("salary", "Salary", Icons.Rounded.Payments, Color(0xFF22C55E), TxType.INCOME),
        Category("freelance", "Freelance", Icons.Rounded.Laptop, Color(0xFF0EA5E9), TxType.INCOME),
        Category("business", "Business", Icons.Rounded.Work, Color(0xFFA855F7), TxType.INCOME),
        Category("gift", "Gift", Icons.Rounded.CardGiftcard, Color(0xFFF43F5E), TxType.INCOME),
        Category("invest", "Investments", Icons.Rounded.TrendingUp, Color(0xFFF59E0B), TxType.INCOME),
        Category("other_inc", "Other", Icons.Rounded.MoreHoriz, Color(0xFF64748B), TxType.INCOME)
    )

    val all: List<Category> = expense + income

    fun byId(id: String): Category = all.firstOrNull { it.id == id } ?: expense.last()

    fun forType(type: TxType): List<Category> = if (type == TxType.EXPENSE) expense else income
}
