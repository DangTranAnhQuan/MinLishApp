package com.project.minlishapp.domain.model

data class DailyStat(
    val date: String = "",
    val wordsLearned: Int = 0,
    val retentionCount: Int = 0,
    val correctReviews: Int = 0,
    val totalReviews: Int = 0
)

