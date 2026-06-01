package com.project.minlishapp.presentation.vocabulary

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        loadDecks()
    }

    private fun loadDecks() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                user?.uid?.let { userId ->
                    deckRepository.getDecks(userId).collect { decks ->
                        _uiState.update { it.copy(decks = decks) }
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Deck Operations
    fun addDeck(title: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            user?.uid?.let { userId ->
                val deck = Deck(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = title,
                    description = description,
                    tags = tags,
                    createdAt = Date()
                )
                deckRepository.insertDeck(deck)
            }
        }
    }

    fun updateDeck(deck: Deck) {
        viewModelScope.launch {
            deckRepository.insertDeck(deck)
        }
    }

    fun deleteDeck(deckId: String) {
        viewModelScope.launch {
            deckRepository.deleteDeck(deckId)
        }
    }

    // Card Form Operations
    fun updateCardForm(update: (CardFormState) -> CardFormState) {
        _uiState.update { it.copy(cardForm = update(it.cardForm)) }
    }

    fun saveCard(deckId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            user?.uid?.let { userId ->
                val form = _uiState.value.cardForm
                val card = Card(
                    id = UUID.randomUUID().toString(),
                    deckId = deckId,
                    userId = userId,
                    word = form.word,
                    pronunciation = form.phonetic,
                    meaning = form.meaning,
                    definition = form.definition,
                    example = form.example,
                    imageUrl = form.imageUrl,
                    audioUrl = form.audioUrl,
                    tags = form.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    createdAt = Date()
                )
                cardRepository.insertCard(card)
                // Reset form after save
                _uiState.update { it.copy(cardForm = CardFormState()) }
            }
        }
    }

    // CSV Import Logic
    fun importCsv(context: Context, uri: Uri, deckId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importProgress = 0f, importStatus = "Starting import...") }
            
            val userId = authRepository.currentUser.first()?.uid ?: return@launch
            
            try {
                val cards = withContext(Dispatchers.IO) {
                    val result = mutableListOf<Card>()
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            // Skip header if exists
                            val firstLine = reader.readLine()
                            // Assuming CSV format: word,phonetic,meaning,definition,example,imageUrl,audioUrl,tags
                            var line: String? = reader.readLine()
                            while (line != null) {
                                val parts = line.split(Regex("[,;]"))
                                if (parts.size >= 3) {
                                    val card = Card(
                                        id = UUID.randomUUID().toString(),
                                        deckId = deckId,
                                        userId = userId,
                                        word = parts.getOrNull(0)?.trim() ?: "",
                                        pronunciation = parts.getOrNull(1)?.trim() ?: "",
                                        meaning = parts.getOrNull(2)?.trim() ?: "",
                                        definition = parts.getOrNull(3)?.trim() ?: "",
                                        example = parts.getOrNull(4)?.trim() ?: "",
                                        imageUrl = parts.getOrNull(5)?.trim() ?: "",
                                        audioUrl = parts.getOrNull(6)?.trim() ?: "",
                                        tags = parts.getOrNull(7)?.split("|")?.map { it.trim() } ?: emptyList()
                                    )
                                    result.add(card)
                                }
                                line = reader.readLine()
                            }
                        }
                    }
                    result
                }

                val total = cards.size
                cards.chunked(50).forEachIndexed { index, chunk ->
                    cardRepository.insertCards(chunk)
                    val processedCount = (index + 1) * chunk.size
                    val progress = processedCount.toFloat() / total
                    _uiState.update { 
                        it.copy(
                            importProgress = progress.coerceAtMost(1f),
                            importStatus = "Processing: ${processedCount.coerceAtMost(total)} / $total words"
                        )
                    }
                }
                
                _uiState.update { it.copy(isImporting = false, importStatus = "Success! $total words imported.") }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, importStatus = "Error: ${e.message}") }
            }
        }
    }
}

data class VocabularyUiState(
    val decks: List<Deck> = emptyList(),
    val cards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importStatus: String = "",
    val cardForm: CardFormState = CardFormState()
)

data class CardFormState(
    val word: String = "",
    val phonetic: String = "",
    val meaning: String = "",
    val definition: String = "",
    val example: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val tags: String = ""
)
