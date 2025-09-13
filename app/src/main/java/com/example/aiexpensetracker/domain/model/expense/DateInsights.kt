package com.example.aiexpensetracker.domain.model.expense

data class DateInsights(
    val date: Long,
    val totalSpent: Double,
    val transactionCount: Int,
    val averagePerTransaction: Double,
    val largestExpense: Expense?,
    val smallestExpense: Expense?,
    val categoryBreakdown: List<Pair<String, Double>>,
    val expenses: List<Expense>
)
