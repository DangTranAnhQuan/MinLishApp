package com.project.minlishapp.presentation.vocabulary

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
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
            _uiState.update { it.copy(isLoading = true) }
            authRepository.currentUser.collect { user ->
                user?.uid?.let { userId ->
                    deckRepository.getDecks(userId)
                        .catch { e ->
                            _uiState.update { it.copy(
                                isLoading = false,
                                errorMessage = "Error loading decks: ${e.message}. If this is a Firestore error, check if you need to create an index."
                            ) }
                        }
                        .collect { decks ->
                            _uiState.update { it.copy(decks = decks, isLoading = false, errorMessage = null) }
                        }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadCards(deckId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            cardRepository.getCardsInDeck(deckId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { cards ->
                    _uiState.update { it.copy(cards = cards, isLoading = false) }
                }
        }
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
            deckRepository.updateDeck(deck)
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

    fun saveCard(deckId: String, onSaved: () -> Unit = {}) {
        val form = _uiState.value.cardForm
        val word = form.word.trim()
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Word is required.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val userId = authRepository.currentUser.first()?.uid
                    ?: error("You need to sign in before saving a card.")
                val definition = form.definition.trim()
                val card = Card(
                    id = UUID.randomUUID().toString(),
                    deckId = deckId,
                    userId = userId,
                    word = word,
                    pronunciation = form.phonetic.trim(),
                    meaning = form.meaning.trim(),
                    definition = definition,
                    descriptionEn = definition,
                    example = form.example.trim(),
                    collocation = form.collocation.trim(),
                    relatedWords = form.relatedWords.trim(),
                    note = form.note.trim(),
                    imageUrl = form.imageUrl.trim(),
                    audioUrl = form.audioUrl.trim(),
                    tags = form.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    createdAt = Date()
                )
                cardRepository.insertCard(card)
                
                // Update deck word count
                deckRepository.getDeck(deckId).first()?.let { deck ->
                    val updatedDeck = deck.copy(wordCount = deck.wordCount + 1)
                    deckRepository.updateDeck(updatedDeck)
                }

                // Reset form after save
                _uiState.update { it.copy(cardForm = CardFormState(), errorMessage = null) }
                onSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.localizedMessage ?: "Unable to save card.")
                }
            }
        }
    }

    fun updateCard(card: Card) {
        val word = card.word.trim()
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Word is required.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val descriptionEn = card.definition.ifBlank { card.descriptionEn }.trim()
                val normalizedCard = card.copy(
                    word = word,
                    pronunciation = card.pronunciation.trim(),
                    meaning = card.meaning.trim(),
                    definition = descriptionEn,
                    descriptionEn = descriptionEn,
                    example = card.example.trim(),
                    collocation = card.collocation.trim(),
                    relatedWords = card.relatedWords.trim(),
                    note = card.note.trim(),
                    imageUrl = card.imageUrl.trim(),
                    audioUrl = card.audioUrl.trim(),
                    tags = card.tags.map { it.trim() }.filter { it.isNotEmpty() }
                )
                cardRepository.updateCard(normalizedCard)
                _uiState.update { it.copy(errorMessage = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.localizedMessage ?: "Unable to update card.")
                }
            }
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            runCatching {
                cardRepository.deleteCard(card.id)
                deckRepository.getDeck(card.deckId).first()?.let { deck ->
                    deckRepository.updateDeck(deck.copy(wordCount = (deck.wordCount - 1).coerceAtLeast(0)))
                }
                _uiState.update { it.copy(errorMessage = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.localizedMessage ?: "Unable to delete card.")
                }
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
                            // Basic CSV split to avoid potential KSP/Compiler issues with complex regex
                            val parts = line.split(",")
                                .map { it.trim().removeSurrounding("\"") }

                                if (parts.size >= 3) {
                                    val word = parts.getOrNull(0)?.trim().orEmpty()
                                    if (word.isBlank()) {
                                        line = reader.readLine()
                                        continue
                                    }
                                    val definition = parts.getOrNull(3)?.trim() ?: ""
                                    val card = Card(
                                        id = UUID.randomUUID().toString(),
                                        deckId = deckId,
                                        userId = userId,
                                        word = word,
                                        pronunciation = parts.getOrNull(1)?.trim() ?: "",
                                        meaning = parts.getOrNull(2)?.trim() ?: "",
                                        definition = definition,
                                        descriptionEn = definition,
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
                var processedCount = 0
                for ((index, chunk) in cards.chunked(50).withIndex()) {
                    cardRepository.insertCards(chunk)
                    processedCount += chunk.size
                    val progress = processedCount.toFloat() / total
                    _uiState.update { 
                        it.copy(
                            importProgress = progress.coerceAtMost(1f),
                            importStatus = "Processing: ${processedCount.coerceAtMost(total)} / $total words"
                        )
                    }
                }
                
                _uiState.update { it.copy(isImporting = false, importStatus = "Success! $total words imported.") }
                
                // Update deck word count
                deckRepository.getDeck(deckId).first()?.let { deck ->
                    val updatedDeck = deck.copy(wordCount = deck.wordCount + total)
                    deckRepository.updateDeck(updatedDeck)
                }
                
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, importStatus = "Error: ${e.message}") }
            }
        }
    }

    fun exportDeck(context: Context, deckId: String, deckTitle: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val cards = cardRepository.getCardsInDeck(deckId).first()
                val csvContent = buildString {
                    append("Word,Phonetic,Meaning,Definition,Example,ImageUrl,AudioUrl,Tags\n")
                    cards.forEach { card ->
                        val definition = card.descriptionEn.ifBlank { card.definition }
                        append("${escapeCsv(card.word)},")
                        append("${escapeCsv(card.pronunciation)},")
                        append("${escapeCsv(card.meaning)},")
                        append("${escapeCsv(definition)},")
                        append("${escapeCsv(card.example)},")
                        append("${escapeCsv(card.imageUrl)},")
                        append("${escapeCsv(card.audioUrl)},")
                        append("${escapeCsv(card.tags.joinToString("|"))}\n")
                    }
                }

                saveFileToDownloads(context, "${deckTitle.replace(" ", "_")}_export.csv", csvContent)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Exported to Downloads folder") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Export failed: ${e.message}") }
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private suspend fun saveFileToDownloads(context: Context, fileName: String, content: String) {
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
            }

            val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                Uri.parse("content://downloads/public_downloads")
            }

            val uri = resolver.insert(collectionUri, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(content)
                    }
                }
            } ?: throw Exception("Could not create file in Downloads")
        }
    }
}

data class VocabularyUiState(
    val decks: List<Deck> = emptyList(),
    val cards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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
    val collocation: String = "",
    val relatedWords: String = "",
    val note: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val tags: String = ""
)
