package com.example.aiexpensetracker.domain.model.category

data class CategoryStatistic(
    val category: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val averageAmount: Double
)
