package com.example.aiexpensetracker.domain.usecase

import android.annotation.SuppressLint
import com.example.aiexpensetracker.domain.model.category.Category
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import timber.log.Timber
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    @SuppressLint("DefaultLocale")
    suspend operator fun invoke(input: String): Boolean {
        // Extract category name from input like "add fitness to the category"
        val categoryName = extractCategoryName(input) ?: return false

        // Generate some basic keywords based on the category name
        val keywords = generateDefaultKeywords(categoryName)

        val category = Category(
            name = categoryName.capitalize(),
            keywords = keywords,
            isDefault = false
        )

        return try {
            categoryRepository.addCategory(category)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to add category: $categoryName")
            false
        }
    }

    private fun extractCategoryName(input: String): String? {
        val patterns = listOf(
            Regex("""add\s+(\w+)\s+to\s+the?\s+category""", RegexOption.IGNORE_CASE),
            Regex("""add\s+(\w+)\s+category""", RegexOption.IGNORE_CASE),
            Regex("""create\s+(\w+)\s+category""", RegexOption.IGNORE_CASE),
            Regex("""new\s+category\s+(\w+)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    private fun generateDefaultKeywords(categoryName: String): List<String> {
        val baseName = categoryName.lowercase()
        return listOf(baseName, "${baseName}s", baseName.take(4))
    }
}