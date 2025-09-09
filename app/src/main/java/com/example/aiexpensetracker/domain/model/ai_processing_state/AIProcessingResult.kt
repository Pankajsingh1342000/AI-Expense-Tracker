package com.example.aiexpensetracker.domain.model.ai_processing_state

import com.example.aiexpensetracker.domain.model.expense.Expense

sealed class AIProcessingResult {
    data class ExpenseAdded(val expense: Expense) : AIProcessingResult()
    data class CategoryAdded(val categoryName: String) : AIProcessingResult()
    data class QueryAnswer(val answer: String, val data: List<Expense> = emptyList()) : AIProcessingResult()
    data class Error(val message: String) : AIProcessingResult()
    object InvalidInput : AIProcessingResult()
}