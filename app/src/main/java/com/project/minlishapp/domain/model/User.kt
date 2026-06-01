package com.project.minlishapp.domain.model

import java.util.Date

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val learningTarget: String = "",
    val currentLevel: String = "",
    val currentStreak: Int = 0,
    val lastLearnedDate: Date? = null,
    val totalWordsLearned: Int = 0,
    val createdAt: Date = Date()
)
