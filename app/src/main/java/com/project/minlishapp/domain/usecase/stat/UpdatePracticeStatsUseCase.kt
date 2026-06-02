package com.project.minlishapp.domain.usecase.stat

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.DailyStatRepository
import com.project.minlishapp.domain.repository.UserRepository
import com.project.minlishapp.domain.usecase.srs.CalculateSm2NextReviewUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UpdatePracticeStatsUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val userRepository: UserRepository,
    private val dailyStatRepository: DailyStatRepository,
    private val calculateSm2NextReviewUseCase: CalculateSm2NextReviewUseCase
) {
    suspend operator fun invoke(userId: String, card: Card, isCorrect: Boolean) {
        // 1. Update Card via SM2
        val grade = if (isCorrect) ReviewGrade.GOOD else ReviewGrade.AGAIN
        val wasUnlearned = card.sm2Repetitions == 0
        
        // For retention tracking: if it was already learned and is due, and correct, it's retention
        val isRetention = card.sm2Repetitions > 0 && 
                         card.nextReviewTime.time <= System.currentTimeMillis() && 
                         isCorrect
                         
        val updatedCard = calculateSm2NextReviewUseCase(card, grade)
        cardRepository.updateCard(updatedCard)

        // 2. Update User Streak
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

        // 3. Update DailyStat
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
