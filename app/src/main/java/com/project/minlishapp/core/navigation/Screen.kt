package com.project.minlishapp.core.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object DeckList : Screen("deck_list")
    object CardList : Screen("card_list/{deckId}") {
        fun createRoute(deckId: String) = "card_list/$deckId"
    }
    object FlashcardLearning : Screen("flashcard_learning/{deckId}") {
        fun createRoute(deckId: String) = "flashcard_learning/$deckId"
    }
    object Practice : Screen("practice/{deckId}") {
        fun createRoute(deckId: String) = "practice/$deckId"
    }

    companion object {
        const val ROOT_GRAPH = "root"
    }
}
