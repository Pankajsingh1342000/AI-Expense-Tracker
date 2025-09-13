package com.example.aiexpensetracker.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.aiexpensetracker.domain.model.budget.Budget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.budgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "budget_preferences")

@Singleton
class BudgetPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.budgetDataStore

    companion object {

        private fun budgetAmountKey(month: String) = doublePreferencesKey("budget_amount_$month")
        private fun budgetCreatedAtKey(month: String) = longPreferencesKey("budget_created_at_$month")
        private fun budgetUpdatedAtKey(month: String) = longPreferencesKey("budget_updated_at_$month")

        private val CURRENT_BUDGET_MONTH = stringPreferencesKey("current_budget_month")
        private val CURRENT_BUDGET_AMOUNT = doublePreferencesKey("current_budget_amount")
    }

    suspend fun saveBudget(amount: Double, month: String = getCurrentMonth()): Boolean {
        return try {
            dataStore.edit { preferences ->
                val currentTime = System.currentTimeMillis()

                preferences[budgetAmountKey(month)] = amount
                preferences[budgetUpdatedAtKey(month)] = currentTime

                if (!preferences.contains(budgetCreatedAtKey(month))) {
                    preferences[budgetCreatedAtKey(month)] = currentTime
                }

                preferences[CURRENT_BUDGET_MONTH] = month
                preferences[CURRENT_BUDGET_AMOUNT] = amount
            }

            Timber.d("Budget saved: ₹$amount for month $month")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to save budget")
            false
        }
    }

    suspend fun getBudget(month: String = getCurrentMonth()): Budget? {
        return try {
            dataStore.data.map { preferences ->
                val amount = preferences[budgetAmountKey(month)]
                val createdAt = preferences[budgetCreatedAtKey(month)]
                val updatedAt = preferences[budgetUpdatedAtKey(month)]

                if (amount != null && createdAt != null && updatedAt != null) {
                    Budget(
                        amount = amount,
                        month = month,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                } else null
            }.first()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get budget for month $month")
            null
        }
    }

    fun getCurrentBudgetFlow(): Flow<Budget?> {
        return dataStore.data.map { preferences ->
            val month = preferences[CURRENT_BUDGET_MONTH]
            val amount = preferences[CURRENT_BUDGET_AMOUNT]

            if (month != null && amount != null) {

                val createdAt = preferences[budgetCreatedAtKey(month)] ?: System.currentTimeMillis()
                val updatedAt = preferences[budgetUpdatedAtKey(month)] ?: System.currentTimeMillis()

                Budget(
                    amount = amount,
                    month = month,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } else null
        }
    }

    suspend fun deleteBudget(month: String = getCurrentMonth()): Boolean {
        return try {
            dataStore.edit { preferences ->
                preferences.remove(budgetAmountKey(month))
                preferences.remove(budgetCreatedAtKey(month))
                preferences.remove(budgetUpdatedAtKey(month))

                if (preferences[CURRENT_BUDGET_MONTH] == month) {
                    preferences.remove(CURRENT_BUDGET_MONTH)
                    preferences.remove(CURRENT_BUDGET_AMOUNT)
                }
            }

            Timber.d("Budget deleted for month $month")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete budget")
            false
        }
    }

    fun getAllBudgetMonths(): Flow<List<String>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith("budget_amount_") }
                .map { it.name.removePrefix("budget_amount_") }
                .sorted()
                .reversed()
        }
    }

    private fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
}