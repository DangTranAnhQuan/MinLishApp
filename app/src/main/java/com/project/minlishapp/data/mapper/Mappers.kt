package com.project.minlishapp.data.mapper

import com.google.firebase.Timestamp
import com.project.minlishapp.data.model.CardDto
import com.project.minlishapp.data.model.DeckDto
import com.project.minlishapp.data.model.UserDto
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.domain.model.User
import java.util.Date

fun UserDto.toDomain(): User {
    return User(
        uid = uid,
        name = name,
        email = email,
        learningTarget = learningTarget,
        currentLevel = currentLevel,
        currentStreak = currentStreak,
        lastLearnedDate = lastLearnedDate?.toDate(),
        totalWordsLearned = totalWordsLearned,
        createdAt = createdAt?.toDate() ?: Date()
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        uid = uid,
        name = name,
        email = email,
        learningTarget = learningTarget,
        currentLevel = currentLevel,
        currentStreak = currentStreak,
        lastLearnedDate = lastLearnedDate?.let { Timestamp(it) },
        totalWordsLearned = totalWordsLearned,
        createdAt = Timestamp(createdAt)
    )
}

fun DeckDto.toDomain(): Deck {
    return Deck(
        id = id,
        userId = userId,
        title = title,
        description = description,
        tags = tags,
        wordCount = wordCount,
        createdAt = createdAt?.toDate() ?: Date()
    )
}

fun Deck.toDto(): DeckDto {
    return DeckDto(
        id = id,
        userId = userId,
        title = title,
        description = description,
        tags = tags,
        wordCount = wordCount,
        createdAt = Timestamp(createdAt)
    )
}

fun CardDto.toDomain(): Card {
    return Card(
        id = id,
        deckId = deckId,
        userId = userId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        definition = definition,
        example = example,
        imageUrl = imageUrl,
        audioUrl = audioUrl,
        tags = tags,
        sm2EaseFactor = sm2EaseFactor,
        sm2Repetitions = sm2Repetitions,
        sm2Interval = sm2Interval,
        nextReviewTime = nextReviewTime?.toDate() ?: Date(),
        createdAt = createdAt?.toDate() ?: Date()
    )
}

fun Card.toDto(): CardDto {
    return CardDto(
        id = id,
        deckId = deckId,
        userId = userId,
        word = word,
        pronunciation = pronunciation,
        meaning = meaning,
        definition = definition,
        example = example,
        imageUrl = imageUrl,
        audioUrl = audioUrl,
        tags = tags,
        sm2EaseFactor = sm2EaseFactor,
        sm2Repetitions = sm2Repetitions,
        sm2Interval = sm2Interval,
        nextReviewTime = Timestamp(nextReviewTime),
        createdAt = Timestamp(createdAt)
    )
}
