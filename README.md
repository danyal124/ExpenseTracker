# Expense Tracker (Kotlin + Jetpack Compose + Room)

A colourful, offline expense tracker. Indigo-to-teal gradient headers, white cards, animated charts, light and dark mode.

## How to run
1. Install **Android Studio** (Koala 2024.1 or newer) with JDK 17 (bundled).
2. `File > Open` and select this `ExpenseTracker` folder.
3. Wait for **Gradle Sync** to finish (first sync downloads dependencies).
4. Pick an emulator or a phone with USB debugging, then press **Run**.

If Android Studio offers to update Gradle or the Android Gradle Plugin, you can accept.

## Features
- **Home**: total balance, month income/expenses, budget alerts, recent transactions
- **Add / edit**: expense or income, amount, category, date picker, notes, delete
- **History**: search, filter by type and category, grouped by day, swipe left to delete with Undo
- **Stats**: animated donut chart by category, 6-month income vs expense bars, insights (average per day, top category, biggest expense, savings rate)
- **Budgets**: overall and per-category monthly limits, colour-coded progress (teal, amber at 80%, red when exceeded) and a notification when you cross 80% or 100%
- **Settings**: currency (PKR, USD, EUR, GBP, SEK, INR, AED), theme (system, light, dark), CSV export, delete all data
- All data is stored locally in a Room database. No account and no internet needed.

## Project map
```
app/src/main/java/com/danyal/expensetracker/
  MainActivity.kt        entry point, theme, notification permission
  ExpenseApp.kt          creates database and repositories
  data/                  Room entities, DAOs, repository, settings
  model/Category.kt      expense and income categories (icon + colour)
  viewmodel/             MainViewModel: all state and actions
  util/                  money formatting, CSV export, budget notifications
  ui/theme/Theme.kt      colours, typography, shapes
  ui/components/         header, cards, charts, transaction row
  ui/screens/            Home, History, Stats, Budgets, Settings, Edit
  ui/AppNav.kt           bottom navigation and routes
```

## Easy customisations
- Colours: `ui/theme/Theme.kt` (object `Brand`)
- Categories: `model/Category.kt`
- Currencies: `data/SettingsRepository.kt` (`CURRENCIES`)
