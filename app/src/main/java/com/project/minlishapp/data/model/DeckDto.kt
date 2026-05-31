package com.project.minlishapp.data.model

import com.google.firebase.Timestamp

data class DeckDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val createdAt: Timestamp? = null
)
