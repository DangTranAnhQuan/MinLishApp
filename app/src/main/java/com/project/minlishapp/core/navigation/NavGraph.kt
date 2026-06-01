package com.project.minlishapp.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.project.minlishapp.presentation.auth.AuthViewModel
import com.project.minlishapp.presentation.auth.LearningGoalScreen
import com.project.minlishapp.presentation.auth.LevelSelectionScreen
import com.project.minlishapp.presentation.auth.LoginScreen
import com.project.minlishapp.presentation.auth.RegisterScreen
import com.project.minlishapp.presentation.main.MainScreen
import com.project.minlishapp.presentation.profile.ProfileScreen

@Composable
fun RootNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.AuthLogin.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.AuthRegister.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.AuthLogin.route) { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate(Screen.AuthLearningGoal.route) {
                        popUpTo(Screen.AuthLogin.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.AuthRegister.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.AuthLogin.route) {
                        popUpTo(Screen.AuthRegister.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.AuthLearningGoal.route) {
                        popUpTo(Screen.AuthRegister.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.AuthLearningGoal.route) {
            LearningGoalScreen(
                onNavigateNext = {
                    navController.navigate(Screen.AuthLevelSelection.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.AuthLevelSelection.route) {
            LevelSelectionScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                rootNavController = navController
            )
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Home")
            }
        }
        composable(Screen.MainDecks.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Decks")
            }
        }
        composable(Screen.MainPractice.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Practice")
            }
        }
        composable(Screen.MainProfile.route) {
            ProfileScreen(
                onLogoutClick = {
                    rootNavController.navigate(Screen.AuthLogin.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
