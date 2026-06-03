package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject
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
    }
}
