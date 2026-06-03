package com.project.minlishapp.data.repository

import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.model.DailyStatDto
import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.repository.DailyStatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDate
import javax.inject.Inject

class DailyStatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyStatRepository {

    override fun getWeeklyStats(userId: String): Flow<List<DailyStat>> = callbackFlow {
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now(java.time.ZoneOffset.UTC)
        val lastWeekStr = today.minusDays(6).format(formatter)
        val todayStr = today.format(formatter)

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
                            dto.toDomain()
                        } else null
                    }
                } ?: emptyList()
                trySend(stats)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllStats(userId: String): Flow<List<DailyStat>> = callbackFlow {
        val listener = firestore.collection("daily_stats")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stats = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(DailyStatDto::class.java)?.toDomain()
                } ?: emptyList()
                trySend(stats)
            }
        awaitClose { listener.remove() }
    }
}
