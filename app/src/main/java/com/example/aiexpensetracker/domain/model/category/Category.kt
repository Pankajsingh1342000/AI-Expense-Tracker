package com.example.aiexpensetracker.domain.model.category

data class Category(
    val name: String,
    val keywords: List<String>,
    val isDefault: Boolean = false
)