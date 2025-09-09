package com.example.aiexpensetracker.domain.model.expense

import com.example.aiexpensetracker.domain.model.category.CategoryStatistic

data class SpendingInsights(
    val currentMonthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val monthlyChange: Double = 0.0,
    val transactionCount: Int = 0,
    val averagePerTransaction: Double = 0.0,
    val biggestExpenses: List<Expense> = emptyList(),
    val topCategories: List<Pair<String, Double>> = emptyList(),
    val categoryStatistics: List<CategoryStatistic> = emptyList()
)
