package com.project.minlishapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.mapper.toDto
import com.project.minlishapp.data.model.CardDto
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.CardRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : CardRepository {

    override fun getCardsInDeck(deckId: String): Flow<List<Card>> = callbackFlow {
        val listener = firestore.collection("cards")
            .whereEqualTo("deckId", deckId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CardDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(cards)
            }
        awaitClose { listener.remove() }
    }

    override fun getCardsByUser(userId: String): Flow<List<Card>> = callbackFlow {
        val listener = firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CardDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(cards)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllCards(): Flow<List<Card>> = callbackFlow {
        val listener = firestore.collection("cards")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CardDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(cards)
            }
        awaitClose { listener.remove() }
    }

    override fun getDueCards(userId: String, currentTimeMs: Long): Flow<List<Card>> = callbackFlow {
        val listener = firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .whereLessThanOrEqualTo("nextReviewTime", Timestamp(Date(currentTimeMs)))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val cards = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CardDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(cards)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun insertCard(card: Card) {
        val newDoc = firestore.collection("cards").document()
        val cardWithId = card.copy(id = newDoc.id)
        val dto = cardWithId.toDto()
        newDoc.set(dto).await()
    }

    override suspend fun insertCards(cards: List<Card>) {
        val batch = firestore.batch()
        cards.forEach { card ->
            val newDoc = firestore.collection("cards").document()
            val cardWithId = card.copy(id = newDoc.id)
            batch.set(newDoc, cardWithId.toDto())
        }
        batch.commit().await()
    }

    override suspend fun updateCard(card: Card) {
        val dto = card.toDto()
        firestore.collection("cards").document(card.id)
            .set(dto)
            .await()
    }

    override suspend fun deleteCard(cardId: String) {
        firestore.collection("cards").document(cardId).delete().await()
    }

    override fun getLearnedCardsCount(userId: String): Flow<Int> = callbackFlow {
        val listener = firestore.collection("cards")
            .whereEqualTo("userId", userId)
            .whereGreaterThan("sm2Interval", 0)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }
}
