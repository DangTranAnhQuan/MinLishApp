package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GenerateQuizUseCaseTest {

    private val useCase = GenerateQuizUseCase()

    @Test
    fun `multiple choice question contains one answer and three distinct distractors`() {
        val cards = listOf(
            Card(id = "1", word = "diligent", meaning = "cham chi"),
            Card(id = "2", word = "candid", meaning = "thang than"),
            Card(id = "3", word = "eloquent", meaning = "luu loat"),
            Card(id = "4", word = "ambiguous", meaning = "mo ho")
        )

        val question = useCase.generateMultipleChoice(cards, random = Random(1))

        assertNotNull(question)
        assertEquals(4, question?.options?.size)
        assertEquals(4, question?.options?.distinct()?.size)
        assertTrue(question?.options?.contains(question.correctAnswer) == true)
    }

    @Test
    fun `multiple choice question is unavailable when deck has fewer than four meanings`() {
        val cards = listOf(
            Card(id = "1", word = "diligent", meaning = "cham chi"),
            Card(id = "2", word = "candid", meaning = "thang than"),
            Card(id = "3", word = "eloquent", meaning = "luu loat")
        )

        val question = useCase.generateMultipleChoice(cards, random = Random(1))

        assertNull(question)
    }

    @Test
    fun `multiple choice question uses system meanings when current deck has fewer than four meanings`() {
        val deckCards = listOf(
            Card(id = "1", deckId = "deck_1", word = "diligent", meaning = "cham chi")
        )
        val systemCards = deckCards + listOf(
            Card(id = "2", deckId = "deck_2", word = "candid", meaning = "thang than"),
            Card(id = "3", deckId = "deck_2", word = "eloquent", meaning = "luu loat"),
            Card(id = "4", deckId = "deck_2", word = "ambiguous", meaning = "mo ho")
        )

        val question = useCase.generateMultipleChoice(deckCards, systemCards, Random(1))

        assertNotNull(question)
        assertEquals("diligent", question?.word)
        assertEquals(4, question?.options?.size)
    }

    @Test
    fun `fill in blank question hides keyword without matching case`() {
        val cards = listOf(
            Card(
                id = "1",
                word = "Diligent",
                example = "His diligent approach helped him pass the exam."
            )
        )

        val question = useCase.generateFillInBlank(cards, Random(1))

        assertNotNull(question)
        assertTrue(question?.sentence?.contains("(_____)") == true)
        assertFalse(question?.sentence?.contains("diligent", ignoreCase = true) == true)
        assertEquals("Diligent", question?.correctAnswer)
        assertEquals("", question?.meaning)
    }

    @Test
    fun `fill in blank question is unavailable when example does not contain keyword`() {
        val cards = listOf(
            Card(id = "1", word = "diligent", example = "This example does not use the keyword.")
        )

        val question = useCase.generateFillInBlank(cards, Random(1))

        assertNull(question)
    }

    @Test
    fun `fill in blank next question excludes previously shown card when another card is available`() {
        val cards = listOf(
            Card(id = "1", word = "diligent", example = "His diligent approach helped him."),
            Card(id = "2", word = "candid", example = "She gave a candid interview.")
        )

        val question = useCase.generateFillInBlank(
            cards = cards,
            excludedCardId = "1",
            random = Random(1)
        )

        assertEquals("2", question?.cardId)
    }
}
