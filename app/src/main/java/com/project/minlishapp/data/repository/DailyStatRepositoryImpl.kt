package com.project.minlishapp.data.repository

import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.model.DailyStatDto
import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.repository.DailyStatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class DailyStatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DailyStatRepository {

    override fun getWeeklyStats(userId: String): Flow<List<DailyStat>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.DAY_OF_YEAR, -6)
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
                            dto.toDomain()
                        } else null
                    }
                } ?: emptyList()
                trySend(stats)
            }
        awaitClose { listener.remove() }
    }
}
