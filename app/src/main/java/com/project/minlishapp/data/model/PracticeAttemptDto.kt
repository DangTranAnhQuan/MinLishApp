package com.project.minlishapp.data.model

import com.google.firebase.Timestamp

data class PracticeAttemptDto(
    val id: String = "",
    val sessionId: String = "",
    val userId: String = "",
    val deckId: String = "",
    val cardId: String = "",
    val quizType: String = "",
    val sessionMode: String = "",
    val correct: Boolean = false,
    val qualityScore: Int = 0,
    val sm2IntervalDays: Int = 0,
    val sm2EaseFactor: Double = 2.5,
    val nextReviewTime: Timestamp? = null,
    val answeredAt: Timestamp? = null
)
