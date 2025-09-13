package com.example.aiexpensetracker.domain.usecase

import javax.inject.Inject

class ExpenseDetectionUseCase @Inject constructor() {

    private val expenseIndicators = listOf(
        // Action words
        "bought", "purchased", "paid", "spent", "got", "ordered",
        "bill", "cost", "price", "charge", "fare", "fee", "subscription",
        // Context indicators
        "for", "at", "from", "to", "on", "in", "of", "worth"
    )

    private val categoryKeywords = listOf(
        "food", "transport", "shopping", "medical", "fuel",
        "groceries", "coffee", "lunch", "dinner", "taxi", "uber",
        "netflix", "spotify", "subscription", "membership", "plan"
    )

    fun isExpenseInput(input: String): Boolean {
        val normalizedInput = input.lowercase().trim()

        // Method 1: Has amount pattern
        val hasAmount = hasAmountPattern(normalizedInput)

        // Method 2: Has expense context (more lenient)
        val hasExpenseContext = expenseIndicators.any {
            normalizedInput.contains(it, ignoreCase = true)
        }

        // Method 3: Expense-like structure detection
        val hasExpenseStructure = detectExpenseStructure(normalizedInput)

        // Method 4: Category + amount combination
        val hasCategoryAmount = hasCategoryWithAmount(normalizedInput)

        // Method 5: Subscription/service patterns
        val hasServicePattern = hasServicePattern(normalizedInput)

        return hasAmount && (hasExpenseContext || hasExpenseStructure || hasCategoryAmount || hasServicePattern)
    }

    private fun hasAmountPattern(input: String): Boolean {
        val patterns = listOf(
            Regex("""\d+\s*(?:rupees?|rs\.?|₹|bucks?)""", RegexOption.IGNORE_CASE),
            Regex("""(?:rupees?|rs\.?|₹)\s*\d+""", RegexOption.IGNORE_CASE),
            Regex("""\d+(?:\.\d+)?\s*k""", RegexOption.IGNORE_CASE), // "5k"
            Regex("""(?:cost|paid|spent|bill|fare|fee|price|charge|worth)\s*(?:is|was|of)?\s*\d+""", RegexOption.IGNORE_CASE),
            Regex("""of\s+\d+\s*(?:rupees?|rs\.?|₹)?""", RegexOption.IGNORE_CASE) // "of 100 rupees"
        )
        return patterns.any { it.find(input) != null }
    }

    private fun detectExpenseStructure(input: String): Boolean {
        val structurePatterns = listOf(
            Regex("""^\w+\s+\d+"""), // "coffee 50"
            Regex("""\d+\s+for\s+\w+"""), // "50 for coffee"
            Regex("""\w+\s+for\s+\d+"""), // "coffee for 50"
            Regex("""\d+\s+\w+"""), // "50 coffee"
            Regex("""\w+\s+of\s+\w+\s+of\s+\d+""") // "subscription of netflix of 100"
        )
        return structurePatterns.any { it.find(input) != null }
    }

    private fun hasCategoryWithAmount(input: String): Boolean {
        val hasCategory = categoryKeywords.any { input.contains(it, ignoreCase = true) }
        val hasNumber = Regex("""\d+""").find(input) != null
        return hasCategory && hasNumber
    }

    private fun hasServicePattern(input: String): Boolean {
        val servicePatterns = listOf(
            Regex("""(?:subscription|membership|plan)\s+(?:of|for)\s+\w+""", RegexOption.IGNORE_CASE),
            Regex("""(?:bought|got|purchased)\s+(?:subscription|membership|plan)""", RegexOption.IGNORE_CASE)
        )
        return servicePatterns.any { it.find(input) != null }
    }
}