package com.project.minlishapp.domain.model

data class DailyStat(
    val date: String = "",
    val wordsLearned: Int = 0,
    val retentionCount: Int = 0,
    val dueReviewCount: Int = 0,
    val correctReviews: Int = 0,
    val totalReviews: Int = 0,
    val statsSchemaVersion: Int = LEGACY_STATS_SCHEMA_VERSION
) {
    fun record(attempt: PracticeAttempt): DailyStat {
        val hasAccurateRetentionData = statsSchemaVersion >= CURRENT_STATS_SCHEMA_VERSION
        val existingRetentionCount = if (hasAccurateRetentionData) retentionCount else 0
        val existingDueReviewCount = if (hasAccurateRetentionData) dueReviewCount else 0

        return copy(
            wordsLearned = wordsLearned + if (attempt.isFirstTimeLearned) 1 else 0,
            retentionCount = existingRetentionCount + if (attempt.isDueReview && attempt.isCorrect) 1 else 0,
            dueReviewCount = existingDueReviewCount + if (attempt.isDueReview) 1 else 0,
            correctReviews = correctReviews + if (attempt.isCorrect) 1 else 0,
            totalReviews = totalReviews + 1,
            statsSchemaVersion = CURRENT_STATS_SCHEMA_VERSION
        )
    }

    companion object {
        const val LEGACY_STATS_SCHEMA_VERSION = 1
        const val CURRENT_STATS_SCHEMA_VERSION = 2
    }
}
