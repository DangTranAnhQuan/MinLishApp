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
// Toggle this constant to `true` locally when you want the app to start directly
// on the Flashcard screen for Module 3 manual testing. Keep `false` for normal runs.
private const val DEBUG_START_ON_FLASHCARD = true

@Composable
fun NavGraph(
    navController: NavHostController,
    // For local debugging: start directly on FlashcardLearning when DEBUG build to verify Module 3 UI/flow.
    startDestination: String = if (DEBUG_START_ON_FLASHCARD) {
        // sample deck id used only for debug runs
        Screen.FlashcardLearning.createRoute("debug_deck")
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
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
            )
        }
    }
}
