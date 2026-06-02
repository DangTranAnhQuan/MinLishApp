package com.project.minlishapp.domain.usecase.stat

import com.project.minlishapp.domain.repository.DailyStatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardStats(
    val accuracy: Float,
    val dailyActivityData: List<Float>,
    val retentionData: List<Float?>
)

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: DailyStatRepository
) {
    operator fun invoke(userId: String): Flow<DashboardStats> {
        return repository.getWeeklyStats(userId).map { stats ->
            val formatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val last7Days = (6 downTo 0).map { daysAgo ->
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
                formatter.format(calendar.time)
            }
            val statMap = stats.associateBy { it.date }
            val todayStat = statMap[formatter.format(Calendar.getInstance().time)]

            DashboardStats(
                accuracy = if (todayStat != null && todayStat.totalReviews > 0) {
                    (todayStat.correctReviews.toFloat() / todayStat.totalReviews) * 100f
                } else {
                    0f
                },
                dailyActivityData = last7Days.map { date ->
                    statMap[date]?.totalReviews?.toFloat() ?: 0f
                },
                retentionData = last7Days.map { date ->
                    val stat = statMap[date]
                    if (stat != null && stat.dueReviewCount > 0) {
                        (stat.retentionCount.toFloat() / stat.dueReviewCount) * 100f
                    } else {
                        null
                    }
                }
            )
        }
    }

    private companion object {
        const val DATE_FORMAT = "yyyy-MM-dd"
    }
}
