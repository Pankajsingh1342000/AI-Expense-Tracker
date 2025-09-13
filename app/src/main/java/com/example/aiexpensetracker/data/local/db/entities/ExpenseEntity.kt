package com.example.aiexpensetracker.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.aiexpensetracker.domain.model.expense.Expense

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Long,
    val createdAt: Long,
)

fun ExpenseEntity.toDomain(): Expense {
    return Expense(id, amount, description, category, date, createdAt)
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(id, amount, description, category, date, createdAt)
}
