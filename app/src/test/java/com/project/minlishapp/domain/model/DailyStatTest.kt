package com.project.minlishapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class DailyStatTest {

    @Test
    fun `new word increases activity and learned words without affecting retention`() {
        val updated = DailyStat(date = "2026-06-02").record(
            attempt(isCorrect = true, isDueReview = false, isFirstTimeLearned = true)
        )

        assertEquals(1, updated.totalReviews)
        assertEquals(1, updated.correctReviews)
        assertEquals(1, updated.wordsLearned)
        assertEquals(0, updated.dueReviewCount)
        assertEquals(0, updated.retentionCount)
    }

    @Test
    fun `due reviews increase retention denominator and only correct answer increases numerator`() {
        val afterIncorrectReview = DailyStat(date = "2026-06-02").record(
            attempt(isCorrect = false, isDueReview = true)
        )
        val afterCorrectReview = afterIncorrectReview.record(
            attempt(isCorrect = true, isDueReview = true)
        )

        assertEquals(2, afterCorrectReview.totalReviews)
        assertEquals(2, afterCorrectReview.dueReviewCount)
        assertEquals(1, afterCorrectReview.retentionCount)
    }

    @Test
    fun `first new event resets legacy retention counters with unknown denominator`() {
        val legacyStat = DailyStat(
            date = "2026-06-02",
            retentionCount = 9,
            dueReviewCount = 0,
            totalReviews = 12
        )

        val updated = legacyStat.record(attempt(isCorrect = false, isDueReview = false))

        assertEquals(13, updated.totalReviews)
        assertEquals(0, updated.dueReviewCount)
        assertEquals(0, updated.retentionCount)
        assertEquals(DailyStat.CURRENT_STATS_SCHEMA_VERSION, updated.statsSchemaVersion)
    }

    private fun attempt(
        isCorrect: Boolean,
        isDueReview: Boolean,
        isFirstTimeLearned: Boolean = false
    ) = PracticeAttempt(
        id = "attempt",
        sessionId = "session",
        userId = "user",
        deckId = "deck",
        cardId = "card",
        quizType = PracticeQuizType.FLASHCARD,
        sessionMode = PracticeSessionMode.SPACED_REPETITION,
        isCorrect = isCorrect,
        qualityScore = 2,
        sm2IntervalDays = 1,
        sm2EaseFactor = 2.5,
        nextReviewTime = Date(1_750_000_000_000L),
        isDueReview = isDueReview,
        isFirstTimeLearned = isFirstTimeLearned,
        answeredAt = Date(1_750_000_000_000L)
    )
}
