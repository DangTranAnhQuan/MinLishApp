package com.project.minlishapp.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.minlishapp.data.repository.AuthRepositoryImpl
import com.project.minlishapp.data.repository.CardRepositoryImpl
import com.project.minlishapp.data.repository.DeckRepositoryImpl
import com.project.minlishapp.data.repository.UserRepositoryImpl
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.DeckRepository
import com.project.minlishapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindDeckRepository(
        deckRepositoryImpl: DeckRepositoryImpl
    ): DeckRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(
        cardRepositoryImpl: CardRepositoryImpl
    ): CardRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }
    }
}
