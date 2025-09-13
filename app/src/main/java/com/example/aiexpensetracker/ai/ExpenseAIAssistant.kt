package com.example.aiexpensetracker.ai

import com.example.aiexpensetracker.di.DispatcherModule
import com.example.aiexpensetracker.domain.model.ai_processing_state.AIProcessingResult
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.domain.repository.ExpenseRepository
import com.example.aiexpensetracker.domain.usecase.AddCategoryUseCase
import com.example.aiexpensetracker.domain.usecase.BudgetUseCase
import com.example.aiexpensetracker.domain.usecase.CategorizeExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ExpenseDetectionUseCase
import com.example.aiexpensetracker.domain.usecase.ExtractExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ProcessQueryUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ExpenseAIAssistant @Inject constructor(
    private val extractExpenseUseCase: ExtractExpenseUseCase,
    private val categorizeExpenseUseCase: CategorizeExpenseUseCase,
    private val processQueryUseCase: ProcessQueryUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val expenseRepository: ExpenseRepository,
    private val expenseDetectionUseCase: ExpenseDetectionUseCase,
    private val categoryRepository: CategoryRepository,
    private val budgetUseCase: BudgetUseCase,
    @DispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun processInput(input: String): AIProcessingResult = withContext(ioDispatcher) {
        val normalizedInput = input.trim().lowercase()

        Timber.d("Processing input: '$input' (normalized: '$normalizedInput')")

        return@withContext when {
            // 1. Check for budget/query requests first (higher priority)
            isBudgetOrQueryRequest(normalizedInput) -> {
                Timber.d("Detected as budget/query request")
                handleQuery(input)
            }

            // 2. Check for category addition
            isAddCategoryRequest(normalizedInput) -> {
                Timber.d("Detected as category addition request")
                handleAddCategory(input)
            }

            // 3. Check for expense input (lowest priority, most specific)
            isExpenseInput(normalizedInput) -> {
                Timber.d("Detected as expense input")
                handleAddExpense(input)
            }

            // 4. Default to query handling
            else -> {
                Timber.d("Defaulting to query processing")
                handleQuery(input)
            }
        }
    }

    private fun isBudgetOrQueryRequest(input: String): Boolean {
        val budgetPatterns = listOf(
            "budget", "limit", "goal", "target",
            "set.*budget", "my budget", "budget.*status",
            "update.*budget", "change.*budget", "modify.*budget",
            "delete.*budget", "remove.*budget", "clear.*budget",
            "how much", "total", "spent", "spending", "statistics", "stats",
            "insights", "summary", "overview", "analysis", "report",
            "average", "count", "transactions", "compare", "comparison",
            "trend", "pattern", "breakdown"
        )

        return budgetPatterns.any { pattern ->
            input.contains(Regex(pattern, RegexOption.IGNORE_CASE))
        }
    }

    private fun isAddCategoryRequest(input: String): Boolean {
        val categoryPatterns = listOf(
            "add.*to.*category",
            "add.*category",
            "create.*category",
            "new category"
        )

        return categoryPatterns.any { pattern ->
            input.contains(Regex(pattern, RegexOption.IGNORE_CASE))
        }
    }

    private fun isExpenseInput(input: String): Boolean {
        return expenseDetectionUseCase.isExpenseInput(input)
    }

    private suspend fun handleAddCategory(input: String): AIProcessingResult {
        return try {
            if (addCategoryUseCase(input)) {
                val categoryName = extractCategoryName(input)
                AIProcessingResult.CategoryAdded(categoryName ?: "New Category")
            } else {
                AIProcessingResult.Error("Failed to add category. Please try again.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error adding category")
            AIProcessingResult.Error("Failed to add category: ${e.message}")
        }
    }

    private suspend fun handleAddExpense(input: String): AIProcessingResult {
        return try {
            // Extract expense from input
            val extractedExpense = extractExpenseUseCase(input)
                ?: return AIProcessingResult.InvalidInput

            // Get categories and categorize
            val categories = categoryRepository.getCategories()
            val category = categorizeExpenseUseCase(extractedExpense.description, categories)

            // Create final expense with category
            val finalExpense = extractedExpense.copy(category = category)

            // Save to repository
            val savedId = expenseRepository.insertExpense(finalExpense)

            Timber.d("Successfully added expense: ${finalExpense.description} - ₹${finalExpense.amount}")
            AIProcessingResult.ExpenseAdded(finalExpense.copy(id = savedId))

        } catch (e: Exception) {
            Timber.e(e, "Error processing expense")
            AIProcessingResult.Error("Failed to process expense: ${e.message}")
        }
    }

    private suspend fun handleQuery(input: String): AIProcessingResult {
        return try {
            val queryResult = processQueryUseCase(input)
            AIProcessingResult.QueryAnswer(queryResult.answer, queryResult.data)
        } catch (e: Exception) {
            Timber.e(e, "Error processing query")
            AIProcessingResult.Error("Sorry, I couldn't process your query. Please try again.")
        }
    }

    private fun extractCategoryName(input: String): String? {
        val patterns = listOf(
            Regex("""add\s+(\w+)\s+to\s+the?\s+category""", RegexOption.IGNORE_CASE),
            Regex("""add\s+(\w+)\s+category""", RegexOption.IGNORE_CASE),
            Regex("""create\s+(\w+)\s+category""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return match.groupValues[1].replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
        }
        return null
    }
}