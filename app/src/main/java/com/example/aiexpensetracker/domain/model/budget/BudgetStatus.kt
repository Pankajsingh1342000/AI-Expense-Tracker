package com.example.aiexpensetracker.domain.model.budget

data class BudgetStatus(
    val budget: Budget?,
    val totalSpent: Double,
    val remaining: Double,
    val percentageUsed: Double,
    val isOverBudget: Boolean
)
