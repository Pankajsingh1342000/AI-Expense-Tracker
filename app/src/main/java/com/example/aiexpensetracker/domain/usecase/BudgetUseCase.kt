package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.domain.model.budget.Budget
import com.example.aiexpensetracker.domain.model.budget.BudgetStatus
import com.example.aiexpensetracker.domain.model.budget.MonthlyComparison
import com.example.aiexpensetracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {
    suspend fun setBudget(amount: Double, month: String? = null): Boolean {
        return try {
            if (amount <= 0) {
                Timber.w("Invalid budget amount: $amount")
                return false
            }

            budgetRepository.setBudget(amount, month)
        } catch (e: Exception) {
            Timber.e(e, "Failed to set budget")
            false
        }
    }

    suspend fun getBudget(month: String? = null): Budget? {
        return budgetRepository.getBudget(month)
    }

    fun getCurrentBudgetFlow(): Flow<Budget?> {
        return budgetRepository.getCurrentBudgetFlow()
    }

    suspend fun getBudgetStatus(month: String? = null): BudgetStatus {
        return budgetRepository.getBudgetStatus(month)
    }

    suspend fun updateBudget(amount: Double, month: String? = null): Boolean {
        return setBudget(amount, month) // Same as setting a new budget
    }

    suspend fun deleteBudget(month: String? = null): Boolean {
        return budgetRepository.deleteBudget(month)
    }

    fun getAllBudgetMonths(): Flow<List<String>> {
        return budgetRepository.getAllBudgetMonths()
    }

    suspend fun isBudgetSet(month: String? = null): Boolean {
        return getBudget(month) != null
    }

    suspend fun getBudgetStatusForMonth(month: String): BudgetStatus {
        return budgetRepository.getBudgetStatus(month)
    }

    suspend fun getMonthlySpendingComparison(targetMonth: String): MonthlyComparison? {
        return try {
            val currentBudgetStatus = getBudgetStatus(targetMonth)
            val previousMonth = getPreviousMonth(targetMonth)
            val previousBudgetStatus = getBudgetStatus(previousMonth)

            MonthlyComparison(
                currentMonth = currentBudgetStatus,
                previousMonth = previousBudgetStatus,
                spendingChange = currentBudgetStatus.totalSpent - previousBudgetStatus.totalSpent,
                percentageChange = if (previousBudgetStatus.totalSpent > 0) {
                    ((currentBudgetStatus.totalSpent - previousBudgetStatus.totalSpent) / previousBudgetStatus.totalSpent) * 100
                } else 0.0
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting monthly comparison for $targetMonth")
            null
        }
    }

    private fun getPreviousMonth(month: String): String {
        return try {
            val parts = month.split("-")
            val year = parts[0].toInt()
            val monthIndex = parts[1].toInt()

            if (monthIndex == 1) {
                // Previous month is December of previous year
                "${year - 1}-12"
            } else {
                // Previous month in same year
                "$year-${String.format("%02d", monthIndex - 1)}"
            }
        } catch (e: Exception) {
            getCurrentMonth() // Fallback
        }
    }

    private fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
}