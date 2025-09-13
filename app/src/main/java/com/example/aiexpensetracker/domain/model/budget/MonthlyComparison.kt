package com.example.aiexpensetracker.domain.model.budget

data class MonthlyComparison(
    val currentMonth: BudgetStatus,
    val previousMonth: BudgetStatus,
    val spendingChange: Double,
    val percentageChange: Double
)
