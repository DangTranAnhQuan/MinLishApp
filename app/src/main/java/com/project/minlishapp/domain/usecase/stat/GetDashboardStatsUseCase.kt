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
    val retentionData: List<Float>
)

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: DailyStatRepository
) {
    operator fun invoke(userId: String): Flow<DashboardStats> {
        return repository.getWeeklyStats(userId).map { stats ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            
            val last7Days = (6 downTo 0).map { i ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                sdf.format(cal.time)
            }
            
            val statMap = stats.associateBy { it.date }
            
            val dailyActivityData = mutableListOf<Float>()
            val retentionData = mutableListOf<Float>()
            
            var todayAccuracy = 0f
            
            val todayStr = sdf.format(calendar.time)
            val todayStat = statMap[todayStr]
            if (todayStat != null && todayStat.totalReviews > 0) {
                todayAccuracy = (todayStat.correctReviews.toFloat() / todayStat.totalReviews) * 100f
            }

            for (date in last7Days) {
                val stat = statMap[date]
                // Số từ đã học trong 7 ngày gần nhất
                val activity = stat?.wordsLearned?.toFloat() ?: 0f
                dailyActivityData.add(activity)
                
                // Lượng từ chưa bị quên
                val retention = stat?.retentionCount?.toFloat() ?: 0f
                retentionData.add(retention)
            }
            
            DashboardStats(
                accuracy = todayAccuracy,
                dailyActivityData = dailyActivityData,
                retentionData = retentionData
            )
        }
    }
}
