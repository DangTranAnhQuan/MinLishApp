package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsInDeck(deckId: String): Flow<List<Card>>
    fun getDueCards(userId: String, currentTimeMs: Long): Flow<List<Card>>
    suspend fun insertCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(cardId: String)
    fun getLearnedCardsCount(userId: String): Flow<Int>
}
