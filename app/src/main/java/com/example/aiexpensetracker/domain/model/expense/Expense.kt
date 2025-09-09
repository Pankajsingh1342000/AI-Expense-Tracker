package com.example.aiexpensetracker.domain.model.expense

data class Expense(
    val id : Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)