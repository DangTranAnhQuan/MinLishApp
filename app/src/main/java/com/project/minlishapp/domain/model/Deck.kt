package com.project.minlishapp.domain.model

import java.util.Date

data class Deck(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val createdAt: Date = Date()
)
