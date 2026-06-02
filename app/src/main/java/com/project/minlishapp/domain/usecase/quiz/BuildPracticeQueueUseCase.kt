package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.PracticeQuizType
import com.project.minlishapp.domain.model.PracticeSessionMode
import javax.inject.Inject
import kotlin.random.Random

class BuildPracticeQueueUseCase @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val filterUsableFlashcardsUseCase: FilterUsableFlashcardsUseCase
) {

    operator fun invoke(
        cards: List<Card>,
        distractorCards: List<Card>,
        quizType: PracticeQuizType,
        sessionMode: PracticeSessionMode,
        currentTimeMs: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<String> {
        val eligibleCards = when (quizType) {
            PracticeQuizType.FLASHCARD -> {
                filterUsableFlashcardsUseCase(cards)
            }

            PracticeQuizType.MULTIPLE_CHOICE -> {
                generateQuizUseCase.eligibleMultipleChoiceCards(cards, distractorCards)
            }

            PracticeQuizType.FILL_IN_THE_BLANK -> {
                generateQuizUseCase.eligibleFillInBlankCards(cards)
            }
        }.distinctBy { it.id }

        val sessionCards = when (sessionMode) {
            PracticeSessionMode.SPACED_REPETITION -> {
                eligibleCards.filter {
                    it.sm2Interval > 0 && it.nextReviewTime.time <= currentTimeMs
                }
            }

            PracticeSessionMode.DECK_PRACTICE -> eligibleCards
        }
        return sessionCards
            .sortedBy { it.nextReviewTime.time }
            .distinctBy { it.word.trim().lowercase() }
            .shuffled(random)
            .map(Card::id)
    }
}
