package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class CalculateSm2NextReviewUseCaseTest {

    private val useCase = CalculateSm2NextReviewUseCase()

    @Test
    fun `good review from new card sets interval to 1 day and increments repetitions`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2EaseFactor = 2.5, sm2Repetitions = 0, sm2Interval = 0, nextReviewTime = Date(now))

        val updated = useCase(card, ReviewGrade.GOOD, now)

        assertEquals(1, updated.sm2Interval)
        assertEquals(1, updated.sm2Repetitions)
        assertEquals(2.4208, updated.sm2EaseFactor, 0.0001)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), updated.nextReviewTime.time)
    }

    @Test
    fun `again review resets repetitions and keeps ease factor above lower bound`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2EaseFactor = 1.31, sm2Repetitions = 4, sm2Interval = 12, nextReviewTime = Date(now))

        val updated = useCase(card, ReviewGrade.AGAIN, now)

        assertEquals(0, updated.sm2Repetitions)
        assertEquals(1, updated.sm2Interval)
        assertTrue(updated.sm2EaseFactor >= 1.3)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), updated.nextReviewTime.time)
    }

    @Test
    fun `easy review after repeated sessions grows interval with ease factor`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2EaseFactor = 2.5, sm2Repetitions = 2, sm2Interval = 6, nextReviewTime = Date(now))

        val updated = useCase(card, ReviewGrade.EASY, now)

        assertEquals(3, updated.sm2Repetitions)
        assertEquals(15, updated.sm2Interval)
        assertEquals(now + TimeUnit.DAYS.toMillis(15), updated.nextReviewTime.time)
    }
}


