package com.example.aiexpensetracker.ai

import com.example.aiexpensetracker.di.DispatcherModule
import com.example.aiexpensetracker.domain.model.ai_processing_state.AIProcessingResult
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.domain.repository.ExpenseRepository
import com.example.aiexpensetracker.domain.usecase.AddCategoryUseCase
import com.example.aiexpensetracker.domain.usecase.CategorizeExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ExtractExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ProcessQueryUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseAIAssistant @Inject constructor(
    private val extractExpenseUseCase: ExtractExpenseUseCase,
    private val categorizeExpenseUseCase: CategorizeExpenseUseCase,
    private val processQueryUseCase: ProcessQueryUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    @DispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun processInput(input: String): AIProcessingResult = withContext(ioDispatcher) {
        val normalizedInput = input.trim().lowercase()

        return@withContext when {

            isAddCategoryRequest(normalizedInput) -> {
                handleAddCategory(input)
            }

            isExpenseInput(normalizedInput) -> {
                handleAddExpense(input)
            }

            else -> {
                handleQuery(input)
            }
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
        val expensePatterns = listOf(
            "bought", "purchased", "paid", "spent", "rupees", "rs", "₹"
        )
        return expensePatterns.any { pattern ->
            input.contains(pattern, ignoreCase = true)
        } || input.matches(Regex(""".*\d+.*""")) // Contains numbers
    }

    private suspend fun handleAddCategory(input: String): AIProcessingResult {
        return if (addCategoryUseCase(input)) {
            val categoryName = extractCategoryName(input)
            AIProcessingResult.CategoryAdded(categoryName ?: "New Category")
        } else {
            AIProcessingResult.Error("Failed to add category. Please try again.")
        }
    }

    private suspend fun handleAddExpense(input: String): AIProcessingResult {
        try {
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

            return AIProcessingResult.ExpenseAdded(finalExpense.copy(id = savedId))

        } catch (e: Exception) {
            return AIProcessingResult.Error("Failed to process expense: ${e.message}")
        }
    }

    private suspend fun handleQuery(input: String): AIProcessingResult {
        return try {
            val queryResult = processQueryUseCase(input)
            AIProcessingResult.QueryAnswer(queryResult.answer, queryResult.data)
        } catch (e: Exception) {
            AIProcessingResult.Error("Sorry, I couldn't process your query. Please try again.")
        }
    }

    private fun extractCategoryName(input: String): String? {
        val patterns = listOf(
            Regex("""add\s+(\w+)\s+to\s+the?\s+category""", RegexOption.IGNORE_CASE),
            Regex("""add\s+(\w+)\s+category""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return match.groupValues[1].capitalize()
            }
        }
        return null
    }
}