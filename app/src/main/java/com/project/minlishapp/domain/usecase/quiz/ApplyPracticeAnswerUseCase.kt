package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.usecase.srs.CalculateSm2NextReviewUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import javax.inject.Inject

data class PracticeReviewResult(
    val grade: ReviewGrade,
    val reviewedCard: Card
)

class ApplyPracticeAnswerUseCase @Inject constructor(
    private val calculateSm2NextReviewUseCase: CalculateSm2NextReviewUseCase
) {

    operator fun invoke(
        card: Card,
        grade: ReviewGrade,
        nowMs: Long = System.currentTimeMillis()
    ): PracticeReviewResult {
        return PracticeReviewResult(
            grade = grade,
            reviewedCard = calculateSm2NextReviewUseCase(card, grade, nowMs)
        )
    }
}
