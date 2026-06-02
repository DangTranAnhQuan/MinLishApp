package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject

private const val DAY_IN_MILLIS = 86_400_000L
private const val DEFAULT_FORECAST_BUCKETS = 5

data class ReviewForecastBucket(
    val label: String,
    val count: Int
)

class GetReviewForecastUseCase @Inject constructor() {

    operator fun invoke(
        cards: List<Card>,
        nowMs: Long = System.currentTimeMillis(),
        bucketCount: Int = DEFAULT_FORECAST_BUCKETS
    ): List<ReviewForecastBucket> {
        require(bucketCount > 0) { "Forecast bucket count must be positive." }

        val learnedCards = cards.filter { it.sm2Interval > 0 }
        return List(bucketCount) { index ->
            val count = if (index == 0) {
                learnedCards.count { it.nextReviewTime.time <= nowMs }
            } else {
                val startExclusive = nowMs + (index - 1) * DAY_IN_MILLIS
                val endInclusive = nowMs + index * DAY_IN_MILLIS
                learnedCards.count {
                    it.nextReviewTime.time > startExclusive &&
                        it.nextReviewTime.time <= endInclusive
                }
            }
            ReviewForecastBucket(
                label = if (index == 0) "Đến hạn" else "${index * 24}h",
                count = count
            )
        }
    }
}
