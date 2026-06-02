package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

data class ReviewSchedule(
    val dueNowCount: Int = 0,
    val nextReviewTime: Date? = null,
    val nextReviewCount: Int = 0
)

class GetReviewScheduleUseCase @Inject constructor() {

    operator fun invoke(
        cards: List<Card>,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewSchedule {
        val scheduledCards = cards.filter { it.sm2Interval > 0 }
        val dueNowCount = scheduledCards.count { it.nextReviewTime.time <= nowMs }
        val nextReviewTime = scheduledCards
            .asSequence()
            .map(Card::nextReviewTime)
            .filter { it.time > nowMs }
            .minByOrNull(Date::getTime)
            ?: return ReviewSchedule(dueNowCount = dueNowCount)

        return ReviewSchedule(
            dueNowCount = dueNowCount,
            nextReviewTime = nextReviewTime,
            nextReviewCount = scheduledCards.count {
                it.nextReviewTime.time > nowMs && isSameDay(it.nextReviewTime, nextReviewTime)
            }
        )
    }

    private fun isSameDay(first: Date, second: Date): Boolean {
        val firstCalendar = Calendar.getInstance().apply { time = first }
        val secondCalendar = Calendar.getInstance().apply { time = second }
        return firstCalendar.get(Calendar.ERA) == secondCalendar.get(Calendar.ERA) &&
            firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) &&
            firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR)
    }
}
