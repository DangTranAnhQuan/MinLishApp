package com.project.minlishapp.domain.usecase.stat

import com.project.minlishapp.domain.model.DailyStat
import com.project.minlishapp.domain.repository.DailyStatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetDashboardStatsUseCaseTest {

    @Test
    fun `dashboard reports completed attempts and retention percentage for today`() = runBlocking {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val repository = FakeDailyStatRepository(
            listOf(
                DailyStat(
                    date = today,
                    retentionCount = 3,
                    dueReviewCount = 4,
                    correctReviews = 4,
                    totalReviews = 5,
                    statsSchemaVersion = DailyStat.CURRENT_STATS_SCHEMA_VERSION
                )
            )
        )

        val stats = GetDashboardStatsUseCase(repository)("user").first()

        assertEquals(5f, stats.dailyActivityData.last(), 0.001f)
        assertEquals(75f, stats.retentionData.last() ?: error("Missing retention rate"), 0.001f)
        assertEquals(80f, stats.accuracy, 0.001f)
    }

    @Test
    fun `dashboard reports missing retention when no due review denominator exists`() = runBlocking {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val repository = FakeDailyStatRepository(
            listOf(
                DailyStat(
                    date = today,
                    retentionCount = 8,
                    dueReviewCount = 0,
                    totalReviews = 8
                )
            )
        )

        val stats = GetDashboardStatsUseCase(repository)("user").first()

        assertEquals(null, stats.retentionData.last())
    }

    private class FakeDailyStatRepository(
        private val stats: List<DailyStat>
    ) : DailyStatRepository {
        override fun getWeeklyStats(userId: String): Flow<List<DailyStat>> = flowOf(stats)
    }
}
