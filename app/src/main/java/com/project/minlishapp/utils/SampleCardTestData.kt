package com.project.minlishapp.utils

import com.project.minlishapp.domain.model.Card
import java.util.Date

/**
 * Sample flashcard data for local testing.
 * Use TestDataInjection.insertSampleCardsForDeck() to populate cards into repository.
 */
object SampleCardTestData {

    fun createSampleCards(deckId: String, userId: String): List<Card> {
        val now = System.currentTimeMillis()
        return listOf(
            Card(
                id = "card_1",
                deckId = deckId,
                userId = userId,
                word = "accountability",
                pronunciation = "/əˌkaʊntəˈbɪləti/",
                meaning = "trách nhiệm giải trình",
                descriptionEn = "the fact of being responsible for your actions",
                example = "The manager emphasized accountability in every project.",
                collocation = "take accountability",
                relatedWords = "responsibility, obligation",
                note = "Common in business contexts",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            ),
            Card(
                id = "card_2",
                deckId = deckId,
                userId = userId,
                word = "ambiguous",
                pronunciation = "/æmˈbɪɡjuəs/",
                meaning = "có thể hiểu theo nhiều cách khác nhau",
                descriptionEn = "open to more than one interpretation; unclear",
                example = "The instructions were ambiguous and caused confusion.",
                collocation = "ambiguous statement, ambiguous meaning",
                relatedWords = "unclear, vague, uncertain",
                note = "Often used in legal or technical writing",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            ),
            Card(
                id = "card_3",
                deckId = deckId,
                userId = userId,
                word = "benevolent",
                pronunciation = "/bəˈnɛvələnt/",
                meaning = "tốt bụng, từ bi",
                descriptionEn = "kind and generous; showing goodwill",
                example = "The benevolent donation helped countless families.",
                collocation = "benevolent fund, benevolent organization",
                relatedWords = "kind, generous, compassionate",
                note = "Often used to describe charitable acts",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            ),
            Card(
                id = "card_4",
                deckId = deckId,
                userId = userId,
                word = "candid",
                pronunciation = "/ˈkændɪd/",
                meaning = "thẳng thắn, tươi sáng",
                descriptionEn = "frank, honest, and straightforward in expression",
                example = "She gave a candid interview about her career challenges.",
                collocation = "candid camera, candid opinion",
                relatedWords = "frank, direct, honest",
                note = "Formal word, often used in interviews",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            ),
            Card(
                id = "card_5",
                deckId = deckId,
                userId = userId,
                word = "diligent",
                pronunciation = "/ˈdɪlɪdʒənt/",
                meaning = "chăm chỉ, cẩn thận",
                descriptionEn = "showing care and effort in work; conscientious",
                example = "His diligent approach to studying helped him pass the exam.",
                collocation = "diligent worker, diligent effort",
                relatedWords = "hardworking, industrious, careful",
                note = "Positive trait often mentioned in evaluations",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            ),
            Card(
                id = "card_6",
                deckId = deckId,
                userId = userId,
                word = "eloquent",
                pronunciation = "/ˈɛləkwənt/",
                meaning = "nói lưu loát, hùng heloquent",
                descriptionEn = "fluent and expressive in speech or writing",
                example = "The speaker delivered an eloquent speech that moved everyone.",
                collocation = "eloquent speech, eloquent writing",
                relatedWords = "articulate, fluent, expressive",
                note = "Used to praise communication skills",
                sm2EaseFactor = 2.5,
                sm2Repetitions = 0,
                sm2Interval = 0,
                nextReviewTime = Date(now),
                createdAt = Date(now)
            )
        )
    }
}

