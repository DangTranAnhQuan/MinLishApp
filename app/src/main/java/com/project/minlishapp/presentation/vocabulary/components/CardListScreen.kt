package com.project.minlishapp.presentation.vocabulary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.presentation.vocabulary.*
import com.project.minlishapp.domain.model.Card

@Composable
fun CardListScreen(
    deckId: String,
    onBack: () -> Unit,
    onAddCardClick: (String) -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(deckId) {
        viewModel.loadCards(deckId)
    }

    val deck = uiState.decks.find { it.id == deckId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
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
                        text = "${uiState.cards.size} cards",
                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No cards in this deck yet.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.cards) { card ->
                        CardItem(card = card)
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
fun CardItem(card: Card) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            IconButton(onClick = { /* TODO: More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}
