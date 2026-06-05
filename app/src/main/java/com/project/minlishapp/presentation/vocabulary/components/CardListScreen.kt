package com.project.minlishapp.presentation.vocabulary.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.core.utils.PronunciationAccent
import com.project.minlishapp.core.utils.TextToSpeechHelper
import com.project.minlishapp.presentation.vocabulary.*
import com.project.minlishapp.domain.model.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun CardListScreen(
    deckId: String,
    onBack: () -> Unit,
    onAddCardClick: (String) -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    var editingCard by remember { mutableStateOf<Card?>(null) }
    var deletingCard by remember { mutableStateOf<Card?>(null) }
    var selectedCard by remember { mutableStateOf<Card?>(null) }
    var cardSearchQuery by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    LaunchedEffect(deckId) {
        viewModel.loadCards(deckId)
    }

    val deck = uiState.decks.find { it.id == deckId }
    val filteredCards = remember(uiState.cards, cardSearchQuery) {
        val query = cardSearchQuery.trim()
        if (query.isBlank()) {
            uiState.cards
        } else {
            uiState.cards.filter { card ->
                card.word.contains(query, ignoreCase = true) ||
                    card.meaning.contains(query, ignoreCase = true) ||
                    card.pronunciation.contains(query, ignoreCase = true) ||
                    card.definition.contains(query, ignoreCase = true) ||
                    card.descriptionEn.contains(query, ignoreCase = true) ||
                    card.example.contains(query, ignoreCase = true) ||
                    card.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }

    fun speakCard(card: Card, accent: PronunciationAccent = PronunciationAccent.US) {
        val audioUrl = when (accent) {
            PronunciationAccent.US -> card.audioUrlUs.ifBlank { card.audioUrl }
            PronunciationAccent.UK -> card.audioUrlUk.ifBlank { card.audioUrl }
        }
        ttsHelper.speak(
            text = card.word,
            audioUrl = audioUrl,
            accent = accent
        )
    }

    editingCard?.let { card ->
        EditCardDialog(
            card = card,
            form = uiState.cardForm,
            isAutoFilling = uiState.isAutoFilling,
            autoFillMessage = uiState.autoFillMessage,
            errorMessage = uiState.errorMessage,
            onFormChange = viewModel::updateCardForm,
            onAutoFill = viewModel::autoFillCardDetails,
            onClear = viewModel::clearCardForm,
            onDismiss = {
                viewModel.clearCardForm()
                editingCard = null
            },
            onSave = {
                viewModel.updateCardFromForm(card) {
                    editingCard = null
                }
            }
        )
    }
    selectedCard?.let { card ->
        CardDetailDialog(
            card = card,
            onDismiss = { selectedCard = null },
            onSpeakUs = { speakCard(card, PronunciationAccent.US) },
            onSpeakUk = { speakCard(card, PronunciationAccent.UK) },
            onEdit = {
                selectedCard = null
                viewModel.loadCardIntoForm(card)
                editingCard = card
            }
        )
    }
    deletingCard?.let { card ->
        AlertDialog(
            onDismissRequest = { deletingCard = null },
            title = { Text("Delete card") },
            text = { Text("Delete \"${card.word}\" from this deck?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCard(card)
                        deletingCard = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCard = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Column {
                    Text(
                        text = deck?.title ?: "Deck Details",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (cardSearchQuery.isBlank()) {
                            "${uiState.cards.size} cards"
                        } else {
                            "${filteredCards.size}/${uiState.cards.size} cards"
                        },
                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                }
            }

            OutlinedTextField(
                value = cardSearchQuery,
                onValueChange = { cardSearchQuery = it },
                placeholder = { Text("Search words in this deck") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xff0061ff)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No cards in this deck yet.")
                }
            } else if (filteredCards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No words match your search.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCards, key = { it.id }) { card ->
                        CardItem(
                            card = card,
                            onOpenDetails = { selectedCard = card },
                            onEdit = {
                                viewModel.loadCardIntoForm(card)
                                editingCard = card
                            },
                            onDelete = { deletingCard = card },
                            onSpeak = { speakCard(card) }
                        )
                    }
                }
            }
        }

        // FAB to add new card
        FloatingActionButton(
            onClick = { onAddCardClick(deckId) },
            containerColor = Color(0xff0061ff),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Card")
        }
    }
}

@Composable
fun CardItem(
    card: Card,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetails)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSpeak) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak word",
                        tint = Color(0xff0061ff)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = card.word,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                    if (card.pronunciation.isNotEmpty()) {
                        Text(
                            text = card.pronunciation,
                            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.meaning,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Card options", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit card") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete card") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardDetailDialog(
    card: Card,
    onDismiss: () -> Unit,
    onSpeakUs: () -> Unit,
    onSpeakUk: () -> Unit,
    onEdit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(card.word, fontWeight = FontWeight.Bold)
                card.pronunciation.takeIf { it.isNotBlank() }?.let { pronunciation ->
                    Text(
                        pronunciation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RemoteCardImage(
                    imageUrl = card.imageUrl,
                    contentDescription = "${card.word} image"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSpeakUs,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("US")
                    }
                    OutlinedButton(
                        onClick = onSpeakUk,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UK")
                    }
                }

                DetailRow(label = "Meaning", value = card.meaning)
                DetailRow(label = "Description", value = card.descriptionEn.ifBlank { card.definition })
                DetailRow(label = "Example", value = card.example)
                DetailRow(label = "Collocation", value = card.collocation)
                DetailRow(label = "Related words", value = card.relatedWords)
                DetailRow(label = "Note", value = card.note)
                card.tags.takeIf { it.isNotEmpty() }?.let { tags ->
                    DetailRow(label = "Tags", value = tags.joinToString(", "))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onEdit) {
                Text("Edit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    val displayValue = value.trim()
    if (displayValue.isBlank()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xfff8f9fa)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = Color(0xff0061ff),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayValue,
                color = Color(0xff1f2937),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RemoteCardImage(
    imageUrl: String,
    contentDescription: String
) {
    val normalizedUrl = imageUrl.trim()
    if (normalizedUrl.isBlank()) return

    var imageBitmap by remember(normalizedUrl) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }

    LaunchedEffect(normalizedUrl) {
        imageBitmap = runCatching {
            withContext(Dispatchers.IO) {
                val connection = URL(normalizedUrl).openConnection().apply {
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                connection.getInputStream().use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }
        }.getOrNull()
    }

    imageBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun EditCardDialog(
    card: Card,
    form: CardFormState,
    isAutoFilling: Boolean,
    autoFillMessage: String?,
    errorMessage: String?,
    onFormChange: ((CardFormState) -> CardFormState) -> Unit,
    onAutoFill: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var showAdvanced by remember(card.id) { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit card") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormField(
                    label = "Word",
                    value = form.word,
                    onValueChange = { newValue -> onFormChange { it.copy(word = newValue) } }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoFill,
                        enabled = form.word.isNotBlank() && !isAutoFilling,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xff0061ff))
                    ) {
                        if (isAutoFilling) {
                            CircularProgressIndicator(
                                modifier = Modifier.requiredSize(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Text("  Autofill...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Autofill", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = onClear,
                        enabled = !isAutoFilling,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Clear all", fontWeight = FontWeight.Bold)
                    }
                }

                autoFillMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xff4b5563),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedButton(
                    onClick = { showAdvanced = !showAdvanced },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (showAdvanced) "Hide advanced fields" else "Show advanced fields",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showAdvanced) {
                    FormField(
                        label = "Phonetic",
                        value = form.phonetic,
                        onValueChange = { newValue -> onFormChange { it.copy(phonetic = newValue) } }
                    )
                    FormField(
                        label = "Meaning",
                        value = form.meaning,
                        onValueChange = { newValue -> onFormChange { it.copy(meaning = newValue) } }
                    )
                    FormField(
                        label = "Description (English)",
                        value = form.definition,
                        onValueChange = { newValue -> onFormChange { it.copy(definition = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Example",
                        value = form.example,
                        onValueChange = { newValue -> onFormChange { it.copy(example = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Collocation",
                        value = form.collocation,
                        onValueChange = { newValue -> onFormChange { it.copy(collocation = newValue) } }
                    )
                    FormField(
                        label = "Related Words",
                        value = form.relatedWords,
                        onValueChange = { newValue -> onFormChange { it.copy(relatedWords = newValue) } }
                    )
                    FormField(
                        label = "Image URL",
                        value = form.imageUrl,
                        onValueChange = { newValue -> onFormChange { it.copy(imageUrl = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Audio URL",
                        value = form.audioUrl,
                        onValueChange = { newValue -> onFormChange { it.copy(audioUrl = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Audio URL (US)",
                        value = form.audioUrlUs,
                        onValueChange = { newValue -> onFormChange { it.copy(audioUrlUs = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Audio URL (UK)",
                        value = form.audioUrlUk,
                        onValueChange = { newValue -> onFormChange { it.copy(audioUrlUk = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Note",
                        value = form.note,
                        onValueChange = { newValue -> onFormChange { it.copy(note = newValue) } },
                        minHeight = 80.dp
                    )
                    FormField(
                        label = "Tags (comma separated)",
                        value = form.tags,
                        onValueChange = { newValue -> onFormChange { it.copy(tags = newValue) } }
                    )
                }

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = form.word.isNotBlank() && !isAutoFilling
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
