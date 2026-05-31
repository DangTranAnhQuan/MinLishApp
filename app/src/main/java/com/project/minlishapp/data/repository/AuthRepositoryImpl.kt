package com.project.minlishapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.project.minlishapp.domain.model.User
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        learningTarget: String,
        currentLevel: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User registration failed")
            
            // Update profile with display name
            val profileUpdates = userProfileChangeRequest {
                this.displayName = displayName
            }
            user.updateProfile(profileUpdates).await()

            // Save to Firestore with new schema properties
            val newUser = User(
                uid = user.uid,
                name = displayName,
                email = email,
                learningTarget = learningTarget,
                currentLevel = currentLevel,
                currentStreak = 0,
                lastLearnedDate = null,
                totalWordsLearned = 0,
                createdAt = Date()
            )
            
            try {
                userRepository.saveUser(newUser)
            } catch (fsError: Exception) {
                user.delete().await()
                throw fsError
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithCredential(credential: com.google.firebase.auth.AuthCredential): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google Sign-In failed")
            
            val exists = userRepository.getUser(user.uid).first() != null
            if (!exists) {
                val newUser = User(
                    uid = user.uid,
                    name = user.displayName ?: "Google User",
                    email = user.email ?: "",
                    learningTarget = "IELTS",
                    currentLevel = "A1",
                    currentStreak = 0,
                    lastLearnedDate = null,
                    totalWordsLearned = 0,
                    createdAt = Date()
                )
                userRepository.saveUser(newUser)
            }
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }
}
