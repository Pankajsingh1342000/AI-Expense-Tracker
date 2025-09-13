package com.example.aiexpensetracker.domain.usecase

import timber.log.Timber
import javax.inject.Inject

class SmartAmountExtractor @Inject constructor() {

    private val amountPatterns = listOf(
        // Standard currency formats with context
        Regex("""(\d+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?|₹|bucks?)""", RegexOption.IGNORE_CASE),

        // Currency before number
        Regex("""(?:rupees?|rs\.?|₹)\s*(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE),

        // K format (thousands)
        Regex("""(\d+(?:\.\d+)?)\s*k(?:\s*(?:rupees?|rs\.?|₹))?""", RegexOption.IGNORE_CASE),

        // Context-based patterns
        Regex("""(?:cost|paid|spent|bill|fare|fee|price|charge|worth)\s*(?:is|was|of)?\s*(\d+(?:\.\d{1,2})?)(?:\s*(?:rupees?|rs\.?|₹))?""", RegexOption.IGNORE_CASE),

        // "of X rupees" pattern - specifically for your case
        Regex("""of\s+(\d+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?|₹|bucks?)""", RegexOption.IGNORE_CASE),

        // Bare numbers with expense context words nearby
        Regex("""(?:bought|purchased|paid|spent|got|ordered).{0,50}(\d+(?:\.\d{1,2})?)\s*(?:rupees?|rs\.?|₹|only|total)?""", RegexOption.IGNORE_CASE),

        // Range amounts (take average)
        Regex("""(\d+)\s*(?:to|-)\s*(\d+)\s*(?:rupees?|rs\.?|₹)?""", RegexOption.IGNORE_CASE),

        // Word numbers
        Regex("""(hundred|thousand|lakh)\s*(\d+)?\s*(?:rupees?|rs\.?|₹)?""", RegexOption.IGNORE_CASE)
    )

    fun extractAmount(text: String): Double? {
        val normalizedText = text.lowercase().trim()
        Timber.d("Extracting amount from: '$normalizedText'")

        for ((index, pattern) in amountPatterns.withIndex()) {
            val match = pattern.find(normalizedText)
            if (match != null) {
                Timber.d("Pattern $index matched: ${match.value}")

                val amount = when (index) {
                    2 -> { // K format
                        val number = match.groupValues[1].toDoubleOrNull() ?: continue
                        number * 1000
                    }
                    6 -> { // Range - take average
                        val start = match.groupValues[1].toDoubleOrNull() ?: continue
                        val end = match.groupValues[2].toDoubleOrNull() ?: continue
                        (start + end) / 2
                    }
                    7 -> { // Word numbers
                        parseWordNumber(match.groupValues[1], match.groupValues.getOrNull(2))
                    }
                    else -> match.groupValues[1].toDoubleOrNull()
                }

                if (amount != null && amount > 0) {
                    Timber.d("Extracted amount: $amount")
                    return amount
                }
            }
        }

        Timber.d("No valid amount found")
        return null
    }

    private fun parseWordNumber(word: String, number: String?): Double {
        val multiplier = when (word.lowercase()) {
            "hundred" -> 100.0
            "thousand" -> 1000.0
            "lakh" -> 100000.0
            else -> 1.0
        }
        val base = number?.toDoubleOrNull() ?: 1.0
        return base * multiplier
    }
}
