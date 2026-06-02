package com.project.minlishapp.domain.usecase.srs

import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDueCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(userId: String, currentTimeMs: Long): Flow<List<Card>> {
        return cardRepository.getDueCards(userId, currentTimeMs)
    }
}

