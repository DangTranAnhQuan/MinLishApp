package com.project.minlishapp.domain.model

import java.util.Date

data class Card(
    val id: String = "",
    val deckId: String = "",
    val userId: String = "",
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val definition: String = "",
    val example: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val tags: List<String> = emptyList(),
    val sm2EaseFactor: Double = 2.5,
    val sm2Repetitions: Int = 0,
    val sm2Interval: Int = 0,
    val nextReviewTime: Date = Date(),
    val createdAt: Date = Date()
)
