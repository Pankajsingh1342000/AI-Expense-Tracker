package com.example.aiexpensetracker.data.repository

import com.example.aiexpensetracker.data.local.preferences.BudgetPreferencesManager
import com.example.aiexpensetracker.di.DispatcherModule
import com.example.aiexpensetracker.domain.model.budget.Budget
import com.example.aiexpensetracker.domain.model.budget.BudgetStatus
import com.example.aiexpensetracker.domain.repository.BudgetRepository
import com.example.aiexpensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetPreferencesManager: BudgetPreferencesManager,
    private val expenseRepository: ExpenseRepository,
    @DispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
): BudgetRepository {
    override suspend fun setBudget(amount: Double, month: String?): Boolean = withContext(ioDispatcher) {
        val targetMonth = month ?: getCurrentMonth()
        budgetPreferencesManager.saveBudget(amount, targetMonth)
    }

    override suspend fun getBudget(month: String?): Budget? = withContext(ioDispatcher) {
        val targetMonth = month ?: getCurrentMonth()
        budgetPreferencesManager.getBudget(targetMonth)
    }

    override fun getCurrentBudgetFlow(): Flow<Budget?> = budgetPreferencesManager.getCurrentBudgetFlow()

    override suspend fun getBudgetStatus(month: String?): BudgetStatus = withContext(ioDispatcher) {
        val targetMonth = month ?: getCurrentMonth()
        val budget = getBudget(targetMonth)

        val totalSpent = if (targetMonth == getCurrentMonth()) {
            expenseRepository.getTotalCurrentMonth()
        } else {
            expenseRepository.getTotalBySpecificMonth(targetMonth)
        }

        val remaining = (budget?.amount ?: 0.0) - totalSpent
        val percentageUsed = if (budget != null && budget.amount > 0) {
            (totalSpent / budget.amount) * 100
        } else 0.0

        BudgetStatus(
            budget = budget,
            totalSpent = totalSpent,
            remaining = remaining,
            percentageUsed = percentageUsed,
            isOverBudget = remaining < 0
        )
    }

    override suspend fun deleteBudget(month: String?): Boolean = withContext(ioDispatcher) {
        val targetMonth = month ?: getCurrentMonth()
        budgetPreferencesManager.deleteBudget(targetMonth)
    }

    override suspend fun updateBudget(amount: Double, month: String?): Boolean = withContext(ioDispatcher) {
        val targetMonth = month ?: getCurrentMonth()
        budgetPreferencesManager.saveBudget(amount, targetMonth)
    }

    override fun getAllBudgetMonths(): Flow<List<String>> = budgetPreferencesManager.getAllBudgetMonths()

    private fun getCurrentMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }
}