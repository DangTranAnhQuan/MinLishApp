package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsInDeck(deckId: String): Flow<List<Card>>
<<<<<<< Updated upstream
=======
    fun getCardsByUser(userId: String): Flow<List<Card>>
    fun getAllCards(): Flow<List<Card>>
>>>>>>> Stashed changes
    fun getDueCards(userId: String, currentTimeMs: Long): Flow<List<Card>>
    suspend fun insertCard(card: Card)
    suspend fun insertCards(cards: List<Card>)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(cardId: String)
}
