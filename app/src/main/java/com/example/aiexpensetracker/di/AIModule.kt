package com.example.aiexpensetracker.di

import android.content.Context
import com.example.aiexpensetracker.ai.speech.VoiceRecognitionService
import com.example.aiexpensetracker.domain.repository.BudgetRepository
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.domain.usecase.AddCategoryUseCase
import com.example.aiexpensetracker.domain.usecase.BudgetUseCase
import com.example.aiexpensetracker.domain.usecase.CategorizeExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ExpenseDetectionUseCase
import com.example.aiexpensetracker.domain.usecase.ExtractExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.SmartAmountExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideVoiceRecognitionService(
        @ApplicationContext context: Context
    ): VoiceRecognitionService = VoiceRecognitionService(context)

    @Provides
    @Singleton
    fun provideSmartAmountExtractor(): SmartAmountExtractor = SmartAmountExtractor()

    @Provides
    @Singleton
    fun provideExtractExpenseUseCase(
        smartAmountExtractor: SmartAmountExtractor
    ): ExtractExpenseUseCase = ExtractExpenseUseCase(smartAmountExtractor)

    @Provides
    @Singleton
    fun provideExpenseDetectionUseCase(): ExpenseDetectionUseCase = ExpenseDetectionUseCase()
    
    @Provides
    @Singleton
    fun provideCategorizeExpenseUseCase(): CategorizeExpenseUseCase = CategorizeExpenseUseCase()

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(
        categoryRepository: CategoryRepository
    ): AddCategoryUseCase = AddCategoryUseCase(categoryRepository)

    @Provides
    @Singleton
    fun provideBudgetUseCase(
        budgetRepository: BudgetRepository
    ): BudgetUseCase = BudgetUseCase(budgetRepository)
}