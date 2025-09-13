package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.domain.model.expense.Expense
import timber.log.Timber
import javax.inject.Inject

class ExtractExpenseUseCase @Inject constructor(
    private val smartAmountExtractor: SmartAmountExtractor
) {

    private val expenseActionPatterns = listOf(
        // Enhanced action patterns with better capture groups
        Regex("""(?:i\s+)?(?:bought|purchased|paid for|spent on|got|ordered)\s+(.+?)(?:\s+(?:for|of|worth|costing|at)\s+\d+|\s+₹|\s+rs|\s+rupees|\s*$)""", RegexOption.IGNORE_CASE),

        // Specific pattern for "subscription of X"
        Regex("""(?:bought|purchased|got)\s+(?:a\s+)?(?:subscription|membership)\s+(?:of\s+|for\s+)?(.+?)(?:\s+(?:for|of|worth)\s+\d+|\s+₹|\s+rs|\s+rupees|\s*$)""", RegexOption.IGNORE_CASE),

        // General pattern for "item of brand"
        Regex("""(?:bought|purchased|got)\s+(.+?)\s+(?:for|of|worth)\s+\d+""", RegexOption.IGNORE_CASE),

        // Fallback patterns
        Regex("""(?:i\s+)?(?:bought|purchased|got)\s+(.+)""", RegexOption.IGNORE_CASE)
    )

    operator fun invoke(text: String): Expense? {
        val cleanText = text.trim()
        Timber.d("Extracting expense from: '$cleanText'")

        // Use smart amount extractor
        val amount = smartAmountExtractor.extractAmount(cleanText)
        if (amount == null) {
            Timber.d("No valid amount found")
            return null
        }

        // Extract description with improved logic
        val description = extractDescription(cleanText, amount)
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

    private fun extractDescription(text: String, amount: Double): String {
        val normalizedText = text.lowercase().trim()

        // Method 1: Try action patterns
        for (pattern in expenseActionPatterns) {
            val match = pattern.find(normalizedText)
            if (match != null && match.groupValues.size > 1) {
                val description = match.groupValues[1].trim()
                if (description.isNotBlank()) {
                    val cleaned = cleanDescription(description, amount)
                    if (cleaned.isNotBlank()) {
                        Timber.d("Extracted description using pattern: '$cleaned'")
                        return cleaned
                    }
                }
            }
        }

        // Method 2: Smart fallback - remove amount and common words
        val fallbackDescription = smartFallbackExtraction(normalizedText, amount)
        if (fallbackDescription.isNotBlank()) {
            Timber.d("Extracted description using fallback: '$fallbackDescription'")
            return fallbackDescription
        }

        return "Expense"
    }

    private fun cleanDescription(description: String, amount: Double): String {
        var cleaned = description

        // Remove amount-related text
        val amountPatterns = listOf(
            "\\b${amount.toInt()}\\b",
            "\\b${String.format("%.2f", amount)}\\b",
            "\\d+\\s*(?:rupees?|rs\\.?|₹|bucks?)",
            "(?:rupees?|rs\\.?|₹)\\s*\\d+",
            "\\bof\\s+\\d+",
            "\\bworth\\s+\\d+",
            "\\bfor\\s+\\d+",
            "\\bcosting\\s+\\d+"
        )

        amountPatterns.forEach { pattern ->
            cleaned = cleaned.replace(Regex(pattern, RegexOption.IGNORE_CASE), " ")
        }

        // Clean up extra words and whitespace
        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        // Remove trailing/leading prepositions
        cleaned = cleaned.replace(Regex("^(?:of|for|at|in|on|with)\\s+", RegexOption.IGNORE_CASE), "")
        cleaned = cleaned.replace(Regex("\\s+(?:of|for|at|in|on|with)$", RegexOption.IGNORE_CASE), "")

        return cleaned.trim()
    }

    private fun smartFallbackExtraction(text: String, amount: Double): String {
        var result = text

        // Remove action words from start
        result = result.replace(Regex("^(?:i\\s+)?(?:bought|purchased|paid\\s+for|spent\\s+on|got|ordered)\\s+", RegexOption.IGNORE_CASE), "")

        // Remove amount patterns
        result = result.replace(Regex("\\b${amount.toInt()}\\s*(?:rupees?|rs\\.?|₹|bucks?)\\b", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("(?:rupees?|rs\\.?|₹)\\s*${amount.toInt()}\\b", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("\\bof\\s+${amount.toInt()}\\s*(?:rupees?|rs\\.?|₹)?", RegexOption.IGNORE_CASE), "")

        // Clean up
        result = result.replace(Regex("\\s+"), " ").trim()

        // If still contains meaningful text, return it
        if (result.length > 2 && !result.matches(Regex("\\d+"))) {
            return result.split(" ").take(4).joinToString(" ") // Limit to first 4 words
        }

        return ""
    }
}