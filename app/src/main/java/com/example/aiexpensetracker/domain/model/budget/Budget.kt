package com.example.aiexpensetracker.domain.model.budget

data class Budget(
    val amount: Double,
    val month: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
