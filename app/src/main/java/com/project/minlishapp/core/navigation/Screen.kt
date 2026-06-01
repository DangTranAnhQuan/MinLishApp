package com.project.minlishapp.core.navigation

sealed class Screen(val route: String, val title: String? = null) {
    object AuthLogin : Screen("auth_login")
    object AuthRegister : Screen("auth_register")
    object AuthLearningGoal : Screen("auth_learning_goal")
    object AuthLevelSelection : Screen("auth_level_selection")
    object AuthProfileSetup : Screen("auth_profile_setup")
    
    object MainDashboard : Screen("main_dashboard", "Home")
    object MainDecks : Screen("main_decks", "Decks")
    object MainPractice : Screen("main_practice", "Practice")
    object MainProfile : Screen("main_profile", "Profile")
    
    object Main : Screen("main")
}
