package com.project.minlishapp.data.model

data class DailyStatDto(
    val date: String = "",
    val wordsLearned: Int = 0,
    val retentionCount: Int = 0,
    val correctReviews: Int = 0,
    val totalReviews: Int = 0
)
