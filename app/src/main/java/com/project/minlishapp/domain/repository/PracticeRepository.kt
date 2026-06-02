package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.PracticeAttempt

interface PracticeRepository {
    suspend fun saveReviewedAttempt(attempt: PracticeAttempt, reviewedCard: Card)
}
