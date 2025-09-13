package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.domain.model.expense.Expense
import timber.log.Timber
import javax.inject.Inject

class ExtractExpenseUseCase @Inject constructor() {

    private val amountPatterns = listOf(
        Regex("""(\d+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?|₹)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rupees?|rs\.?|₹)\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(\d+(?:\.\d{1,2})?)(?:\s*(?:rupees?|rs\.?|₹))""", RegexOption.IGNORE_CASE)
    )

    private val expenseActionPatterns = listOf(
        Regex("""(?:i\s+)?(?:bought|purchased|paid for|spent on)\s+(.+?)(?:\s+for|\s+of|\s+₹|\s+rs|\s+rupees|\s+\d|$)""", RegexOption.IGNORE_CASE),
        Regex("""(?:i\s+)?(?:bought|purchased|got)\s+(.+)""", RegexOption.IGNORE_CASE)
    )

    operator fun invoke(text: String): Expense? {
        val cleanText = text.trim()

        Timber.d("Extracting expense from: '$cleanText'")

        // Extract amount
        val amount = extractAmount(cleanText)
        if (amount == null) {
            Timber.d("No valid amount found")
            return null
        }

        // Extract description
        val description = extractDescription(cleanText)
        if (description.isBlank()) {
            Timber.d("No valid description found")
            return null
        }

        Timber.d("Extracted - Amount: $amount, Description: '$description'")

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
                val amountStr = match.groupValues[1]
                return amountStr.toDoubleOrNull()?.also {
                    Timber.d("Found amount: $it using pattern: ${pattern.pattern}")
                }
            }
        }
        return null
    }

    private fun extractDescription(text: String): String {
        // First, try to extract using action patterns
        for (pattern in expenseActionPatterns) {
            val match = pattern.find(text)
            if (match != null && match.groupValues.size > 1) {
                val description = match.groupValues[1].trim()
                if (description.isNotBlank()) {
                    return cleanDescription(description)
                }
            }
        }

        // Fallback: clean the whole text
        return cleanDescription(text)
    }

    private fun cleanDescription(description: String): String {
        var cleaned = description

        // Remove amount patterns
        for (pattern in amountPatterns) {
            cleaned = cleaned.replace(pattern, " ")
        }

        // Remove common action words if at the start
        cleaned = cleaned.replace(
            Regex("""^(?:i\s+)?(?:bought|purchased|paid for|spent on)\s+""", RegexOption.IGNORE_CASE),
            ""
        )

        // Remove "for", "of" followed by amounts
        cleaned = cleaned.replace(
            Regex("""\b(?:for|of)\s+\d+(?:\.\d{1,2})?\s*(?:rupees?|rs\.?|₹)?""", RegexOption.IGNORE_CASE),
            ""
        )

        // Clean up whitespace
        cleaned = cleaned.replace(Regex("""\s+"""), " ").trim()

        return cleaned.takeIf { it.isNotBlank() } ?: "Expense"
    }
}