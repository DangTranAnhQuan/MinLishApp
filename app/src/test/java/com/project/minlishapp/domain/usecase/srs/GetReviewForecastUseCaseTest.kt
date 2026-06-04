package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class GetReviewForecastUseCaseTest {

    private val useCase = GetReviewForecastUseCase()

    @Test
    fun `forecast groups learned cards into due now and next four rolling days`() {
        val now = 1_700_000_000_000L
        val cards = listOf(
            Card(id = "due", sm2Repetitions = 1, sm2Interval = 1, nextReviewTime = Date(now - 1)),
            Card(id = "failed-due", sm2Repetitions = 0, sm2Interval = 1, nextReviewTime = Date(now - 1)),
            Card(id = "next-day", sm2Repetitions = 2, sm2Interval = 1, nextReviewTime = Date(now + TimeUnit.HOURS.toMillis(12))),
            Card(id = "second-day", sm2Repetitions = 3, sm2Interval = 1, nextReviewTime = Date(now + TimeUnit.HOURS.toMillis(36))),
            Card(id = "new-due", sm2Repetitions = 0, sm2Interval = 0, nextReviewTime = Date(now - 1))
        )

        val forecast = useCase(cards = cards, nowMs = now)

        assertEquals(listOf("Đến hạn", "24h", "48h", "72h", "96h"), forecast.map { it.label })
        assertEquals(listOf(2, 1, 1, 0, 0), forecast.map { it.count })
    }
}
