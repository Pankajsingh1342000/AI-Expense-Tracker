package com.example.aiexpensetracker.di

import android.content.Context
import com.example.aiexpensetracker.data.local.preferences.BudgetPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideBudgetPreferencesManager(
        @ApplicationContext context: Context
    ): BudgetPreferencesManager = BudgetPreferencesManager(context)
}