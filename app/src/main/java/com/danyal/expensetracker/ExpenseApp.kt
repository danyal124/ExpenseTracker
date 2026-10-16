package com.danyal.expensetracker

import android.app.Application
import com.danyal.expensetracker.data.AppDatabase
import com.danyal.expensetracker.data.ExpenseRepository
import com.danyal.expensetracker.data.SettingsRepository

/** Application class: creates the database and repositories once for the whole app. */
class ExpenseApp : Application() {
    val database by lazy { AppDatabase.build(this) }
    val repository by lazy { ExpenseRepository(database.transactionDao(), database.budgetDao()) }
    val settingsRepository by lazy { SettingsRepository(this) }
}
