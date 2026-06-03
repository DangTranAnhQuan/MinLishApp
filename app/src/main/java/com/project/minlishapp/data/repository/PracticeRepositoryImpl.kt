package com.project.minlishapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.FieldValue
import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.mapper.toDto
import com.project.minlishapp.data.model.DailyStatDto
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.model.PracticeAttempt
import com.project.minlishapp.domain.repository.PracticeRepository
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val DATE_FORMAT = "yyyy-MM-dd"

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
        val now = Date()
        val dateString = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(now)
        
        val dailyStatDocument = firestore.collection(DAILY_STATS_COLLECTION)
            .document("${attempt.userId}_$dateString")
        val userDocument = firestore.collection(USERS_COLLECTION).document(attempt.userId)

        firestore.runTransaction { transaction ->
            val savedAttempt = transaction.get(attemptDocument)
            if (!savedAttempt.exists()) {
                val currentDailyStat = transaction.get(dailyStatDocument)
                    .toObject(DailyStatDto::class.java)
                    ?.toDomain()
                    ?: DailyStat(date = dateString)
                    
                val currentCardSnapshot = transaction.get(cardDocument)
                val userSnapshot = transaction.get(userDocument)
                
                val shouldCountFirstTimeLearned = attempt.isFirstTimeLearned &&
                    ((currentCardSnapshot.getLong(SM2_INTERVAL_FIELD)?.toInt() ?: 0) == 0)
              
                val recordedAttempt = attempt.copy(
                    isFirstTimeLearned = shouldCountFirstTimeLearned,
                    answeredAt = now 
                )
                val updatedDailyStat = currentDailyStat.record(recordedAttempt)

                transaction.set(attemptDocument, recordedAttempt.toDto())
                transaction.set(cardDocument, reviewedCard.toDto())
                transaction.set(
                    dailyStatDocument,
                    updatedDailyStat.toDto(attempt.userId),
                    SetOptions.merge()
                )
                
                if (userSnapshot.exists()) {
                    val lastLearnedDate = userSnapshot.getTimestamp(LAST_LEARNED_DATE_FIELD)?.toDate()
                    val currentStreak = userSnapshot.getLong(CURRENT_STREAK_FIELD)?.toInt() ?: 0
                    
                    val updatedStreak = calculateNextStreak(
                        currentStreak = currentStreak,
                        lastLearnedDate = lastLearnedDate,
                        answeredAt = now // Dùng biến 'now' đồng nhất
                    )
                    if (updatedStreak != null) {
                        transaction.set(
                            userDocument,
                            mapOf(
                                CURRENT_STREAK_FIELD to updatedStreak,
                                LAST_LEARNED_DATE_FIELD to FieldValue.serverTimestamp()
                            ),
                            SetOptions.merge()
                        )
                    }
                }
            }
        }.await()
    }

    private companion object {
        const val PRACTICE_ATTEMPTS_COLLECTION = "practiceAttempts"
        const val CARDS_COLLECTION = "cards"
        const val DAILY_STATS_COLLECTION = "daily_stats"
        const val USERS_COLLECTION = "users"
        const val SM2_INTERVAL_FIELD = "sm2Interval"
        const val CURRENT_STREAK_FIELD = "currentStreak"
        const val LAST_LEARNED_DATE_FIELD = "lastLearnedDate"
    }
}

internal fun calculateNextStreak(
    currentStreak: Int,
    lastLearnedDate: Date?,
    answeredAt: Date
): Int? {
    if (lastLearnedDate == null) return 1
    
    val answeredLocalDate = answeredAt.toInstant()
        .atZone(java.time.ZoneOffset.UTC)
        .toLocalDate()
        
    val lastLearnedLocalDate = lastLearnedDate.toInstant()
        .atZone(java.time.ZoneOffset.UTC)
        .toLocalDate()

    val daysBetween = DAYS.between(lastLearnedLocalDate, answeredLocalDate)

    return when {
        daysBetween == 0L -> if (currentStreak > 0) null else 1 
        daysBetween == 1L -> currentStreak.coerceAtLeast(0) + 1 
        else -> 1 
    }
}