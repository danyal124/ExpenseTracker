package com.danyal.expensetracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.danyal.expensetracker.ExpenseApp
import com.danyal.expensetracker.data.BudgetEntity
import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.data.isExpense
import com.danyal.expensetracker.data.isIncome
import com.danyal.expensetracker.model.Categories
import com.danyal.expensetracker.model.Category
import com.danyal.expensetracker.util.BudgetNotifier
import com.danyal.expensetracker.util.buildCsv
import com.danyal.expensetracker.util.money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

data class MonthSummary(val income: Double = 0.0, val expense: Double = 0.0) {
    val balance: Double get() = income - expense
}

data class CategoryTotal(val category: Category, val amount: Double)

data class MonthBar(val label: String, val income: Double, val expense: Double)

/** A budget together with how much has been spent against it in the selected month. */
data class BudgetStatus(
    val budget: BudgetEntity,
    val label: String,
    val category: Category?,
    val spent: Double
) {
    val ratio: Float
        get() = if (budget.limitAmount <= 0) 0f else (spent / budget.limitAmount).toFloat()
}

private fun TransactionEntity.yearMonth(): YearMonth = YearMonth.from(LocalDate.ofEpochDay(dateEpochDay))

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExpenseApp
    private val repo = app.repository
    private val settingsRepo = app.settingsRepository

    val settings = settingsRepo.settings

    // ----- raw data -----
    val transactions: StateFlow<List<TransactionEntity>> =
        repo.transactions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val budgets: StateFlow<List<BudgetEntity>> =
        repo.budgets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ----- selected month (shared by Home, Stats and Budgets) -----
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    fun previousMonth() = _month.update { it.minusMonths(1) }

    fun nextMonth() {
        if (_month.value < YearMonth.now()) _month.update { it.plusMonths(1) }
    }

    // ----- derived data -----
    val monthTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, month) { list, m -> list.filter { it.yearMonth() == m } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val summary: StateFlow<MonthSummary> = monthTransactions.map { list ->
        MonthSummary(
            income = list.filter { it.isIncome }.sumOf { it.amount },
            expense = list.filter { it.isExpense }.sumOf { it.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MonthSummary())

    /** All-time balance (income minus expenses). */
    val totalBalance: StateFlow<Double> = transactions.map { list ->
        list.filter { it.isIncome }.sumOf { it.amount } - list.filter { it.isExpense }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val expenseByCategory: StateFlow<List<CategoryTotal>> = monthTransactions.map { list ->
        totalsByCategory(list.filter { it.isExpense })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val incomeByCategory: StateFlow<List<CategoryTotal>> = monthTransactions.map { list ->
        totalsByCategory(list.filter { it.isIncome })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun totalsByCategory(list: List<TransactionEntity>): List<CategoryTotal> =
        list.groupBy { it.categoryId }
            .map { (id, items) -> CategoryTotal(Categories.byId(id), items.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

    /** Income and expenses for the 6 months ending at the selected month. */
    val monthlyTrend: StateFlow<List<MonthBar>> = combine(transactions, month) { list, m ->
        (5 downTo 0).map { back ->
            val ym = m.minusMonths(back.toLong())
            val inMonth = list.filter { it.yearMonth() == ym }
            MonthBar(
                label = ym.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                income = inMonth.filter { it.isIncome }.sumOf { it.amount },
                expense = inMonth.filter { it.isExpense }.sumOf { it.amount }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val budgetStatuses: StateFlow<List<BudgetStatus>> = combine(budgets, monthTransactions) { b, list ->
        b.map { budget -> statusFor(budget, list) }
            .sortedWith(compareBy<BudgetStatus>({ it.budget.categoryId != Categories.ALL_BUDGET }, { -it.ratio }))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun statusFor(budget: BudgetEntity, monthList: List<TransactionEntity>): BudgetStatus {
        val isAll = budget.categoryId == Categories.ALL_BUDGET
        val spent = monthList
            .filter { it.isExpense && (isAll || it.categoryId == budget.categoryId) }
            .sumOf { it.amount }
        val category = if (isAll) null else Categories.byId(budget.categoryId)
        return BudgetStatus(budget, category?.name ?: "Overall monthly budget", category, spent)
    }

    // ----- actions -----
    suspend fun getTransaction(id: Long): TransactionEntity? = repo.get(id)

    fun save(entity: TransactionEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.save(entity)
            if (entity.isExpense) checkBudgetAlerts(entity)
            onDone()
        }
    }

    fun delete(entity: TransactionEntity) {
        viewModelScope.launch { repo.delete(entity) }
    }

    fun restore(entity: TransactionEntity) {
        viewModelScope.launch { repo.restore(entity) }
    }

    fun saveBudget(categoryId: String, limit: Double) {
        viewModelScope.launch { repo.saveBudget(BudgetEntity(categoryId, limit)) }
    }

    fun deleteBudget(categoryId: String) {
        viewModelScope.launch { repo.deleteBudget(categoryId) }
    }

    fun setCurrency(symbol: String) = settingsRepo.setCurrency(symbol)

    fun setThemeMode(mode: Int) = settingsRepo.setThemeMode(mode)

    fun clearAll() {
        viewModelScope.launch { repo.clearAll() }
    }

    /** Writes every transaction as CSV into the file the user picked. */
    fun exportCsv(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    val csv = buildCsv(repo.allTransactions())
                    val stream = getApplication<Application>().contentResolver.openOutputStream(uri)
                        ?: return@runCatching false
                    stream.use { it.write(csv.toByteArray()) }
                    true
                }.getOrDefault(false)
            }
            onResult(ok)
        }
    }

    /** After saving an expense, warn if a related budget is at 80% or has been exceeded. */
    private suspend fun checkBudgetAlerts(tx: TransactionEntity) {
        val ym = tx.yearMonth()
        val monthExpenses = repo.allTransactions().filter { it.isExpense && it.yearMonth() == ym }
        val symbol = settings.value.currency
        val context = getApplication<Application>()

        for (budget in budgets.value) {
            val isAll = budget.categoryId == Categories.ALL_BUDGET
            if (!isAll && budget.categoryId != tx.categoryId) continue
            if (budget.limitAmount <= 0) continue

            val spent = monthExpenses
                .filter { isAll || it.categoryId == budget.categoryId }
                .sumOf { it.amount }
            val ratio = spent / budget.limitAmount
            val name = if (isAll) "overall" else Categories.byId(budget.categoryId).name

            if (ratio >= 1.0) {
                BudgetNotifier.show(
                    context, budget.categoryId.hashCode(), "Budget exceeded",
                    "Your $name budget is over: ${spent.money(symbol)} of ${budget.limitAmount.money(symbol)}"
                )
            } else if (ratio >= 0.8) {
                BudgetNotifier.show(
                    context, budget.categoryId.hashCode(), "Budget warning",
                    "You have used ${(ratio * 100).toInt()}% of your $name budget"
                )
            }
        }
    }
}
