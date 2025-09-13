package com.example.aiexpensetracker.domain.repository

import com.example.aiexpensetracker.domain.model.budget.Budget
import com.example.aiexpensetracker.domain.model.budget.BudgetStatus
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    suspend fun setBudget(amount: Double, month: String? = null): Boolean
    suspend fun getBudget(month: String? = null): Budget?
    fun getCurrentBudgetFlow(): Flow<Budget?>
    suspend fun getBudgetStatus(month: String? = null): BudgetStatus
    suspend fun deleteBudget(month: String? = null): Boolean
    suspend fun updateBudget(amount: Double, month: String? = null): Boolean
    fun getAllBudgetMonths(): Flow<List<String>>
}