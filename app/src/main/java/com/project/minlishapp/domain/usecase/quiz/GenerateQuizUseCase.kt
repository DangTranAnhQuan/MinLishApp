package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject
<<<<<<< HEAD
import javax.inject.Singleton

@Singleton
class GenerateQuizUseCase @Inject constructor() {

    fun countMultipleChoiceQuestions(cards: List<Card>, distractorCards: List<Card>): Int {
        val distinctMeanings = distractorCards.map { it.meaning.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
        
        if (distinctMeanings.size < 4) return 0
        
        return cards.filter { it.meaning.isNotBlank() }.size
    }

    fun generateMultipleChoice(
        cards: List<Card>,
        distractorCards: List<Card>,
        excludedCardId: String? = null
    ): MultipleChoiceQuestion? {
        val eligibleCards = cards.filter { it.meaning.isNotBlank() && it.id != excludedCardId }
        if (eligibleCards.isEmpty()) {
            val anyEligible = cards.filter { it.meaning.isNotBlank() }
            if (anyEligible.isEmpty()) return null
            // If only one card is available, we might still want to return it even if it was excluded last time
            // but let's stick to simple logic for now
        }

        val targetCard = eligibleCards.randomOrNull() ?: cards.filter { it.meaning.isNotBlank() }.randomOrNull() ?: return null
        
        val allDistractors = distractorCards
            .map { it.meaning.trim() }
            .filter { it.isNotBlank() && !it.equals(targetCard.meaning.trim(), ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .shuffled()

        if (allDistractors.size < 3) return null

        val options = (allDistractors.take(3) + targetCard.meaning.trim()).shuffled()

        return MultipleChoiceQuestion(
            cardId = targetCard.id,
            word = targetCard.word,
            options = options,
            correctAnswer = targetCard.meaning.trim(),
            imageUrl = targetCard.imageUrl
        )
    }

    fun countFillInBlankQuestions(cards: List<Card>): Int {
        return cards.filter { hasValidExample(it) }.size
    }

    fun generateFillInBlank(cards: List<Card>, excludedCardId: String? = null): FillInBlankQuestion? {
        val eligibleCards = cards.filter { hasValidExample(it) && it.id != excludedCardId }
        val targetCard = eligibleCards.randomOrNull() ?: cards.filter { hasValidExample(it) }.randomOrNull() ?: return null

        val sentence = targetCard.example
        val word = targetCard.word
        
        // Simple blanking logic: replace word with _____
        // More robust logic would use regex to handle case-insensitivity and punctuation
        val blankedSentence = sentence.replace(word, "_____", ignoreCase = true)

        return FillInBlankQuestion(
            cardId = targetCard.id,
            sentenceWithBlank = blankedSentence,
            correctAnswer = word,
            phonetic = targetCard.pronunciation,
            meaning = targetCard.meaning
        )
    }

    private fun hasValidExample(card: Card): Boolean {
        return card.example.isNotBlank() && card.word.isNotBlank() && 
               card.example.contains(card.word, ignoreCase = true)
=======
import kotlin.random.Random

private const val DISTRACTOR_COUNT = 3

data class MultipleChoiceQuestion(
    val cardId: String,
    val word: String,
    val options: List<String>,
    val correctAnswer: String
)

data class FillInBlankQuestion(
    val cardId: String,
    val sentence: String,
    val correctAnswer: String,
    val meaning: String
)

class GenerateQuizUseCase @Inject constructor() {

    fun generateMultipleChoice(
        cards: List<Card>,
        distractorCards: List<Card> = cards,
        random: Random = Random.Default,
        excludedCardId: String? = null
    ): MultipleChoiceQuestion? {
        val eligibleCards = eligibleMultipleChoiceCards(cards, distractorCards)
        val answerCard = chooseCard(eligibleCards, excludedCardId, random) ?: return null
        val correctAnswer = answerCard.meaning.trim()
        val distractors = distractorsFor(distractorCards, answerCard)
            .shuffled(random)
            .take(DISTRACTOR_COUNT)

        return MultipleChoiceQuestion(
            cardId = answerCard.id,
            word = answerCard.word,
            options = (distractors + correctAnswer).shuffled(random),
            correctAnswer = correctAnswer
        )
    }

    fun generateFillInBlank(
        cards: List<Card>,
        random: Random = Random.Default,
        excludedCardId: String? = null
    ): FillInBlankQuestion? {
        val eligibleCards = eligibleFillInBlankCards(cards)
        val answerCard = chooseCard(eligibleCards, excludedCardId, random) ?: return null

        return FillInBlankQuestion(
            cardId = answerCard.id,
            sentence = answerCard.example.replace(
                oldValue = answerCard.word,
                newValue = "(_____)",
                ignoreCase = true
            ),
            correctAnswer = answerCard.word,
            meaning = answerCard.meaning.trim()
        )
    }

    fun countMultipleChoiceQuestions(
        cards: List<Card>,
        distractorCards: List<Card> = cards
    ): Int {
        return eligibleMultipleChoiceCards(cards, distractorCards).size
    }

    fun countFillInBlankQuestions(cards: List<Card>): Int {
        return eligibleFillInBlankCards(cards).size
    }

    private fun eligibleMultipleChoiceCards(
        cards: List<Card>,
        distractorCards: List<Card>
    ): List<Card> {
        return cards.filter { card ->
            card.word.isNotBlank() &&
                card.meaning.isNotBlank() &&
                distractorsFor(distractorCards, card).size >= DISTRACTOR_COUNT
        }
    }

    private fun eligibleFillInBlankCards(cards: List<Card>): List<Card> {
        return cards.filter { card ->
            card.word.isNotBlank() &&
                card.example.isNotBlank() &&
                card.example.contains(card.word, ignoreCase = true)
        }
    }

    private fun chooseCard(
        cards: List<Card>,
        excludedCardId: String?,
        random: Random
    ): Card? {
        val newCards = cards.filterNot { it.id == excludedCardId }
        return newCards.ifEmpty { cards }.randomOrNull(random)
    }

    private fun distractorsFor(cards: List<Card>, answerCard: Card): List<String> {
        return cards
            .map { it.meaning.trim() }
            .filter { meaning ->
                meaning.isNotBlank() &&
                    !meaning.equals(answerCard.meaning.trim(), ignoreCase = true)
            }
            .distinctBy { it.lowercase() }
>>>>>>> 25066cdf46e3d5d1b7618a493510347e3c9bf22e
    }
}
