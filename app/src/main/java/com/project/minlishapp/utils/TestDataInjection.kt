package com.project.minlishapp.utils

import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.utils.SampleCardTestData

/**
 * Helper class to inject sample/test data into repositories for local manual testing.
 * 
 * Usage in composable (e.g., debug screen or button):
 *   ```
 *   val cardRepository: CardRepository = hiltViewModel() or inject()
 *   TestDataInjection.insertSampleCardsForDeck(
 *       repository = cardRepository,
 *       deckId = "debug_deck",
 *       userId = "test_user_id"
 *   )
 *   ```
 */
object TestDataInjection {
    
    /**
     * Inserts sample cards into the repository for testing the Flashcard UI.
     * This creates 6 sample cards (accountability, ambiguous, benevolent, candid, diligent, eloquent)
     * all associated with the given [deckId] and [userId].
     * 
     * @param repository CardRepository instance to insert cards into
     * @param deckId The deck ID to associate cards with (typically "debug_deck" for testing)
     * @param userId The user ID to associate cards with
     * @param onProgress Optional callback to track insertion progress
     */
    suspend fun insertSampleCardsForDeck(
        repository: CardRepository,
        deckId: String = "debug_deck",
        userId: String = "test_user_id",
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ) {
        val sampleCards = SampleCardTestData.createSampleCards(deckId, userId)
        sampleCards.forEachIndexed { index, card ->
            try {
                repository.insertCard(card)
                onProgress?.invoke(index + 1, sampleCards.size)
            } catch (e: Exception) {
                throw RuntimeException("Failed to insert card '${card.word}': ${e.message}", e)
            }
        }
    }
}
