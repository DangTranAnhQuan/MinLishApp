package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject
import kotlin.random.Random

private const val DISTRACTOR_COUNT = 3

class GenerateQuizUseCase @Inject constructor() {

    fun generateMultipleChoice(
        cards: List<Card>,
        distractorCards: List<Card> = cards,
        random: Random = Random.Default,
        excludedCardId: String? = null
    ): MultipleChoiceQuestion? {
        val eligibleCards = eligibleMultipleChoiceCards(cards, distractorCards)
        val answerCard = chooseCard(eligibleCards, excludedCardId, random) ?: return null
        return generateMultipleChoiceForCard(answerCard, distractorCards, random)
    }

    fun generateMultipleChoiceForCard(
        answerCard: Card,
        distractorCards: List<Card>,
        random: Random = Random.Default
    ): MultipleChoiceQuestion? {
        if (!isEligibleMultipleChoiceCard(answerCard, distractorCards)) return null

        val correctAnswer = answerCard.meaning.trim()
        val distractors = distractorsFor(distractorCards, answerCard)
            .shuffled(random)
            .take(DISTRACTOR_COUNT)

        return MultipleChoiceQuestion(
            cardId = answerCard.id,
            word = answerCard.word,
            options = (distractors + correctAnswer).shuffled(random),
            correctAnswer = correctAnswer,
            imageUrl = answerCard.imageUrl.takeIf { it.isNotBlank() }
        )
    }

    fun generateFillInBlank(
        cards: List<Card>,
        random: Random = Random.Default,
        excludedCardId: String? = null
    ): FillInBlankQuestion? {
        val eligibleCards = eligibleFillInBlankCards(cards)
        val answerCard = chooseCard(eligibleCards, excludedCardId, random) ?: return null
        return generateFillInBlankForCard(answerCard)
    }

    fun generateFillInBlankForCard(answerCard: Card): FillInBlankQuestion? {
        if (!isEligibleFillInBlankCard(answerCard)) return null

        return FillInBlankQuestion(
            cardId = answerCard.id,
            sentence = answerCard.example.replace(
                oldValue = answerCard.word,
                newValue = "(_____)",
                ignoreCase = true
            ),
            correctAnswer = answerCard.word,
            meaning = answerCard.meaning.trim(),
            phonetic = answerCard.pronunciation.takeIf { it.isNotBlank() }
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

    fun eligibleMultipleChoiceCards(
        cards: List<Card>,
        distractorCards: List<Card>
    ): List<Card> {
        return cards.filter { card -> isEligibleMultipleChoiceCard(card, distractorCards) }
    }

    fun eligibleFillInBlankCards(cards: List<Card>): List<Card> {
        return cards.filter(::isEligibleFillInBlankCard)
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
    }

    private fun isEligibleMultipleChoiceCard(card: Card, distractorCards: List<Card>): Boolean {
        return card.word.isNotBlank() &&
            card.meaning.isNotBlank() &&
            distractorsFor(distractorCards, card).size >= DISTRACTOR_COUNT
    }

    private fun isEligibleFillInBlankCard(card: Card): Boolean {
        return card.word.isNotBlank() &&
            card.example.isNotBlank() &&
            card.example.contains(card.word, ignoreCase = true)
    }
}
