package com.project.minlishapp.data.model

import com.google.firebase.Timestamp

data class CardDto(
    val id: String = "",
    val deckId: String = "",
    val userId: String = "",
    val word: String = "",
    val pronunciation: String = "",
    val meaning: String = "",
    val definition: String = "",
    val descriptionEn: String = "",
    val example: String = "",
    val collocation: String = "",
    val relatedWords: String = "",
    val note: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val tags: List<String> = emptyList(),
    val sm2EaseFactor: Double = 2.5,
    val sm2Repetitions: Int = 0,
    val sm2Interval: Int = 0,
    val nextReviewTime: Timestamp? = null,
    val createdAt: Timestamp? = null
)
