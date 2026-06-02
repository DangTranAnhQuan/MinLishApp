package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject
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
    }
}
