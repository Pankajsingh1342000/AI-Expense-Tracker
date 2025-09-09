package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.domain.model.expense.Expense
import javax.inject.Inject

class ExtractExpenseUseCase @Inject constructor() {
    private val amountPatterns = listOf(
        Regex("""(\d+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?|₹)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rupees?|rs\.?|₹)\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(\d+(?:\.\d{1,2})?)""")
    )

    private val descriptionCleanupPatterns = listOf(
        Regex("""(?:i\s+)?(?:bought|purchased|paid for|spent on)\s+""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:for|of)\s+\d+(?:\.\d{1,2})?\s*(?:rupees?|rs\.?|₹)""", RegexOption.IGNORE_CASE),
        Regex("""\d+(?:\.\d{1,2})?\s*(?:rupees?|rs\.?|₹)""", RegexOption.IGNORE_CASE)
    )

    operator fun invoke(text: String): Expense? {
        val cleanText = text.trim()

        // Extract amount
        val amount = extractAmount(cleanText) ?: return null

        // Extract and clean description
        val description = extractDescription(cleanText)

        if (description.isBlank()) return null

        return Expense(
            amount = amount,
            description = description,
            category = "Uncategorized", // Will be categorized separately
            date = System.currentTimeMillis()
        )
    }

    private fun extractAmount(text: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        return null
    }

    private fun extractDescription(text: String): String {
        var description = text

        // Remove amount patterns
        for (pattern in amountPatterns) {
            description = description.replace(pattern, " ")
        }

        // Clean up common phrases
        for (pattern in descriptionCleanupPatterns) {
            description = description.replace(pattern, " ")
        }

        return description.trim()
            .replace(Regex("""\s+"""), " ")
            .takeIf { it.isNotBlank() } ?: "Expense"
    }
}