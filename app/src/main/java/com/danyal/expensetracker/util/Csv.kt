package com.danyal.expensetracker.util

import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.model.Categories
import java.time.LocalDate

/** Builds a spreadsheet-friendly CSV of all transactions (oldest first). */
fun buildCsv(list: List<TransactionEntity>): String {
    fun esc(s: String): String = "\"" + s.replace("\"", "\"\"") + "\""
    val sb = StringBuilder("Date,Type,Category,Title,Amount,Note\n")
    list.sortedBy { it.dateEpochDay }.forEach { t ->
        sb.append(LocalDate.ofEpochDay(t.dateEpochDay)).append(',')
            .append(t.type).append(',')
            .append(esc(Categories.byId(t.categoryId).name)).append(',')
            .append(esc(t.title)).append(',')
            .append(t.amount).append(',')
            .append(esc(t.note)).append('\n')
    }
    return sb.toString()
}
