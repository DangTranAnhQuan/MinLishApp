package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterUsableFlashcardsUseCaseTest {

    private val useCase = FilterUsableFlashcardsUseCase()

    @Test
    fun `filters blank words and keeps only the first normalized word`() {
        val result = useCase(
            listOf(
                Card(id = "blank", word = " "),
                Card(id = "first", word = "diligent"),
                Card(id = "duplicate", word = " DILIGENT "),
                Card(id = "second", word = "candid")
            )
        )

        assertEquals(listOf("first", "second"), result.map(Card::id))
    }
}
