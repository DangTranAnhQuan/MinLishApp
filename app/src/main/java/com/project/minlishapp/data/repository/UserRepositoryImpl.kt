package com.project.minlishapp.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.project.minlishapp.data.mapper.toDomain
import com.project.minlishapp.data.mapper.toDto
import com.project.minlishapp.data.model.UserDto
import com.project.minlishapp.domain.model.User
import com.project.minlishapp.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUser(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dto = snapshot?.toObject(UserDto::class.java)
                trySend(dto?.toDomain())
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveUser(user: User) {
        val dto = user.toDto()
        firestore.collection("users").document(user.uid)
            .set(dto)
            .await()
    }

    override suspend fun updateStreak(uid: String, newStreak: Int, lastActiveDate: String) {
        // Parse date for Timestamp or update standard fields
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "currentStreak" to newStreak,
                    "lastLearnedDate" to Timestamp.now()
                )
            )
            .await()
    }
}
