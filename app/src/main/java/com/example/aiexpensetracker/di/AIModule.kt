package com.example.aiexpensetracker.di

import android.content.Context
import com.example.aiexpensetracker.ai.speech.VoiceRecognitionService
import com.example.aiexpensetracker.domain.repository.CategoryRepository
import com.example.aiexpensetracker.domain.usecase.AddCategoryUseCase
import com.example.aiexpensetracker.domain.usecase.CategorizeExpenseUseCase
import com.example.aiexpensetracker.domain.usecase.ExtractExpenseUseCase
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
    fun provideExtractExpenseUseCase(): ExtractExpenseUseCase = ExtractExpenseUseCase()

    @Provides
    @Singleton
    fun provideCategorizeExpenseUseCase(): CategorizeExpenseUseCase = CategorizeExpenseUseCase()

    @Provides
    @Singleton
    fun provideAddCategoryUseCase(
        categoryRepository: CategoryRepository
    ): AddCategoryUseCase = AddCategoryUseCase(categoryRepository)
}