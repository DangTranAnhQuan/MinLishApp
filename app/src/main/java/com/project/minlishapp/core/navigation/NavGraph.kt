package com.project.minlishapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.project.minlishapp.presentation.auth.LoginScreen
import com.project.minlishapp.presentation.auth.RegisterScreen
import com.project.minlishapp.presentation.flashcard.FlashcardScreen
import com.project.minlishapp.presentation.practice.QuizScreen
// Toggle this constant to `true` locally when you want the app to start directly
// on the Practice screen for Module 4 manual testing. Keep `false` for normal runs.
private const val DEBUG_START_ON_PRACTICE = true

@Composable
fun NavGraph(
    navController: NavHostController,
    // For local debugging: start directly on Practice when DEBUG build to verify Module 4 UI/flow.
    startDestination: String = if (DEBUG_START_ON_PRACTICE) {
        // sample deck id used only for debug runs
        Screen.Practice.createRoute("debug_deck")
    } else {
        Screen.Register.route
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Dashboard.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Dashboard Screen")
            }
        }

        composable(Screen.DeckList.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Deck List Screen")
            }
        }

        composable(
            route = Screen.CardList.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Card List Screen")
            }
        }

        composable(
            route = Screen.FlashcardLearning.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) {
            FlashcardScreen(
                onBack = { navController.navigateBackOrToRegister() },
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.Practice.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) {
            QuizScreen(
                onBack = { navController.navigateBackOrToRegister() },
                viewModel = hiltViewModel()
            )
        }
    }
}

private fun NavHostController.navigateBackOrToRegister() {
    if (!popBackStack()) {
        navigate(Screen.Register.route) {
            popUpTo(graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
}
