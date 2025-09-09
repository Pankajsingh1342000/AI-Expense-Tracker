package com.example.aiexpensetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.aiexpensetracker.data.local.entities.ExpenseEntity
import com.example.aiexpensetracker.domain.model.category.CategoryCount
import com.example.aiexpensetracker.domain.model.category.CategoryTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY createdAt DESC ")
    suspend fun getExpensesByCategory(category: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC ")
    suspend fun getExpensesByDateRange(startDate: Long, endDate: Long): List<ExpenseEntity>

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT SUM(amount) FROM expenses")
    suspend fun getTotalAmount(): Double?

    @Query("""
        SELECT * FROM expenses
        WHERE date >= :startOfMonth AND date <= :endOfMonth
        ORDER BY createdAt DESC
    """)
    suspend fun getExpensesByMonth(startOfMonth: Long, endOfMonth: Long): List<ExpenseEntity>

    @Query("""
        SELECT * FROM expenses
        WHERE date >= :startOfWeek AND date <= :endOfWeek
        ORDER BY createdAt DESC
    """)
    suspend fun getExpensesByWeek(startOfWeek: Long, endOfWeek: Long): List<ExpenseEntity>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    suspend fun getTotalByCategory(category: String): Double?

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getTotalByDateRange(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM expenses 
        GROUP BY category 
        ORDER BY total DESC 
        LIMIT :limit
    """)
    suspend fun getTopCategories(limit: Int): List<CategoryTotal>

    @Query("""
        SELECT COUNT(*) FROM expenses 
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getExpenseCountByDateRange(startDate: Long, endDate: Long): Int

    @Query("""
        SELECT AVG(amount) FROM expenses 
        WHERE category = :category
    """)
    suspend fun getAverageAmountByCategory(category: String): Double?

    @Query("""
        SELECT * FROM expenses 
        WHERE amount >= :minAmount 
        ORDER BY amount DESC 
        LIMIT :limit
    """)
    suspend fun getLargestExpenses(minAmount: Double = 0.0, limit: Int = 10): List<ExpenseEntity>

    @Query("""
        SELECT category, COUNT(*) as count 
        FROM expenses 
        GROUP BY category 
        ORDER BY count DESC
    """)
    suspend fun getExpenseCountByCategory(): List<CategoryCount>
}