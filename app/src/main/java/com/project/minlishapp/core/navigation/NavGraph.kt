package com.project.minlishapp.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private const val DEBUG_START_ON_PRACTICE = false

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
            DashboardScreen(
                onSelectDeck = { deckId ->
                    navController.navigate(Screen.FlashcardLearning.createRoute(deckId))
                },
                onLogout = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Graph.ROOT) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DeckList.route) {
            DeckListScreen(
                onSelectDeck = { deckId ->
                    navController.navigate(Screen.CardList.createRoute(deckId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CardList.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            CardListScreen(
                deckId = deckId,
                onPractice = { selectedDeckId ->
                    navController.navigate(Screen.Practice.createRoute(selectedDeckId))
                },
                onLearn = { selectedDeckId ->
                    navController.navigate(Screen.FlashcardLearning.createRoute(selectedDeckId))
                },
                onBack = { navController.popBackStack() }
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
                viewModel = hiltViewModel()
            )
        }
    }
}

// Screen Implementations

@Composable
private fun DashboardScreen(
    onSelectDeck: (String) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            DashboardHeader(onLogout = onLogout)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Xin chào!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Chọn bộ từ để bắt đầu học",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Sample Decks
            val sampleDecks = listOf(
                "Bộ từ vựng IELTS" to "ielts_deck",
                "Bộ từ vựng TOEIC" to "toeic_deck",
                "Từ vựng giao tiếp" to "conversation_deck",
                "Từ vựng công vụ" to "business_deck"
            )

            sampleDecks.forEach { (deckName, deckId) ->
                DeckCard(
                    name = deckName,
                    onClick = { onSelectDeck(deckId) }
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        Text(
            text = "MinLish",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        TextButton(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text("Đăng xuất")
        }
    }
}

@Composable
private fun DeckListScreen(
    onSelectDeck: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DeckListHeader(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val decks = listOf(
                "Bộ từ vựng IELTS" to "ielts_deck",
                "Bộ từ vựng TOEIC" to "toeic_deck",
                "Từ vựng giao tiếp" to "conversation_deck",
                "Từ vựng công vụ" to "business_deck",
                "Bộ từ vựng thường dùng" to "common_deck"
            )

            decks.forEach { (deckName, deckId) ->
                DeckCard(
                    name = deckName,
                    onClick = { onSelectDeck(deckId) }
                )
            }
        }
    }
}

@Composable
private fun DeckListHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Các bộ từ vựng",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun CardListScreen(
    deckId: String,
    onPractice: (String) -> Unit,
    onLearn: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CardListHeader(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bộ từ vựng",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "ID: $deckId",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.padding(8.dp))

            // Action Buttons
            Button(
                onClick = { onLearn(deckId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Học từ vựng")
            }

            Button(
                onClick = { onPractice(deckId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Luyện tập")
            }
        }
    }
}

@Composable
private fun CardListHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text(
            text = "Chi tiết bộ từ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun DeckCard(
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Nhấp để xem chi tiết",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

object Graph {
    const val ROOT = "root_graph"
}

private fun NavHostController.navigateBackOrToRegister() {
    if (!popBackStack()) {
        navigate(Screen.Register.route) {
            popUpTo(graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
}
