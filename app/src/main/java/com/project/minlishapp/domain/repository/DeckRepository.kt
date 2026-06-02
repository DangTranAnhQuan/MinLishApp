package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.Deck
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun getDecks(userId: String): Flow<List<Deck>>
    fun getDeck(deckId: String): Flow<Deck?>
    suspend fun insertDeck(deck: Deck)
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(deckId: String)
}
