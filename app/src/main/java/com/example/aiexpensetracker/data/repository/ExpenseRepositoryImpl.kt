package com.example.aiexpensetracker.data.repository

import com.example.aiexpensetracker.data.local.db.dao.ExpenseDao
import com.example.aiexpensetracker.data.local.db.entities.toDomain
import com.example.aiexpensetracker.data.local.db.entities.toEntity
import com.example.aiexpensetracker.di.DispatcherModule.IoDispatcher
import com.example.aiexpensetracker.domain.model.category.CategoryStatistic
import com.example.aiexpensetracker.domain.model.expense.Expense
import com.example.aiexpensetracker.domain.model.expense.SpendingInsights
import com.example.aiexpensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
): ExpenseRepository {
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun insertExpense(expense: Expense): Long = withContext(ioDispatcher) {
        try {
            val entity = expense.toEntity()
            expenseDao.insertExpense(entity)
        } catch (e: Exception) {
            Timber.e(e, "Error inserting expense: ${expense.description}")
            throw e
        }
    }

    override suspend fun deleteExpense(expense: Expense) = withContext(ioDispatcher) {
        try {
            val entity = expense.toEntity()
            expenseDao.deleteExpense(entity)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting expense: ${expense.description}")
            throw e
        }
    }

    override suspend fun getExpenseByCategory(category: String): List<Expense> = withContext(ioDispatcher) {
        try {
            expenseDao.getExpensesByCategory(category)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting expenses by category: $category")
            emptyList()
        }
    }

    override suspend fun getExpenseByDateRange(
        startDate: Long,
        endDate: Long,
    ): List<Expense> = withContext(ioDispatcher) {
        try {
            expenseDao.getExpensesByDateRange(startDate, endDate)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting expenses by date range: $startDate to $endDate")
            emptyList()
        }
    }

    override suspend fun getTotalAmount(): Double = withContext(ioDispatcher) {
        try {
            expenseDao.getTotalAmount() ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting total amount")
            0.0
        }
    }

    override suspend fun getExpenseByCurrentMonth(): List<Expense> = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        try {
            expenseDao.getExpensesByMonth(startOfMonth = startOfMonth, endOfMonth = endOfMonth)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current month expense")
            emptyList()
        }
    }

    override suspend fun getExpensesByLastMonth(): List<Expense> = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getLastMonthRange()
        try {
            expenseDao.getExpensesByMonth(startOfMonth, endOfMonth)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting last month expenses")
            emptyList()
        }
    }

    override suspend fun getExpensesByCurrentWeek(): List<Expense> = withContext(ioDispatcher) {
        val (startOfWeek, endOfWeek) = getCurrentWeekRange()
        try {
            expenseDao.getExpensesByWeek(startOfWeek, endOfWeek)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current week expenses")
            emptyList()
        }
    }

    override suspend fun getTotalByCategory(category: String): Double = withContext(ioDispatcher) {
        try {
            expenseDao.getTotalByCategory(category) ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting total by category: $category")
            0.0
        }
    }

    override suspend fun getTopCategories(limit: Int): List<Pair<String, Double>> = withContext(ioDispatcher) {
        try {
            expenseDao.getTopCategories(limit)
                .map { it.category to it.total }
        } catch (e: Exception) {
            Timber.e(e, "Error getting top categories")
            emptyList()
        }
    }

    override suspend fun getTotalCurrentMonth(): Double = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        try {
            expenseDao.getTotalByDateRange(startOfMonth, endOfMonth) ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting current month total")
            0.0
        }
    }

    override suspend fun getTotalLastMonth(): Double = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getLastMonthRange()
        try {
            expenseDao.getTotalByDateRange(startOfMonth, endOfMonth) ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting last month total")
            0.0
        }
    }

    override suspend fun getExpenseCountCurrentMonth(): Int = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getCurrentMonthRange()
        try {
            expenseDao.getExpenseCountByDateRange(startOfMonth, endOfMonth)
        } catch (e: Exception) {
            Timber.e(e, "Error getting current month expense count")
            0
        }
    }

    override suspend fun getExpenseCountLastMonth(): Int = withContext(ioDispatcher) {
        val (startOfMonth, endOfMonth) = getLastMonthRange()
        try {
            expenseDao.getExpenseCountByDateRange(startOfMonth, endOfMonth)
        } catch (e: Exception) {
            Timber.e(e, "Error getting last month expense count")
            0
        }
    }

    override suspend fun getExpenseCountCurrentWeek(): Int = withContext(ioDispatcher) {
        val (startOfWeek, endOfWeek) = getCurrentWeekRange()
        try {
            expenseDao.getExpenseCountByDateRange(startOfWeek, endOfWeek)
        } catch (e: Exception) {
            Timber.e(e, "Error getting current week expense count")
            0
        }
    }

    override suspend fun getAverageExpenseByCategory(category: String): Double = withContext(ioDispatcher) {
        try {
            expenseDao.getAverageAmountByCategory(category) ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting average expense for category: $category")
            0.0
        }
    }

    override suspend fun getAllCategoriesWithAverages(): List<Pair<String, Double>> = withContext(ioDispatcher) {
        try {
            val categoryStats = expenseDao.getExpenseCountByCategory()
            categoryStats.mapNotNull { stat ->
                val average = expenseDao.getAverageAmountByCategory(stat.category)
                if (average != null) {
                    stat.category to average
                } else null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting category averages")
            emptyList()
        }
    }

    override suspend fun getBiggestExpenses(limit: Int): List<Expense> = withContext(ioDispatcher) {
        try {
            expenseDao.getLargestExpenses(limit = limit)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting biggest expenses")
            emptyList()
        }
    }

    override suspend fun getLargeExpensesAboveAmount(
        amount: Double,
        limit: Int,
    ): List<Expense> = withContext(ioDispatcher) {
        try {
            expenseDao.getLargestExpenses(minAmount = amount, limit = limit)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting large expenses above $amount")
            emptyList()
        }
    }

    override suspend fun getCategoryStatistics(): List<CategoryStatistic> = withContext(ioDispatcher) {
        try {
            val categoryCounts = expenseDao.getExpenseCountByCategory()
            val categoryTotals = expenseDao.getTopCategories(50)

            categoryCounts.map { countData ->
                val totalData = categoryTotals.find { it.category == countData.category }
                val average = expenseDao.getAverageAmountByCategory(countData.category) ?: 0.0

                CategoryStatistic(
                    category = countData.category,
                    totalAmount = totalData?.total ?: 0.0,
                    transactionCount = countData.count,
                    averageAmount = average
                )
            }.sortedByDescending { it.totalAmount }
        } catch (e: Exception) {
            Timber.e(e, "Error getting category statistics")
            emptyList()
        }
    }

    override suspend fun getSpendingFrequencyByCategory(): List<Pair<String, Int>> = withContext(ioDispatcher) {
        try {
            expenseDao.getExpenseCountByCategory()
                .map { it.category to it.count }
                .sortedByDescending { it.second }
        } catch (e: Exception) {
            Timber.e(e, "Error getting spending frequency")
            emptyList()
        }
    }

    override suspend fun getSpendingInsights(): SpendingInsights = withContext(ioDispatcher) {
        try {
            val currentMonthTotal = getTotalCurrentMonth()
            val lastMonthTotal = getTotalLastMonth()
            val currentMonthCount = getExpenseCountCurrentMonth()
            val biggestExpenses = getBiggestExpenses(3)
            val topCategories = getTopCategories(3)
            val categoryStats = getCategoryStatistics()

            SpendingInsights(
                currentMonthTotal = currentMonthTotal,
                lastMonthTotal = lastMonthTotal,
                monthlyChange = ((currentMonthTotal - lastMonthTotal) / lastMonthTotal * 100).takeIf { !it.isNaN() } ?: 0.0,
                transactionCount = currentMonthCount,
                averagePerTransaction = if (currentMonthCount > 0) currentMonthTotal / currentMonthCount else 0.0,
                biggestExpenses = biggestExpenses,
                topCategories = topCategories,
                categoryStatistics = categoryStats
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting spending insights")
            SpendingInsights()
        }
    }

    override suspend fun getExpensesBySpecificMonth(month: String): List<Expense> = withContext(ioDispatcher) {
        try {
            val (startOfMonth, endOfMonth) = getSpecificMonthRange(month)
            expenseDao.getExpensesByMonth(startOfMonth, endOfMonth)
                .map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Error getting expenses for month: $month")
            emptyList()
        }
    }

    override suspend fun getTotalBySpecificMonth(month: String): Double = withContext(ioDispatcher) {
        try {
            val (startOfMonth, endOfMonth) = getSpecificMonthRange(month)
            expenseDao.getTotalByDateRange(startOfMonth, endOfMonth) ?: 0.0
        } catch (e: Exception) {
            Timber.e(e, "Error getting total for month: $month")
            0.0
        }
    }

    override suspend fun getExpenseCountBySpecificMonth(month: String): Int = withContext(ioDispatcher) {
        try {
            val (startOfMonth, endOfMonth) = getSpecificMonthRange(month)
            expenseDao.getExpenseCountByDateRange(startOfMonth, endOfMonth)
        } catch (e: Exception) {
            Timber.e(e, "Error getting expense count for month: $month")
            0
        }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val startOfMonth = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return startOfMonth to endOfMonth
    }

    private fun getLastMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val startOfMonth = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return startOfMonth to endOfMonth
    }

    private fun getCurrentWeekRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        // Start of week (Monday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        // End of week (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfWeek = calendar.timeInMillis

        return startOfWeek to endOfWeek
    }

    private fun getSpecificMonthRange(month: String): Pair<Long, Long> {
        return try {
            // Parse month string (format: "yyyy-MM" e.g. "2025-09")
            val parts = month.split("-")
            val year = parts[0].toInt()
            val monthIndex = parts[1].toInt() - 1 // Calendar months are 0-based

            val startOfMonth = Calendar.getInstance().apply {
                set(year, monthIndex, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfMonth = Calendar.getInstance().apply {
                set(year, monthIndex, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            startOfMonth to endOfMonth
        } catch (e: Exception) {
            Timber.e(e, "Error parsing month: $month, falling back to current month")
            getCurrentMonthRange()
        }
    }
}