package com.project.minlishapp.data.model

import com.google.firebase.Timestamp

data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val learningTarget: String = "",
    val currentLevel: String = "",
    val profilePictureUrl: String? = null,
    val currentStreak: Int = 0,
    val lastLearnedDate: Timestamp? = null,
    val totalWordsLearned: Int = 0,
    val createdAt: Timestamp? = null
)
