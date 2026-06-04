package com.project.minlishapp.core.navigation

sealed class Screen(val route: String, val title: String? = null) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AuthLogin : Screen("auth_login")
    object AuthRegister : Screen("auth_register")
    object AuthLearningGoal : Screen("auth_learning_goal")
    object AuthLevelSelection : Screen("auth_level_selection")
    object AuthProfileSetup : Screen("auth_profile_setup")

    object Main : Screen("main")
    object Dashboard : Screen("dashboard")
    object MainDashboard : Screen("main_dashboard", "Home")
    object MainDecks : Screen("main_decks", "Decks")
    object MainPractice : Screen("main_practice", "Practice")
    object MainProfile : Screen("main_profile", "Profile")

    object DeckList : Screen("deck_list")

    object CardList : Screen("card_list/{deckId}") {
        fun createRoute(deckId: String) = "card_list/$deckId"
    }

    object AddCard : Screen("add_card/{deckId}") {
        fun createRoute(deckId: String) = "add_card/$deckId"
    }

    object FlashcardLearning : Screen("flashcard_learning/{deckId}?reviewMode={reviewMode}") {
        fun createRoute(
            deckId: String,
            reviewMode: String = OFFICIAL_REVIEW_MODE
        ) = "flashcard_learning/$deckId?reviewMode=$reviewMode"

        fun createSpacedRepetitionRoute() = createRoute(SPACED_REPETITION_DECK_ID)

        const val SPACED_REPETITION_DECK_ID = "__spaced_repetition__"
        const val OFFICIAL_REVIEW_MODE = "official"
        const val FREE_REVIEW_MODE = "free"
    }

    object Practice : Screen("practice/{deckId}") {
        fun createRoute(deckId: String) = "practice/$deckId"
    }

    companion object {
        const val ROOT_GRAPH = "root"
    }
}
