package com.project.minlishapp.domain.repository

import com.project.minlishapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(uid: String): Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun updateStreak(uid: String, newStreak: Int, lastActiveDate: String)
}
