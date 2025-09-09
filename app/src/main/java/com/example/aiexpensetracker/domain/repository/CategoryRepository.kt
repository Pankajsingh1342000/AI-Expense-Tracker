package com.example.aiexpensetracker.domain.repository

import com.example.aiexpensetracker.domain.model.category.Category

interface CategoryRepository {

    suspend fun getCategories(): List<Category>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(categoryName: String)
    suspend fun initializeDefaultCategories()
    suspend fun updateCategoryKeywords(categoryName: String, newKeywords: List<String>)
    suspend fun getCategoryByName(name: String): Category?
    suspend fun searchCategoriesByKeyword(keyword: String): List<Category>
}