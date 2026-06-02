package com.project.minlishapp.domain.usecase.stat

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.DailyStatRepository
import com.project.minlishapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UpdatePracticeStatsUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val dailyStatRepository: DailyStatRepository
) {
    suspend operator fun invoke(userId: String, card: Card, isCorrect: Boolean) {
        val wasUnlearned = card.sm2Interval == 0

        // For retention tracking: if it was already learned and is due, and correct, it's retention
        val isRetention = card.sm2Interval > 0 &&
                         card.nextReviewTime.time <= System.currentTimeMillis() &&
                         isCorrect

        // 1. Update User Streak
        val user = userRepository.getUser(userId).firstOrNull()
        if (user != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())

            var newStreak = user.currentStreak
            if (user.lastLearnedDate != null) {
                val lastStr = sdf.format(user.lastLearnedDate)
                if (lastStr != todayStr) {
                    // Check if it was exactly yesterday
                    val cal = Calendar.getInstance()
                    cal.time = Date()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr = sdf.format(cal.time)

                    if (lastStr == yesterdayStr) {
                        newStreak += 1
                    } else {
                        newStreak = 1
                    }
                }
            } else {
                newStreak = 1
            }

            // Only update user if date is different or streak changed
            val currentLastStr = user.lastLearnedDate?.let { sdf.format(it) } ?: ""
            if (currentLastStr != todayStr || newStreak != user.currentStreak) {
                userRepository.updateStreak(userId, newStreak, todayStr)
            }
        }

        // 2. Update DailyStat
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = sdf.format(Date())
        val isFirstTimeLearned = wasUnlearned && isCorrect

        dailyStatRepository.recordReview(
            userId = userId,
            dateString = dateString,
            isCorrect = isCorrect,
            isFirstTimeLearned = isFirstTimeLearned,
            isRetention = isRetention
        )
    }
}
