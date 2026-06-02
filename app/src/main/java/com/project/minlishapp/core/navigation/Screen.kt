package com.project.minlishapp.core.navigation

sealed class Screen(val route: String, val title: String? = null) {
    // --- Authentication Screens ---
    object Login : Screen("login")
    object Register : Screen("register")
    object AuthLogin : Screen("auth_login")
    object AuthRegister : Screen("auth_register")
    object AuthLearningGoal : Screen("auth_learning_goal")
    object AuthLevelSelection : Screen("auth_level_selection")
    object AuthProfileSetup : Screen("auth_profile_setup")

    // --- Dashboard & Main Screens ---
    object Main : Screen("main")
    object Dashboard : Screen("dashboard")
    object MainDashboard : Screen("main_dashboard", "Home")
    object MainDecks : Screen("main_decks", "Decks")
    object MainPractice : Screen("main_practice", "Practice")
    object MainProfile : Screen("main_profile", "Profile")

    // --- Learning & Deck Modules Screens ---
    object DeckList : Screen("deck_list")

    object CardList : Screen("card_list/{deckId}") {
        fun createRoute(deckId: String) = "card_list/$deckId"
    }

    object FlashcardLearning : Screen("flashcard_learning/{deckId}") {
        fun createRoute(deckId: String) = "flashcard_learning/$deckId"
        fun createSpacedRepetitionRoute() = createRoute(SPACED_REPETITION_DECK_ID)

        const val SPACED_REPETITION_DECK_ID = "__spaced_repetition__"
    }

    object Practice : Screen("practice/{deckId}") {
        fun createRoute(deckId: String) = "practice/$deckId"
    }

    companion object {
        const val ROOT_GRAPH = "root"
    }
}
