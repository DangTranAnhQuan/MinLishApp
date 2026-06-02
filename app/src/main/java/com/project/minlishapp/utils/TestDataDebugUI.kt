package com.project.minlishapp.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.minlishapp.domain.repository.CardRepository
import kotlinx.coroutines.launch

/**
 * Debug UI composable to inject sample test data into the repository.
 * Use this composable in your Flashcard or debug screens when testing locally.
 * 
 * Example usage:
 *   if (BuildConfig.DEBUG && uiState.cards.isEmpty()) {
 *       TestDataDebugPanel(cardRepository = cardRepository, deckId = "debug_deck", userId = "test_user_id")
 *   }
 */
@Composable
fun TestDataDebugPanel(
    cardRepository: CardRepository,
    deckId: String = "debug_deck",
    userId: String = "test_user_id",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "DEBUG: Insert Test Cards",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Click button below to insert 6 sample cards into deck '$deckId' for testing Flashcard UI.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            TestDataInjection.insertSampleCardsForDeck(
                                repository = cardRepository,
                                deckId = deckId,
                                userId = userId
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insert 6 Sample Cards Now")
            }
            Text(
                text = "After clicking, cards will appear in the Flashcard view. You can test Flip, Grade buttons, and SM-2 data updates.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

