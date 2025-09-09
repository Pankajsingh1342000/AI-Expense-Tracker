package com.example.aiexpensetracker.domain.usecase

import com.example.aiexpensetracker.data.repository.ExpenseRepositoryImpl
import com.example.aiexpensetracker.domain.model.query_result.QueryResult
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class ProcessQueryUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepositoryImpl
) {

    suspend operator fun invoke(query: String): QueryResult {
        val normalizedQuery = query.lowercase().trim()

        return try {
            when {
                // Count-based queries
                containsAny(normalizedQuery, listOf("how many", "count", "number of", "transactions")) -> {
                    handleCountQuery(normalizedQuery)
                }

                // Average-based queries
                containsAny(normalizedQuery, listOf("average", "typical", "usual", "mean")) -> {
                    handleAverageQuery(normalizedQuery)
                }

                // Largest/biggest expense queries
                containsAny(normalizedQuery, listOf("biggest", "largest", "highest", "most expensive", "top")) -> {
                    handleLargestExpensesQuery(normalizedQuery)
                }

                // Statistics and analytics queries
                containsAny(normalizedQuery, listOf("statistics", "stats", "breakdown", "analysis", "report")) -> {
                    handleStatisticsQuery(normalizedQuery)
                }

                // Insights and summary queries
                containsAny(normalizedQuery, listOf("insights", "summary", "overview", "analysis")) -> {
                    handleInsightsQuery()
                }

                // Total spending queries
                containsAny(normalizedQuery, listOf("total", "how much", "spent", "spending")) -> {
                    handleTotalQuery(normalizedQuery)
                }

                // Category-specific queries
                containsAny(normalizedQuery, listOf("food", "transport", "shopping", "entertainment", "bills", "medical")) -> {
                    handleCategoryQuery(normalizedQuery)
                }

                // Time-based queries
                containsAny(normalizedQuery, listOf("last month", "previous month")) -> {
                    handleLastMonthQuery(normalizedQuery)
                }

                containsAny(normalizedQuery, listOf("this month", "current month")) -> {
                    handleThisMonthQuery(normalizedQuery)
                }

                containsAny(normalizedQuery, listOf("this week", "current week")) -> {
                    handleThisWeekQuery(normalizedQuery)
                }

                containsAny(normalizedQuery, listOf("today")) -> {
                    handleTodayQuery(normalizedQuery)
                }

                // Comparison queries
                containsAny(normalizedQuery, listOf("compare", "comparison", "vs", "versus")) -> {
                    handleComparisonQuery(normalizedQuery)
                }

                // Budget and goal queries
                containsAny(normalizedQuery, listOf("budget", "limit", "goal", "target")) -> {
                    handleBudgetQuery(normalizedQuery)
                }

                // Trend queries
                containsAny(normalizedQuery, listOf("trend", "pattern", "increasing", "decreasing")) -> {
                    handleTrendQuery(normalizedQuery)
                }

                else -> {
                    QueryResult(generateHelpMessage())
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing query: $query")
            QueryResult("Sorry, I encountered an error processing your query. Please try again.")
        }
    }

    private suspend fun handleCountQuery(query: String): QueryResult {
        return when {
            query.contains("this month") -> {
                val count = expenseRepository.getExpenseCountCurrentWeek()
                QueryResult("You made $count transactions this month.")
            }

            query.contains("last month") -> {
                val count = expenseRepository.getExpenseCountLastMonth()
                QueryResult("You made $count transactions last month.")
            }

            query.contains("this week") -> {
                val count = expenseRepository.getExpenseCountCurrentWeek()
                QueryResult("You made $count transactions this week.")
            }

            query.contains("food") -> {
                val expenses = expenseRepository.getExpenseByCategory("Food & Dining")
                QueryResult("You made ${expenses.size} food-related transactions.")
            }

            query.contains("transport") -> {
                val expenses = expenseRepository.getExpenseByCategory("Transport")
                QueryResult("You made ${expenses.size} transport-related transactions.")
            }

            query.contains("shopping") -> {
                val expenses = expenseRepository.getExpenseByCategory("Shopping")
                QueryResult("You made ${expenses.size} shopping transactions.")
            }

            else -> {
                val count = expenseRepository.getExpenseCountCurrentMonth()
                QueryResult("You made $count transactions this month.")
            }
        }
    }

    private suspend fun handleAverageQuery(query: String): QueryResult {
        return when {
            query.contains("food") -> {
                val average = expenseRepository.getAverageExpenseByCategory("Food & Dining")
                QueryResult("Your average food expense is ₹${String.format("%.2f", average)} per transaction.")
            }

            query.contains("transport") -> {
                val average = expenseRepository.getAverageExpenseByCategory("Transport")
                QueryResult("Your average transport expense is ₹${String.format("%.2f", average)} per transaction.")
            }

            query.contains("shopping") -> {
                val average = expenseRepository.getAverageExpenseByCategory("Shopping")
                QueryResult("Your average shopping expense is ₹${String.format("%.2f", average)} per transaction.")
            }

            query.contains("entertainment") -> {
                val average = expenseRepository.getAverageExpenseByCategory("Entertainment")
                QueryResult("Your average entertainment expense is ₹${String.format("%.2f", average)} per transaction.")
            }

            else -> {
                val insights = expenseRepository.getSpendingInsights()
                QueryResult("Your average expense per transaction is ₹${String.format("%.2f", insights.averagePerTransaction)}.")
            }
        }
    }

    private suspend fun handleLargestExpensesQuery(query: String): QueryResult {
        val limit = extractNumber(query) ?: when {
            query.contains("top 3") || query.contains("biggest 3") -> 3
            query.contains("top 5") || query.contains("biggest 5") -> 5
            query.contains("top 10") || query.contains("biggest 10") -> 10
            else -> 5
        }

        val biggestExpenses = expenseRepository.getBiggestExpenses(limit = limit.toInt())

        if (biggestExpenses.isEmpty()) {
            return QueryResult("No expenses found.")
        }

        val expenseList = biggestExpenses.mapIndexed { index, expense ->
            "${index + 1}. ₹${String.format("%.2f", expense.amount)} - ${expense.description} (${expense.category})"
        }.joinToString("\n")

        return QueryResult(
            answer = "Your top $limit expenses:\n$expenseList",
            data = biggestExpenses
        )
    }

    private suspend fun handleStatisticsQuery(query: String): QueryResult {
        val categoryStats = expenseRepository.getCategoryStatistics()

        if (categoryStats.isEmpty()) {
            return QueryResult("No expense statistics available.")
        }

        val statsText = buildString {
            appendLine("📊 Your spending statistics:")
            appendLine()
            categoryStats.take(5).forEach { stat ->
                appendLine("💰 ${stat.category}:")
                appendLine("   Total: ₹${String.format("%.2f", stat.totalAmount)}")
                appendLine("   Transactions: ${stat.transactionCount}")
                appendLine("   Average: ₹${String.format("%.2f", stat.averageAmount)}")
                appendLine()
            }
        }

        return QueryResult(statsText.trim())
    }

    private suspend fun handleInsightsQuery(): QueryResult {
        val insights = expenseRepository.getSpendingInsights()

        val changeText = when {
            insights.monthlyChange > 0 -> "increased by ${String.format("%.1f", insights.monthlyChange)}%"
            insights.monthlyChange < 0 -> "decreased by ${String.format("%.1f", kotlin.math.abs(insights.monthlyChange))}%"
            else -> "remained the same"
        }

        val topExpense = insights.biggestExpenses.firstOrNull()
        val topCategory = insights.topCategories.firstOrNull()

        val insightText = buildString {
            appendLine("📈 Your Monthly Insights:")
            appendLine()
            appendLine("💰 This month: ₹${String.format("%.2f", insights.currentMonthTotal)}")
            appendLine("📊 Spending has $changeText compared to last month")
            appendLine("🔢 ${insights.transactionCount} transactions this month")
            appendLine("📱 Average per transaction: ₹${String.format("%.2f", insights.averagePerTransaction)}")
            appendLine()

            if (topExpense != null) {
                appendLine("🏆 Biggest expense: ₹${String.format("%.2f", topExpense.amount)} - ${topExpense.description}")
            }

            if (topCategory != null) {
                appendLine("🎯 Top spending category: ${topCategory.first} (₹${String.format("%.2f", topCategory.second)})")
            }

            // Smart recommendations
            appendLine()
            appendLine("💡 Smart Recommendations:")
            when {
                insights.monthlyChange > 20 -> {
                    appendLine("⚠️ Consider reviewing your budget as spending increased significantly.")
                }
                insights.monthlyChange < -20 -> {
                    appendLine("🎉 Great job on reducing your expenses!")
                }
                insights.averagePerTransaction > 500 -> {
                    appendLine("💭 Your average transaction is quite high. Consider smaller, frequent purchases.")
                }
            }
        }

        return QueryResult(insightText.trim())
    }

    private suspend fun handleTotalQuery(query: String): QueryResult {
        return when {
            query.contains("last month") -> {
                val total = expenseRepository.getTotalLastMonth()
                val count = expenseRepository.getExpenseCountLastMonth()
                QueryResult("Last month you spent ₹${String.format("%.2f", total)} across $count transactions.")
            }

            query.contains("this month") -> {
                val total = expenseRepository.getTotalCurrentMonth()
                val count = expenseRepository.getExpenseCountCurrentMonth()
                QueryResult("This month you've spent ₹${String.format("%.2f", total)} across $count transactions.")
            }

            query.contains("this week") -> {
                val expenses = expenseRepository.getExpensesByCurrentWeek()
                val total = expenses.sumOf { it.amount }
                QueryResult("This week you've spent ₹${String.format("%.2f", total)} across ${expenses.size} transactions.")
            }

            else -> {
                val total = expenseRepository.getTotalAmount()
                QueryResult("Your total spending is ₹${String.format("%.2f", total)} across all transactions.")
            }
        }
    }

    private suspend fun handleCategoryQuery(query: String): QueryResult {
        val category = extractCategoryFromQuery(query)
        val expenses = expenseRepository.getExpenseByCategory(category)
        val total = expenses.sumOf { it.amount }
        val average = expenseRepository.getAverageExpenseByCategory(category)

        return if (expenses.isEmpty()) {
            QueryResult("No expenses found for $category category.")
        } else {
            val recentExpenses = expenses.take(3)
            val expenseList = recentExpenses.joinToString("\n") { expense ->
                "• ₹${String.format("%.2f", expense.amount)} - ${expense.description}"
            }

            val responseText = buildString {
                appendLine("💰 $category Spending:")
                appendLine("Total: ₹${String.format("%.2f", total)}")
                appendLine("Transactions: ${expenses.size}")
                appendLine("Average: ₹${String.format("%.2f", average)}")
                appendLine()
                appendLine("Recent transactions:")
                appendLine(expenseList)
                if (expenses.size > 3) {
                    appendLine("... and ${expenses.size - 3} more")
                }
            }

            QueryResult(responseText.trim(), expenses)
        }
    }

    private suspend fun handleLastMonthQuery(query: String): QueryResult {
        val expenses = expenseRepository.getExpensesByLastMonth()
        val total = expenses.sumOf { it.amount }

        return when {
            query.contains("food") -> {
                val foodExpenses = expenses.filter { it.category.contains("Food", ignoreCase = true) }
                val foodTotal = foodExpenses.sumOf { it.amount }
                QueryResult("Last month you spent ₹${String.format("%.2f", foodTotal)} on food across ${foodExpenses.size} transactions.")
            }

            else -> {
                QueryResult("Last month you spent ₹${String.format("%.2f", total)} across ${expenses.size} transactions.")
            }
        }
    }

    private suspend fun handleThisMonthQuery(query: String): QueryResult {
        val expenses = expenseRepository.getExpenseByCurrentMonth()
        val total = expenses.sumOf { it.amount }

        return when {
            query.contains("food") -> {
                val foodExpenses = expenses.filter { it.category.contains("Food", ignoreCase = true) }
                val foodTotal = foodExpenses.sumOf { it.amount }
                QueryResult("This month you've spent ₹${String.format("%.2f", foodTotal)} on food across ${foodExpenses.size} transactions.")
            }

            else -> {
                QueryResult("This month you've spent ₹${String.format("%.2f", total)} across ${expenses.size} transactions.")
            }
        }
    }

    private suspend fun handleThisWeekQuery(query: String): QueryResult {
        val expenses = expenseRepository.getExpensesByCurrentWeek()
        val total = expenses.sumOf { it.amount }

        return QueryResult("This week you've spent ₹${String.format("%.2f", total)} across ${expenses.size} transactions.")
    }

    private suspend fun handleTodayQuery(query: String): QueryResult {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val expenses = expenseRepository.getExpenseByDateRange(startOfDay, endOfDay)
        val total = expenses.sumOf { it.amount }

        return if (expenses.isEmpty()) {
            QueryResult("You haven't made any expenses today.")
        } else {
            val expenseList = expenses.joinToString("\n") { expense ->
                "• ₹${String.format("%.2f", expense.amount)} - ${expense.description}"
            }
            QueryResult("Today you've spent ₹${String.format("%.2f", total)}:\n$expenseList")
        }
    }

    private suspend fun handleComparisonQuery(query: String): QueryResult {
        val currentMonthTotal = expenseRepository.getTotalCurrentMonth()
        val lastMonthTotal = expenseRepository.getTotalLastMonth()

        val difference = currentMonthTotal - lastMonthTotal
        val percentageChange = if (lastMonthTotal > 0) {
            (difference / lastMonthTotal) * 100
        } else 0.0

        val comparisonText = when {
            difference > 0 -> "increased by ₹${String.format("%.2f", difference)} (${String.format("%.1f", percentageChange)}%)"
            difference < 0 -> "decreased by ₹${String.format("%.2f", kotlin.math.abs(difference))} (${String.format("%.1f", kotlin.math.abs(percentageChange))}%)"
            else -> "remained the same"
        }

        return QueryResult(
            "📊 Monthly Comparison:\n" +
                    "This month: ₹${String.format("%.2f", currentMonthTotal)}\n" +
                    "Last month: ₹${String.format("%.2f", lastMonthTotal)}\n" +
                    "Your spending has $comparisonText compared to last month."
        )
    }

    private suspend fun handleBudgetQuery(query: String): QueryResult {
        val currentMonthTotal = expenseRepository.getTotalCurrentMonth()

        // Extract budget amount from query if mentioned
        val mentionedBudget = extractNumber(query)

        return if (mentionedBudget != null) {
            val remaining = mentionedBudget - currentMonthTotal
            val percentageUsed = (currentMonthTotal / mentionedBudget) * 100

            when {
                remaining > 0 -> {
                    QueryResult("💰 Budget Status:\nBudget: ₹${String.format("%.2f", mentionedBudget)}\nSpent: ₹${String.format("%.2f", currentMonthTotal)} (${String.format("%.1f", percentageUsed)}%)\nRemaining: ₹${String.format("%.2f", remaining)}")
                }
                else -> {
                    val overspent = kotlin.math.abs(remaining)
                    QueryResult("⚠️ Budget Status:\nBudget: ₹${String.format("%.2f", mentionedBudget)}\nSpent: ₹${String.format("%.2f", currentMonthTotal)}\nOver budget by: ₹${String.format("%.2f", overspent)}")
                }
            }
        } else {
            QueryResult("💡 To track your budget, you can say something like 'My budget is 5000 rupees' or 'How much of my 3000 rupee budget have I used?'")
        }
    }

    private suspend fun handleTrendQuery(query: String): QueryResult {
        val insights = expenseRepository.getSpendingInsights()
        val categoryStats = expenseRepository.getCategoryStatistics().take(3)

        val trendText = buildString {
            appendLine("📈 Spending Trends:")
            appendLine()

            when {
                insights.monthlyChange > 10 -> {
                    appendLine("📊 Upward trend: Spending increased by ${String.format("%.1f", insights.monthlyChange)}%")
                }
                insights.monthlyChange < -10 -> {
                    appendLine("📉 Downward trend: Spending decreased by ${String.format("%.1f", kotlin.math.abs(insights.monthlyChange))}%")
                }
                else -> {
                    appendLine("➡️ Stable trend: Spending is relatively consistent")
                }
            }

            appendLine()
            appendLine("Top spending categories:")
            categoryStats.forEach { stat ->
                appendLine("• ${stat.category}: ₹${String.format("%.2f", stat.totalAmount)} (${stat.transactionCount} transactions)")
            }
        }

        return QueryResult(trendText.trim())
    }

    // Helper functions
    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it, ignoreCase = true) }
    }

    private fun extractCategoryFromQuery(query: String): String {
        return when {
            query.contains("food") -> "Food & Dining"
            query.contains("transport") -> "Transport"
            query.contains("shopping") -> "Shopping"
            query.contains("entertainment") -> "Entertainment"
            query.contains("bills") -> "Bills & Utilities"
            query.contains("medical") -> "Health & Medical"
            query.contains("education") -> "Education"
            query.contains("personal") -> "Personal Care"
            query.contains("gifts") -> "Gifts & Donations"
            else -> "Miscellaneous"
        }
    }

    private fun extractNumber(text: String): Double? {
        val numberPattern = Regex("""\d+(?:\.\d+)?""")
        return numberPattern.find(text)?.value?.toDoubleOrNull()
    }

    private fun generateHelpMessage(): String {
        return """
        I can help you with various expense queries! Try asking:
        
        📊 Totals & Spending:
        • "How much did I spend this month?"
        • "What's my total spending?"
        
        🔢 Counts & Statistics:
        • "How many transactions this week?"
        • "Show me spending statistics"
        
        📈 Insights & Analysis:
        • "Give me spending insights"
        • "What are my spending trends?"
        
        🏆 Top Expenses:
        • "Show me my biggest expenses"
        • "What are my top 5 expenses?"
        
        📂 Category Queries:
        • "How much did I spend on food?"
        • "Show me transport expenses"
        
        📅 Time-based Queries:
        • "Last month expenses"
        • "This week's spending"
        
        💰 Averages:
        • "What's my average food expense?"
        • "Average spending per transaction"
        """.trimIndent()
    }
}
