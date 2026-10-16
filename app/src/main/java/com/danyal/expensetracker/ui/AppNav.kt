package com.danyal.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danyal.expensetracker.data.TransactionEntity
import com.danyal.expensetracker.ui.screens.BudgetsScreen
import com.danyal.expensetracker.ui.screens.EditScreen
import com.danyal.expensetracker.ui.screens.HistoryScreen
import com.danyal.expensetracker.ui.screens.HomeScreen
import com.danyal.expensetracker.ui.screens.SettingsScreen
import com.danyal.expensetracker.ui.screens.StatsScreen
import com.danyal.expensetracker.ui.theme.Brand
import com.danyal.expensetracker.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Home", Icons.Rounded.Home),
    History("history", "History", Icons.Rounded.ReceiptLong),
    Stats("stats", "Stats", Icons.Rounded.BarChart),
    Budgets("budgets", "Budgets", Icons.Rounded.Savings),
    Settings("settings", "Settings", Icons.Rounded.Settings)
}

private const val EDIT_ROUTE = "edit/{id}"

@Composable
fun ExpenseTrackerApp(vm: MainViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onTab = Tab.entries.any { it.route == route }
    val showFab = route == Tab.Home.route || route == Tab.History.route

    /** Deletes a transaction and shows a snackbar that lets the user undo it. */
    fun deleteWithUndo(tx: TransactionEntity) {
        vm.delete(tx)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "Undo"
            )
            if (result == SnackbarResult.ActionPerformed) vm.restore(tx)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("edit/0") },
                    containerColor = Brand.Teal,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Add") }
                )
            }
        },
        bottomBar = {
            if (onTab) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Only the bottom padding is applied so gradient headers can reach the top of the screen.
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Tab.Home.route) {
                HomeScreen(
                    vm = vm,
                    onEdit = { id -> navController.navigate("edit/$id") },
                    onSeeAll = { navController.navigate(Tab.History.route) },
                    onOpenBudgets = { navController.navigate(Tab.Budgets.route) }
                )
            }
            composable(Tab.History.route) {
                HistoryScreen(
                    vm = vm,
                    onEdit = { id -> navController.navigate("edit/$id") },
                    onDelete = { tx -> deleteWithUndo(tx) }
                )
            }
            composable(Tab.Stats.route) { StatsScreen(vm) }
            composable(Tab.Budgets.route) { BudgetsScreen(vm) }
            composable(Tab.Settings.route) { SettingsScreen(vm) }
            composable(
                route = EDIT_ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                EditScreen(
                    vm = vm,
                    transactionId = entry.arguments?.getLong("id") ?: 0L,
                    onClose = { navController.popBackStack() },
                    onDeleted = { tx ->
                        navController.popBackStack()
                        deleteWithUndo(tx)
                    }
                )
            }
        }
    }
}
