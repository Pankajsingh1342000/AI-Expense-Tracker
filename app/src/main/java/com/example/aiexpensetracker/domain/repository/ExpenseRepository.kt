package com.example.aiexpensetracker.domain.repository

import com.example.aiexpensetracker.domain.model.category.CategoryStatistic
import com.example.aiexpensetracker.domain.model.expense.DateInsights
import com.example.aiexpensetracker.domain.model.expense.Expense
import com.example.aiexpensetracker.domain.model.expense.SpendingInsights
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun deleteExpense(expense: Expense)
    suspend fun getExpenseByCategory(category: String): List<Expense>
    suspend fun getExpenseByDateRange(startDate: Long, endDate: Long): List<Expense>
    suspend fun getTotalAmount(): Double
    suspend fun getExpenseByCurrentMonth(): List<Expense>
    suspend fun getExpensesByLastMonth(): List<Expense>
    suspend fun getExpensesByCurrentWeek(): List<Expense>
    suspend fun getTotalByCategory(category: String): Double
    suspend fun getTopCategories(limit: Int = 5): List<Pair<String, Double>>
    suspend fun getTotalCurrentMonth(): Double
    suspend fun getTotalLastMonth(): Double
    suspend fun getExpenseCountCurrentMonth(): Int
    suspend fun getExpenseCountLastMonth(): Int
    suspend fun getExpenseCountCurrentWeek(): Int
    suspend fun getAverageExpenseByCategory(category: String): Double
    suspend fun getAllCategoriesWithAverages(): List<Pair<String, Double>>
    suspend fun getBiggestExpenses(limit: Int = 10): List<Expense>
    suspend fun getLargeExpensesAboveAmount(amount: Double, limit: Int = 10): List<Expense>
    suspend fun getCategoryStatistics(): List<CategoryStatistic>
    suspend fun getSpendingFrequencyByCategory(): List<Pair<String, Int>>
    suspend fun getSpendingInsights(): SpendingInsights

    suspend fun getExpensesBySpecificMonth(month: String): List<Expense>
    suspend fun getTotalBySpecificMonth(month: String): Double
    suspend fun getExpenseCountBySpecificMonth(month: String): Int

    suspend fun getExpensesByDateQuery(raw: String): List<Expense>
    suspend fun getInsightsByDateQuery(raw: String): DateInsights

}