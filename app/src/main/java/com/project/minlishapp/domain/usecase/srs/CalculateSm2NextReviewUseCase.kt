package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import kotlin.math.max
import kotlin.math.roundToInt
import java.util.Date
import javax.inject.Inject

private const val DAY_IN_MILLIS = 86_400_000L

enum class ReviewGrade(val qualityScore: Int) {
    AGAIN(0),
    HARD(1),
    GOOD(2),
    EASY(3)
}

class CalculateSm2NextReviewUseCase @Inject constructor() {
    operator fun invoke(
        card: Card,
        grade: ReviewGrade,
        nowMs: Long = System.currentTimeMillis()
    ): Card {
        val q = grade.qualityScore
        val mappedQuality = q * 1.6 + 0.2

        var repetitions = card.sm2Repetitions
        var interval = card.sm2Interval
        var easeFactor = card.sm2EaseFactor

        if (q >= 2) {
            interval = when (repetitions) {
                0 -> 1
                1 -> 6
                else -> max(1, (interval * easeFactor).roundToInt())
            }
            repetitions += 1
        } else {
            repetitions = 0
            interval = 1
        }

        easeFactor += 0.1 - (5 - mappedQuality) * (0.08 + (5 - mappedQuality) * 0.02)
        if (easeFactor < 1.3) {
            easeFactor = 1.3
        }

        val nextReviewTime = Date(nowMs + interval * DAY_IN_MILLIS)

        return card.copy(
            sm2EaseFactor = easeFactor,
            sm2Repetitions = repetitions,
            sm2Interval = interval,
            nextReviewTime = nextReviewTime
        )
    }
}


