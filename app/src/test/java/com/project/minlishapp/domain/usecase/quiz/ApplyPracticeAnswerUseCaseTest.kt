package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.usecase.srs.CalculateSm2NextReviewUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class ApplyPracticeAnswerUseCaseTest {

    private val useCase = ApplyPracticeAnswerUseCase(CalculateSm2NextReviewUseCase())

    @Test
    fun `good grade advances sm2 repetition`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2Repetitions = 0, sm2Interval = 0, nextReviewTime = Date(now))

        val result = useCase(card = card, grade = ReviewGrade.GOOD, nowMs = now)

        assertEquals(ReviewGrade.GOOD, result.grade)
        assertEquals(1, result.reviewedCard.sm2Repetitions)
        assertEquals(1, result.reviewedCard.sm2Interval)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), result.reviewedCard.nextReviewTime.time)
    }

    @Test
    fun `again grade resets sm2 repetition`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2Repetitions = 3, sm2Interval = 15, nextReviewTime = Date(now))

        val result = useCase(card = card, grade = ReviewGrade.AGAIN, nowMs = now)

        assertEquals(ReviewGrade.AGAIN, result.grade)
        assertEquals(0, result.reviewedCard.sm2Repetitions)
        assertEquals(1, result.reviewedCard.sm2Interval)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), result.reviewedCard.nextReviewTime.time)
    }

    @Test
    fun `hard grade resets repetition and reduces ease factor`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2EaseFactor = 2.5, sm2Repetitions = 3, sm2Interval = 15, nextReviewTime = Date(now))

        val result = useCase(card = card, grade = ReviewGrade.HARD, nowMs = now)

        assertEquals(ReviewGrade.HARD, result.grade)
        assertEquals(0, result.reviewedCard.sm2Repetitions)
        assertEquals(1, result.reviewedCard.sm2Interval)
        assertEquals(now + TimeUnit.DAYS.toMillis(1), result.reviewedCard.nextReviewTime.time)
    }

    @Test
    fun `easy grade grows interval for an existing review card`() {
        val now = 1_700_000_000_000L
        val card = Card(sm2EaseFactor = 2.5, sm2Repetitions = 2, sm2Interval = 6, nextReviewTime = Date(now))

        val result = useCase(card = card, grade = ReviewGrade.EASY, nowMs = now)

        assertEquals(ReviewGrade.EASY, result.grade)
        assertEquals(3, result.reviewedCard.sm2Repetitions)
        assertEquals(15, result.reviewedCard.sm2Interval)
        assertEquals(now + TimeUnit.DAYS.toMillis(15), result.reviewedCard.nextReviewTime.time)
    }
}
