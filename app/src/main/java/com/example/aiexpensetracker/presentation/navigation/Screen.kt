package com.example.aiexpensetracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aiexpensetracker.presentation.screens.chat.ChatScreen
import com.example.aiexpensetracker.presentation.screens.expenses.ExpensesScreen
import com.example.aiexpensetracker.presentation.screens.home.HomeScreen
import com.example.aiexpensetracker.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home: Screen("home")
    object Chat: Screen("screen")
    object Expenses: Screen("expenses")
    object Settings: Screen("settings")
}

@Composable
fun ExpenseNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToExpenses = { navController.navigate(Screen.Expenses.route) }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Expenses.route) {
            ExpensesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}