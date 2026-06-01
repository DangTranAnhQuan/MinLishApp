package com.project.minlishapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        learningTarget: String,
        currentLevel: String
    ): Result<FirebaseUser>
    suspend fun loginWithCredential(credential: com.google.firebase.auth.AuthCredential): Result<FirebaseUser>
    suspend fun logout()
}
