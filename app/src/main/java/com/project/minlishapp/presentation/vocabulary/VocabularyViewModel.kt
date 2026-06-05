package com.project.minlishapp.presentation.vocabulary

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.data.remote.CardAutoFillService
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository,
    private val cardAutoFillService: CardAutoFillService
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
                            syncWordCounts(decks)
                        }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun syncWordCounts(decks: List<Deck>) {
        viewModelScope.launch {
            decks.forEach { deck ->
                try {
                    val cards = cardRepository.getCardsInDeck(deck.id).first()
                    val actualCount = cards.size
                    if (deck.wordCount != actualCount) {
                        deckRepository.updateDeck(deck.copy(wordCount = actualCount))
                    }
                } catch (e: Exception) {
                    // Ignore errors during sync
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearImportExportStatus() {
        _uiState.update {
            it.copy(
                importStatus = "",
                errorMessage = null
            )
        }
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
    fun addDeck(description: String, tags: List<String>) {
        viewModelScope.launch {
            val user = authRepository.currentUser.first()
            user?.uid?.let { userId ->
                val currentDecks = _uiState.value.decks
                val nextNumber = currentDecks
                    .mapNotNull { deck ->
                        if (deck.title.startsWith("New Deck ")) {
                            deck.title.removePrefix("New Deck ").toIntOrNull()
                        } else if (deck.title == "New Deck") {
                            1
                        } else null
                    }
                    .maxOrNull()?.plus(1) ?: (if (currentDecks.any { it.title == "New Deck" }) 2 else 1)

                val defaultTitle = if (nextNumber == 1 && currentDecks.none { it.title == "New Deck" }) {
                    "New Deck"
                } else {
                    "New Deck $nextNumber"
                }

                val deck = Deck(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = defaultTitle,
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

    fun clearCardForm() {
        _uiState.update {
            it.copy(
                cardForm = CardFormState(),
                autoFillMessage = null,
                errorMessage = null
            )
        }
    }

    fun loadCardIntoForm(card: Card) {
        _uiState.update {
            it.copy(
                cardForm = card.toFormState(),
                autoFillMessage = null,
                errorMessage = null
            )
        }
    }

    fun autoFillCardDetails() {
        val word = _uiState.value.cardForm.word.trim()
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a word before auto fill.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAutoFilling = true,
                    autoFillMessage = "Looking up \"$word\"...",
                    errorMessage = null
                )
            }
            runCatching {
                cardAutoFillService.lookup(word)
            }.onSuccess { result ->
                _uiState.update { state ->
                    state.copy(
                        cardForm = state.cardForm.mergeAutoFill(result),
                        isAutoFilling = false,
                        autoFillMessage = if (result.hasAnyData) {
                            "Auto fill completed. Review the details before saving."
                        } else {
                            "No public data found. You can still save the word manually."
                        },
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isAutoFilling = false,
                        autoFillMessage = null,
                        errorMessage = throwable.localizedMessage
                            ?: "Unable to auto fill this word."
                    )
                }
            }
        }
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
                    audioUrlUs = form.audioUrlUs.trim(),
                    audioUrlUk = form.audioUrlUk.trim(),
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
                _uiState.update {
                    it.copy(
                        cardForm = CardFormState(),
                        autoFillMessage = null,
                        errorMessage = null
                    )
                }
                onSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.localizedMessage ?: "Unable to save card.")
                }
            }
        }
    }

    fun updateCardFromForm(
        originalCard: Card,
        onSaved: () -> Unit = {}
    ) {
        val form = _uiState.value.cardForm
        val word = form.word.trim()
        if (word.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Word is required.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val definition = form.definition.trim()
                val normalizedCard = originalCard.copy(
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
                    audioUrlUs = form.audioUrlUs.trim(),
                    audioUrlUk = form.audioUrlUk.trim(),
                    tags = form.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                )
                cardRepository.updateCard(normalizedCard)
                _uiState.update {
                    it.copy(
                        cardForm = CardFormState(),
                        autoFillMessage = null,
                        errorMessage = null
                    )
                }
                onSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.localizedMessage ?: "Unable to update card.")
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
                    audioUrlUs = card.audioUrlUs.trim(),
                    audioUrlUk = card.audioUrlUk.trim(),
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

    // CSV Import/Export Logic
    fun importCsv(context: Context, uri: Uri, deckId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImporting = true,
                    importProgress = 0f,
                    importStatus = "Reading CSV file...",
                    errorMessage = null
                )
            }

            val userId = authRepository.currentUser.first()?.uid
            if (userId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importStatus = "Import failed: you need to sign in first.",
                        errorMessage = "You need to sign in before importing cards."
                    )
                }
                return@launch
            }

            runCatching {
                val importResult = withContext(Dispatchers.IO) {
                    val csvContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        inputStream.readBytes().toString(Charsets.UTF_8)
                    } ?: error("Cannot open the selected CSV file.")
                    parseCardsFromCsv(
                        csvContent = csvContent,
                        deckId = deckId,
                        userId = userId
                    )
                }

                if (importResult.cards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importProgress = 0f,
                            importStatus = "Import failed: no valid cards found. The CSV needs at least a Word column."
                        )
                    }
                    return@launch
                }

                var processedCount = 0
                importResult.cards.chunked(CSV_IMPORT_CHUNK_SIZE).forEach { chunk ->
                    cardRepository.insertCards(chunk)
                    processedCount += chunk.size
                    val progress = processedCount.toFloat() / importResult.cards.size.toFloat()
                    _uiState.update {
                        it.copy(
                            importProgress = progress.coerceIn(0f, 1f),
                            importStatus = "Importing $processedCount / ${importResult.cards.size} cards..."
                        )
                    }
                }

                deckRepository.getDeck(deckId).first()?.let { deck ->
                    val actualCount = cardRepository.getCardsInDeck(deckId).first().size
                    deckRepository.updateDeck(deck.copy(wordCount = actualCount))
                }

                val skippedMessage = if (importResult.skippedRows > 0) {
                    " Skipped ${importResult.skippedRows} invalid rows."
                } else {
                    ""
                }
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importProgress = 1f,
                        importStatus = "Import completed: ${importResult.cards.size} cards added.$skippedMessage",
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importProgress = 0f,
                        importStatus = "Import failed: ${throwable.localizedMessage ?: "Unknown error"}",
                        errorMessage = throwable.localizedMessage ?: "Import failed."
                    )
                }
            }
        }
    }

    fun exportDeck(context: Context, deckId: String, deckTitle: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    importStatus = "Preparing CSV export...",
                    errorMessage = null
                )
            }
            runCatching {
                val cards = cardRepository.getCardsInDeck(deckId).first()
                if (cards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            importStatus = "Export skipped: this deck has no cards.",
                            errorMessage = "This deck has no cards to export."
                        )
                    }
                    return@launch
                }

                val csvContent = buildCardsCsv(cards)
                val fileName = buildExportFileName(deckTitle)
                saveFileToDownloads(context, fileName, csvContent)

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        importStatus = "Export completed: ${cards.size} cards saved to Downloads/$fileName.",
                        errorMessage = "Exported ${cards.size} cards to Downloads/$fileName"
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        importStatus = "Export failed: ${throwable.localizedMessage ?: "Unknown error"}",
                        errorMessage = throwable.localizedMessage ?: "Export failed."
                    )
                }
            }
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
                    OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(content)
                    }
                }
            } ?: throw Exception("Could not create file in Downloads")
        }
    }
}

private data class CsvImportResult(
    val cards: List<Card>,
    val skippedRows: Int
)

private fun buildCardsCsv(cards: List<Card>): String {
    val builder = StringBuilder()
    builder.append('\uFEFF')
    builder.appendLine(toCsvRow(CARD_CSV_HEADERS))
    cards.forEach { card ->
        builder.appendLine(
            toCsvRow(
                listOf(
                    card.word,
                    card.pronunciation,
                    card.meaning,
                    card.definition,
                    card.descriptionEn.ifBlank { card.definition },
                    card.example,
                    card.collocation,
                    card.relatedWords,
                    card.note,
                    card.imageUrl,
                    card.audioUrl,
                    card.audioUrlUs,
                    card.audioUrlUk,
                    card.tags.joinToString("|")
                )
            )
        )
    }
    return builder.toString()
}

private fun parseCardsFromCsv(
    csvContent: String,
    deckId: String,
    userId: String
): CsvImportResult {
    val rows = parseCsvRows(csvContent)
        .filterNot { row -> row.all { it.isBlank() } }
    if (rows.isEmpty()) return CsvImportResult(emptyList(), skippedRows = 0)

    val firstRow = rows.first()
    val hasHeader = firstRow.any { normalizeCsvHeader(it) == CSV_KEY_WORD } &&
        firstRow.count { normalizeCsvHeader(it) in CSV_HEADER_ALIASES } >= 2
    val headerIndexes = if (hasHeader) {
        firstRow.mapIndexedNotNull { index, header ->
            CSV_HEADER_ALIASES[normalizeCsvHeader(header)]?.let { key -> key to index }
        }.toMap()
    } else {
        emptyMap()
    }
    val dataRows = if (hasHeader) rows.drop(1) else rows

    val cards = mutableListOf<Card>()
    var skippedRows = 0
    dataRows.forEach { row ->
        val isNewSchemaWithoutHeader = !hasHeader && row.size >= CARD_CSV_HEADERS.size
        fun cell(
            key: String,
            legacyIndex: Int,
            newSchemaIndex: Int = legacyIndex
        ): String {
            val index = if (hasHeader) {
                headerIndexes[key]
            } else if (isNewSchemaWithoutHeader) {
                newSchemaIndex
            } else {
                legacyIndex
            }
            return index?.let { row.getOrNull(it) }.orEmpty().trim()
        }

        val word = cell(CSV_KEY_WORD, legacyIndex = 0, newSchemaIndex = 0)
        if (word.isBlank()) {
            skippedRows++
            return@forEach
        }

        val rawDefinition = cell(CSV_KEY_DEFINITION, legacyIndex = 3, newSchemaIndex = 3)
        val rawDescriptionEn = cell(CSV_KEY_DESCRIPTION_EN, legacyIndex = 3, newSchemaIndex = 4)
        val definition = rawDefinition.ifBlank { rawDescriptionEn }
        val descriptionEn = rawDescriptionEn.ifBlank { definition }
        val now = Date()
        cards += Card(
            id = UUID.randomUUID().toString(),
            deckId = deckId,
            userId = userId,
            word = word,
            pronunciation = cell(CSV_KEY_PRONUNCIATION, legacyIndex = 1, newSchemaIndex = 1),
            meaning = cell(CSV_KEY_MEANING, legacyIndex = 2, newSchemaIndex = 2),
            definition = definition,
            descriptionEn = descriptionEn,
            example = cell(CSV_KEY_EXAMPLE, legacyIndex = 4, newSchemaIndex = 5),
            collocation = cell(CSV_KEY_COLLOCATION, legacyIndex = -1, newSchemaIndex = 6),
            relatedWords = cell(CSV_KEY_RELATED_WORDS, legacyIndex = -1, newSchemaIndex = 7),
            note = cell(CSV_KEY_NOTE, legacyIndex = -1, newSchemaIndex = 8),
            imageUrl = cell(CSV_KEY_IMAGE_URL, legacyIndex = 5, newSchemaIndex = 9),
            audioUrl = cell(CSV_KEY_AUDIO_URL, legacyIndex = 6, newSchemaIndex = 10),
            audioUrlUs = cell(CSV_KEY_AUDIO_URL_US, legacyIndex = -1, newSchemaIndex = 11),
            audioUrlUk = cell(CSV_KEY_AUDIO_URL_UK, legacyIndex = -1, newSchemaIndex = 12),
            tags = parseTags(cell(CSV_KEY_TAGS, legacyIndex = 7, newSchemaIndex = 13)),
            createdAt = now,
            nextReviewTime = now
        )
    }

    return CsvImportResult(cards = cards, skippedRows = skippedRows)
}

private fun parseCsvRows(csvContent: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val row = mutableListOf<String>()
    val field = StringBuilder()
    var inQuotes = false
    var index = 0

    fun endField() {
        row += field.toString()
        field.setLength(0)
    }

    fun endRow() {
        endField()
        rows += row.toList()
        row.clear()
    }

    while (index < csvContent.length) {
        when (val char = csvContent[index]) {
            '"' -> {
                if (inQuotes && index + 1 < csvContent.length && csvContent[index + 1] == '"') {
                    field.append('"')
                    index++
                } else {
                    inQuotes = !inQuotes
                }
            }
            ',' -> {
                if (inQuotes) {
                    field.append(char)
                } else {
                    endField()
                }
            }
            '\r' -> {
                if (inQuotes) {
                    field.append(char)
                } else {
                    endRow()
                    if (index + 1 < csvContent.length && csvContent[index + 1] == '\n') {
                        index++
                    }
                }
            }
            '\n' -> {
                if (inQuotes) {
                    field.append(char)
                } else {
                    endRow()
                }
            }
            else -> field.append(char)
        }
        index++
    }

    if (field.isNotEmpty() || row.isNotEmpty()) {
        endRow()
    }
    return rows
}

private fun toCsvRow(values: List<String>): String {
    return values.joinToString(",") { escapeCsv(it) }
}

private fun escapeCsv(value: String): String {
    val normalizedValue = value.replace("\r\n", "\n").replace("\r", "\n")
    return if (
        normalizedValue.contains(",") ||
        normalizedValue.contains("\"") ||
        normalizedValue.contains("\n")
    ) {
        "\"${normalizedValue.replace("\"", "\"\"")}\""
    } else {
        normalizedValue
    }
}

private fun parseTags(value: String): List<String> {
    return value
        .split("|", ";", ",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase(Locale.US) }
}

private fun normalizeCsvHeader(header: String): String {
    return header
        .trim()
        .removePrefix("\uFEFF")
        .lowercase(Locale.US)
        .filter { it.isLetterOrDigit() }
}

private fun buildExportFileName(deckTitle: String): String {
    val safeTitle = deckTitle
        .trim()
        .ifBlank { "deck" }
        .replace(Regex("""[\\/:*?"<>|]+"""), "_")
        .replace(Regex("""\s+"""), "_")
        .take(80)
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
    return "${safeTitle}_minlish_$timestamp.csv"
}

data class VocabularyUiState(
    val decks: List<Deck> = emptyList(),
    val cards: List<Card> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAutoFilling: Boolean = false,
    val autoFillMessage: String? = null,
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
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
    val audioUrlUs: String = "",
    val audioUrlUk: String = "",
    val tags: String = ""
)

private fun Card.toFormState(): CardFormState {
    return CardFormState(
        word = word,
        phonetic = pronunciation,
        meaning = meaning,
        definition = descriptionEn.ifBlank { definition },
        example = example,
        collocation = collocation,
        relatedWords = relatedWords,
        note = note,
        imageUrl = imageUrl,
        audioUrl = audioUrl,
        audioUrlUs = audioUrlUs,
        audioUrlUk = audioUrlUk,
        tags = tags.joinToString(", ")
    )
}

private fun CardFormState.mergeAutoFill(
    result: com.project.minlishapp.data.remote.CardAutoFillResult
): CardFormState {
    val shouldReplaceExample = example.isBlank() || example.isOldGeneratedExampleFor(word)
    return copy(
        phonetic = phonetic.ifBlank { result.pronunciation },
        meaning = meaning.ifBlank { result.meaning },
        definition = definition.ifBlank { result.definition },
        example = if (shouldReplaceExample) result.example.ifBlank { example } else example,
        collocation = collocation.ifBlank { result.collocation },
        relatedWords = relatedWords.ifBlank { result.relatedWords },
        imageUrl = imageUrl.ifBlank { result.imageUrl },
        audioUrl = audioUrl.ifBlank { result.audioUrl },
        audioUrlUs = audioUrlUs.ifBlank { result.audioUrlUs },
        audioUrlUk = audioUrlUk.ifBlank { result.audioUrlUk },
        tags = tags.ifBlank { result.tags }
    )
}

private fun String.isOldGeneratedExampleFor(word: String): Boolean {
    val normalizedWord = word.trim()
    return normalizedWord.isNotBlank() &&
        trim().equals(
            "This sentence uses the word $normalizedWord in context.",
            ignoreCase = true
        )
}

private val com.project.minlishapp.data.remote.CardAutoFillResult.hasAnyData: Boolean
    get() = listOf(
        pronunciation,
        meaning,
        definition,
        example,
        collocation,
        relatedWords,
        tags,
        imageUrl,
        audioUrl,
        audioUrlUs,
        audioUrlUk
    ).any(String::isNotBlank)

private const val CSV_IMPORT_CHUNK_SIZE = 50
private const val CSV_KEY_WORD = "word"
private const val CSV_KEY_PRONUNCIATION = "pronunciation"
private const val CSV_KEY_MEANING = "meaning"
private const val CSV_KEY_DEFINITION = "definition"
private const val CSV_KEY_DESCRIPTION_EN = "descriptionEn"
private const val CSV_KEY_EXAMPLE = "example"
private const val CSV_KEY_COLLOCATION = "collocation"
private const val CSV_KEY_RELATED_WORDS = "relatedWords"
private const val CSV_KEY_NOTE = "note"
private const val CSV_KEY_IMAGE_URL = "imageUrl"
private const val CSV_KEY_AUDIO_URL = "audioUrl"
private const val CSV_KEY_AUDIO_URL_US = "audioUrlUs"
private const val CSV_KEY_AUDIO_URL_UK = "audioUrlUk"
private const val CSV_KEY_TAGS = "tags"

private val CARD_CSV_HEADERS = listOf(
    "Word",
    "Pronunciation",
    "Meaning",
    "Definition",
    "DescriptionEn",
    "Example",
    "Collocation",
    "RelatedWords",
    "Note",
    "ImageUrl",
    "AudioUrl",
    "AudioUrlUs",
    "AudioUrlUk",
    "Tags"
)

private val CSV_HEADER_ALIASES = mapOf(
    "word" to CSV_KEY_WORD,
    "term" to CSV_KEY_WORD,
    "vocabulary" to CSV_KEY_WORD,
    "pronunciation" to CSV_KEY_PRONUNCIATION,
    "phonetic" to CSV_KEY_PRONUNCIATION,
    "ipa" to CSV_KEY_PRONUNCIATION,
    "meaning" to CSV_KEY_MEANING,
    "vietnamese" to CSV_KEY_MEANING,
    "vietnamesemeaning" to CSV_KEY_MEANING,
    "definition" to CSV_KEY_DEFINITION,
    "description" to CSV_KEY_DESCRIPTION_EN,
    "descriptionen" to CSV_KEY_DESCRIPTION_EN,
    "englishdescription" to CSV_KEY_DESCRIPTION_EN,
    "descriptionenglish" to CSV_KEY_DESCRIPTION_EN,
    "example" to CSV_KEY_EXAMPLE,
    "sentence" to CSV_KEY_EXAMPLE,
    "collocation" to CSV_KEY_COLLOCATION,
    "collocations" to CSV_KEY_COLLOCATION,
    "relatedword" to CSV_KEY_RELATED_WORDS,
    "relatedwords" to CSV_KEY_RELATED_WORDS,
    "related" to CSV_KEY_RELATED_WORDS,
    "note" to CSV_KEY_NOTE,
    "notes" to CSV_KEY_NOTE,
    "image" to CSV_KEY_IMAGE_URL,
    "imageurl" to CSV_KEY_IMAGE_URL,
    "picture" to CSV_KEY_IMAGE_URL,
    "audio" to CSV_KEY_AUDIO_URL,
    "audiourl" to CSV_KEY_AUDIO_URL,
    "audious" to CSV_KEY_AUDIO_URL_US,
    "audiourlus" to CSV_KEY_AUDIO_URL_US,
    "audiousurl" to CSV_KEY_AUDIO_URL_US,
    "audiouk" to CSV_KEY_AUDIO_URL_UK,
    "audiourluk" to CSV_KEY_AUDIO_URL_UK,
    "audioukurl" to CSV_KEY_AUDIO_URL_UK,
    "tags" to CSV_KEY_TAGS,
    "tag" to CSV_KEY_TAGS
)
