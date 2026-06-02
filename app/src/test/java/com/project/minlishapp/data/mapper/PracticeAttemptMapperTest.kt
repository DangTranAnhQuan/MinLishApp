package com.project.minlishapp.data.mapper

import com.project.minlishapp.domain.model.PracticeAttempt
import com.project.minlishapp.domain.model.PracticeQuizType
import com.project.minlishapp.domain.model.PracticeSessionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class PracticeAttemptMapperTest {

    @Test
    fun `practice attempt dto keeps fields required by firestore consumers`() {
        val answeredAt = Date(1_750_000_000_000L)
        val attempt = PracticeAttempt(
            id = "attempt-1",
            sessionId = "session-1",
            userId = "user-1",
            deckId = "deck-1",
            cardId = "card-1",
            quizType = PracticeQuizType.FILL_IN_THE_BLANK,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            isCorrect = true,
            qualityScore = 2,
            sm2IntervalDays = 6,
            sm2EaseFactor = 2.42,
            nextReviewTime = Date(answeredAt.time + 1_000),
            isDueReview = true,
            isFirstTimeLearned = false,
            answeredAt = answeredAt
        )

        val dto = attempt.toDto()

        assertEquals("attempt-1", dto.id)
        assertEquals("session-1", dto.sessionId)
        assertEquals("user-1", dto.userId)
        assertEquals("deck-1", dto.deckId)
        assertEquals("card-1", dto.cardId)
        assertEquals("FILL_IN_THE_BLANK", dto.quizType)
        assertEquals("SPACED_REPETITION", dto.sessionMode)
        assertTrue(dto.correct)
        assertEquals(2, dto.qualityScore)
        assertEquals(6, dto.sm2IntervalDays)
        assertEquals(2.42, dto.sm2EaseFactor, 0.001)
        assertEquals(attempt.nextReviewTime, dto.nextReviewTime?.toDate())
        assertTrue(dto.dueReview)
        assertEquals(false, dto.firstTimeLearned)
        assertEquals(answeredAt, dto.answeredAt?.toDate())
    }
}
