package com.example.aiexpensetracker.domain.model.query_result

import com.example.aiexpensetracker.domain.model.expense.Expense

data class QueryResult(
    val answer: String,
    val data: List<Expense> = emptyList(),
)