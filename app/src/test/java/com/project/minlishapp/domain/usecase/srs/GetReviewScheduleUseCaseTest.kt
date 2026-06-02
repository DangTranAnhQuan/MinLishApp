package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class GetReviewScheduleUseCaseTest {

    private val useCase = GetReviewScheduleUseCase()

    @Test
    fun `schedule counts due cards and groups next review batch by local day`() {
        val now = 1_700_000_000_000L
        val nextDay = now + TimeUnit.DAYS.toMillis(1)
        val cards = listOf(
            Card(id = "new-due", sm2Interval = 0, nextReviewTime = Date(now - 1)),
            Card(id = "due", sm2Interval = 1, nextReviewTime = Date(now - 1)),
            Card(id = "next-1", sm2Interval = 1, nextReviewTime = Date(nextDay)),
            Card(id = "next-2", sm2Interval = 1, nextReviewTime = Date(nextDay + TimeUnit.HOURS.toMillis(2))),
            Card(id = "later", sm2Interval = 1, nextReviewTime = Date(now + TimeUnit.DAYS.toMillis(3)))
        )

        val schedule = useCase(cards = cards, nowMs = now)

        assertEquals(1, schedule.dueNowCount)
        assertEquals(nextDay, schedule.nextReviewTime?.time)
        assertEquals(2, schedule.nextReviewCount)
    }
}
