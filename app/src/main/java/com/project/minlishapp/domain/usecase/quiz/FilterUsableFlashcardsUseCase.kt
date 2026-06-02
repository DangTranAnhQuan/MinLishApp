package com.project.minlishapp.domain.usecase.quiz

import com.project.minlishapp.domain.model.Card
import javax.inject.Inject

class FilterUsableFlashcardsUseCase @Inject constructor() {

    operator fun invoke(cards: List<Card>): List<Card> {
        return cards
            .filter { it.word.isNotBlank() }
            .distinctBy(Card::id)
            .distinctBy { it.word.trim().lowercase() }
    }
}
