package com.project.minlishapp.domain.model

import java.util.Date

data class PracticeAttempt(
    val id: String,
    val sessionId: String,
    val userId: String,
    val deckId: String,
    val cardId: String,
    val quizType: PracticeQuizType,
    val sessionMode: PracticeSessionMode,
    val isCorrect: Boolean,
    val qualityScore: Int,
    val sm2IntervalDays: Int,
    val sm2EaseFactor: Double,
    val nextReviewTime: Date,
    val isDueReview: Boolean,
    val isFirstTimeLearned: Boolean,
    val answeredAt: Date = Date()
)

enum class PracticeQuizType {
    FLASHCARD,
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK
}

enum class PracticeSessionMode {
    SPACED_REPETITION,
    DECK_PRACTICE
}
