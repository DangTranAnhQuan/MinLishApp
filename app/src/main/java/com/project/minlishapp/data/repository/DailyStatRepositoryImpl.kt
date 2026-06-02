package com.project.minlishapp.data.repository

import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.repository.DailyStatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.minlishapp.data.model.DailyStatDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class DailyStatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyStatRepository {

    override fun getWeeklyStats(userId: String): Flow<List<DailyStat>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DAY_OF_YEAR, -7)
        val lastWeekStr = sdf.format(cal.time)
        val todayStr = sdf.format(today)

        val listener = firestore.collection("daily_stats")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DailyStatDto::class.java)?.let { dto ->
                        if (dto.date >= lastWeekStr && dto.date <= todayStr) {
                            DailyStat(
                                date = dto.date,
                                wordsLearned = dto.wordsLearned,
                                retentionCount = dto.retentionCount,
                                correctReviews = dto.correctReviews,
                                totalReviews = dto.totalReviews
                            )
                        } else null
                    }
                } ?: emptyList()
                trySend(stats)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun recordReview(
        userId: String,
        dateString: String,
        isCorrect: Boolean,
        isFirstTimeLearned: Boolean,
        isRetention: Boolean
    ) {
        val docId = "${userId}_$dateString"
        val docRef = firestore.collection("daily_stats").document(docId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            if (snapshot.exists()) {
                val dto = snapshot.toObject(DailyStatDto::class.java) ?: DailyStatDto(date = dateString)
                val updatedDto = dto.copy(
                    totalReviews = dto.totalReviews + 1,
                    correctReviews = dto.correctReviews + (if (isCorrect) 1 else 0),
                    wordsLearned = dto.wordsLearned + (if (isFirstTimeLearned) 1 else 0),
                    retentionCount = dto.retentionCount + (if (isRetention) 1 else 0)
                )
                // We must also keep userId in the document for querying!
                val map = mapOf(
                    "userId" to userId,
                    "date" to updatedDto.date,
                    "totalReviews" to updatedDto.totalReviews,
                    "correctReviews" to updatedDto.correctReviews,
                    "wordsLearned" to updatedDto.wordsLearned,
                    "retentionCount" to updatedDto.retentionCount
                )
                transaction.set(docRef, map, SetOptions.merge())
            } else {
                val map = mapOf(
                    "userId" to userId,
                    "date" to dateString,
                    "totalReviews" to 1,
                    "correctReviews" to if (isCorrect) 1 else 0,
                    "wordsLearned" to if (isFirstTimeLearned) 1 else 0,
                    "retentionCount" to if (isRetention) 1 else 0
                )
                transaction.set(docRef, map)
            }
        }.await()
    }
}

