package com.project.minlishapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.mapper.toDto
import com.project.minlishapp.data.model.DeckDto
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.domain.repository.DeckRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DeckRepository {

    override fun getDecks(userId: String): Flow<List<Deck>> = callbackFlow {
        val listener = firestore.collection("decks")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val decks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DeckDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                trySend(decks)
            }
        awaitClose { listener.remove() }
    }

    override fun getDeck(deckId: String): Flow<Deck?> = callbackFlow {
        val listener = firestore.collection("decks").document(deckId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val deckDto = snapshot?.toObject(DeckDto::class.java)?.copy(id = snapshot.id)
                trySend(deckDto?.toDomain())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun insertDeck(deck: Deck) {
        val newDoc = firestore.collection("decks").document()
        val deckWithId = deck.copy(id = newDoc.id)
        val dto = deckWithId.toDto()
        newDoc.set(dto).await()
    }

    override suspend fun deleteDeck(deckId: String) {
        firestore.collection("decks").document(deckId).delete().await()
    }
}
