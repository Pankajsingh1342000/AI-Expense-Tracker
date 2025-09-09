package com.example.aiexpensetracker.di

import com.example.aiexpensetracker.data.repository.CategoryRepositoryImpl
import com.example.aiexpensetracker.data.repository.ExpenseRepositoryImpl
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.domain.repository.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository
}