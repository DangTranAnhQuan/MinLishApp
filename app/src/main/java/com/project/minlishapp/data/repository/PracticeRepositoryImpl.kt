package com.project.minlishapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.minlishapp.data.mapper.toDto
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.PracticeAttempt
import com.project.minlishapp.domain.repository.PracticeRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PracticeRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PracticeRepository {

    override suspend fun saveReviewedAttempt(attempt: PracticeAttempt, reviewedCard: Card) {
        require(attempt.id.isNotBlank()) { "Practice attempt id is required." }
        require(attempt.sessionId.isNotBlank()) { "Practice attempt sessionId is required." }
        require(attempt.userId.isNotBlank()) { "Practice attempt userId is required." }
        require(attempt.cardId.isNotBlank()) { "Practice attempt cardId is required." }
        require(reviewedCard.id == attempt.cardId) { "Reviewed card must match the practice attempt." }

        val attemptDocument = firestore.collection(PRACTICE_ATTEMPTS_COLLECTION).document(attempt.id)
        val cardDocument = firestore.collection(CARDS_COLLECTION).document(reviewedCard.id)
        firestore.batch()
            .set(attemptDocument, attempt.toDto())
            .set(cardDocument, reviewedCard.toDto())
            .commit()
            .await()
    }

    private companion object {
        const val PRACTICE_ATTEMPTS_COLLECTION = "practiceAttempts"
        const val CARDS_COLLECTION = "cards"
    }
}
