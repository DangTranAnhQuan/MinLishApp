package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.PracticeQuizType
import com.project.minlishapp.domain.model.PracticeSessionMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import kotlin.random.Random

class BuildPracticeQueueUseCaseTest {

    private val useCase = BuildPracticeQueueUseCase(
        generateQuizUseCase = GenerateQuizUseCase(),
        filterUsableFlashcardsUseCase = FilterUsableFlashcardsUseCase()
    )

    @Test
    fun `spaced repetition queue only includes learned due cards and removes duplicate words`() {
        val now = 1_750_000_000_000L
        val dueCard = Card(
            id = "due",
            word = "diligent",
            example = "He is diligent.",
            sm2Repetitions = 1,
            sm2Interval = 1,
            nextReviewTime = Date(now - 1)
        )
        val futureCard = Card(
            id = "future",
            word = "candid",
            example = "She is candid.",
            sm2Repetitions = 1,
            sm2Interval = 1,
            nextReviewTime = Date(now + 1)
        )
        val duplicateFutureWord = Card(
            id = "duplicate-future",
            word = "Diligent",
            example = "They are diligent.",
            sm2Repetitions = 1,
            sm2Interval = 1,
            nextReviewTime = Date(now + 2)
        )

        val queue = useCase(
            cards = listOf(futureCard, duplicateFutureWord, dueCard, dueCard),
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FILL_IN_THE_BLANK,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(listOf("due"), queue)
    }

    @Test
    fun `deck practice queue includes future cards and excludes invalid multiple choice cards`() {
        val now = 1_750_000_000_000L
        val cards = listOf(
            Card(id = "1", word = "diligent", meaning = "cham chi", nextReviewTime = Date(now + 1)),
            Card(id = "2", word = "candid", meaning = "thang than", nextReviewTime = Date(now + 1)),
            Card(id = "3", word = "eloquent", meaning = "luu loat", nextReviewTime = Date(now + 1)),
            Card(id = "4", word = "", meaning = "mo ho", nextReviewTime = Date(now + 1))
        )

        val queue = useCase(
            cards = cards,
            distractorCards = cards,
            quizType = PracticeQuizType.MULTIPLE_CHOICE,
            sessionMode = PracticeSessionMode.DECK_PRACTICE,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(3, queue.size)
        assertTrue(queue.containsAll(listOf("1", "2", "3")))
        assertFalse(queue.contains("4"))
    }

    @Test
    fun `spaced repetition queue excludes new cards even when they are due`() {
        val now = 1_750_000_000_000L
        val newCard = Card(
            id = "new",
            word = "diligent",
            example = "He is diligent.",
            sm2Repetitions = 0,
            nextReviewTime = Date(now - 1)
        )

        val queue = useCase(
            cards = listOf(newCard),
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FILL_IN_THE_BLANK,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            currentTimeMs = now,
            random = Random(1)
        )

        assertTrue(queue.isEmpty())
    }

    @Test
    fun `new duplicate does not hide learned due card in spaced repetition queue`() {
        val now = 1_750_000_000_000L
        val newDuplicate = Card(
            id = "new",
            word = "diligent",
            example = "He is diligent.",
            sm2Repetitions = 0,
            nextReviewTime = Date(now - 2)
        )
        val learnedDueCard = Card(
            id = "learned",
            word = "Diligent",
            example = "She is diligent.",
            sm2Repetitions = 2,
            sm2Interval = 1,
            nextReviewTime = Date(now - 1)
        )

        val queue = useCase(
            cards = listOf(newDuplicate, learnedDueCard),
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FILL_IN_THE_BLANK,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(listOf("learned"), queue)
    }

    @Test
    fun `flashcard queue accepts cards without quiz fields`() {
        val now = 1_750_000_000_000L
        val learnedCard = Card(
            id = "learned",
            word = "diligent",
            sm2Repetitions = 1,
            sm2Interval = 1,
            nextReviewTime = Date(now - 1)
        )

        val queue = useCase(
            cards = listOf(learnedCard),
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FLASHCARD,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(listOf("learned"), queue)
    }

    @Test
    fun `flashcard queue excludes blank and duplicate words`() {
        val now = 1_750_000_000_000L
        val cards = listOf(
            Card(id = "blank", word = "  ", nextReviewTime = Date(now)),
            Card(id = "first", word = "diligent", nextReviewTime = Date(now)),
            Card(id = "duplicate", word = " DILIGENT ", nextReviewTime = Date(now)),
            Card(id = "second", word = "candid", nextReviewTime = Date(now))
        )

        val queue = useCase(
            cards = cards,
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FLASHCARD,
            sessionMode = PracticeSessionMode.DECK_PRACTICE,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(2, queue.size)
        assertTrue(queue.containsAll(listOf("first", "second")))
    }

    @Test
    fun `spaced repetition queue keeps scheduled card after repetitions reset`() {
        val now = 1_750_000_000_000L
        val failedCard = Card(
            id = "failed",
            word = "diligent",
            sm2Repetitions = 0,
            sm2Interval = 1,
            nextReviewTime = Date(now - 1)
        )

        val queue = useCase(
            cards = listOf(failedCard),
            distractorCards = emptyList(),
            quizType = PracticeQuizType.FLASHCARD,
            sessionMode = PracticeSessionMode.SPACED_REPETITION,
            currentTimeMs = now,
            random = Random(1)
        )

        assertEquals(listOf("failed"), queue)
    }
}
