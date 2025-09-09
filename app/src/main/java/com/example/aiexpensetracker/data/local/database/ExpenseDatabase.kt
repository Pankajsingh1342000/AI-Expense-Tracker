package com.example.aiexpensetracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.aiexpensetracker.data.local.dao.CategoryDao
import com.example.aiexpensetracker.data.local.dao.ExpenseDao
import com.example.aiexpensetracker.data.local.entities.CategoryEntity
import com.example.aiexpensetracker.data.local.entities.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
}