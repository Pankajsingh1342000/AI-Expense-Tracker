package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.domain.model.category.Category
import javax.inject.Inject

class CategorizeExpenseUseCase @Inject constructor() {
    operator fun invoke(description: String, categories: List<Category>): String {
        val normalizedDesc = description.lowercase().trim()

        // Find best matching category based on keywords
        var bestMatch: Category? = null
        var maxMatches = 0

        for (category in categories) {
            val matches = category.keywords.count { keyword ->
                normalizedDesc.contains(keyword.lowercase())
            }

            if (matches > maxMatches) {
                maxMatches = matches
                bestMatch = category
            }
        }

        return bestMatch?.name ?: "Miscellaneous"
    }
}