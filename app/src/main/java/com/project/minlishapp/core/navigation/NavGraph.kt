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
<<<<<<< HEAD
import com.project.minlishapp.presentation.vocabulary.components.CardListScreen
import com.project.minlishapp.presentation.vocabulary.components.CardManagementScreen
import com.project.minlishapp.presentation.vocabulary.components.DeckManagementScreen
=======
import com.project.minlishapp.presentation.flashcard.FlashcardScreen
import com.project.minlishapp.presentation.practice.QuizScreen
import com.project.minlishapp.presentation.dashboard.DashboardScreen
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val DEBUG_START_ON_PRACTICE = true
>>>>>>> 25066cdf46e3d5d1b7618a493510347e3c9bf22e

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = if (DEBUG_START_ON_PRACTICE) {
        Screen.Practice.createRoute("debug_deck")
    } else {
        Screen.Register.route
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... (Login, Register, Dashboard)
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
<<<<<<< HEAD
                    navController.navigate(Screen.DeckList.route) {
=======
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.Main.route) {
>>>>>>> 25066cdf46e3d5d1b7618a493510347e3c9bf22e
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
<<<<<<< HEAD
                    navController.navigate(Screen.DeckList.route) {
=======
                    navController.navigate(Screen.Main.route) {
>>>>>>> 25066cdf46e3d5d1b7618a493510347e3c9bf22e
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Main.route) {
            com.project.minlishapp.presentation.main.MainScreen(navController)
        }

        composable(Screen.DeckList.route) {
            DeckManagementScreen(
                onImportExportClick = { /* Handled inside */ },
                onDeckClick = { deckId ->
                    navController.navigate(Screen.CardList.createRoute(deckId))
                },
                onAddCardClick = { deckId ->
                    navController.navigate(Screen.AddCard.createRoute(deckId))
                }
            )
        }

        composable(
            route = Screen.CardList.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
<<<<<<< HEAD
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            CardListScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onAddCardClick = { id ->
                    navController.navigate(Screen.AddCard.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.AddCard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            CardManagementScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() }
=======
        ) {
            val deckId = it.arguments?.getString("deckId") ?: ""
            com.project.minlishapp.presentation.vocabulary.components.CardManagementScreen(
                deckId = deckId,
                onBack = { navController.popBackStack() },
                viewModel = hiltViewModel()
>>>>>>> 25066cdf46e3d5d1b7618a493510347e3c9bf22e
            )
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
                onNavigateToDashboard = { navController.navigate(Screen.Main.route) },
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

@Composable
fun MainNavGraph(
    navController: NavHostController,
    rootNavController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainDashboard.route,
        modifier = modifier
    ) {
        composable(Screen.MainDashboard.route) {
            DashboardScreen()
        }
        
        composable(Screen.MainDecks.route) {
            com.project.minlishapp.presentation.vocabulary.components.DeckManagementScreen(
                onImportExportClick = { /* TODO */ },
                onDeckClick = { deckId ->
                    rootNavController.navigate(Screen.CardList.createRoute(deckId))
                },
                onAddCardClick = { deckId ->
                    rootNavController.navigate(Screen.CardList.createRoute(deckId))
                }
            )
        }
        
        composable(Screen.MainPractice.route) {
            com.project.minlishapp.presentation.practice.QuizScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.MainDashboard.route) {
                        popUpTo(Screen.MainDashboard.route)
                    }
                }
            )
        }
        
        composable(Screen.MainProfile.route) {
            com.project.minlishapp.presentation.profile.ProfileScreen(
                onLogoutClick = {
                    rootNavController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
