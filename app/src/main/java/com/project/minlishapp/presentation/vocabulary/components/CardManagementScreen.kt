package com.project.minlishapp.presentation.vocabulary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.presentation.vocabulary.*

@Composable
fun CardManagementScreen(
    deckId: String,
    onBack: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.cardForm

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(start = 12.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Text(
                    text = if (form.word.isEmpty()) "Add New Card" else "Edit Card",
                    color = Color(0xff1c1b1f),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                item {
                    FormField(
                        label = "Word",
                        value = form.word,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(word = newValue) } }
                    )
                }
                item {
                    FormField(
                        label = "Phonetic",
                        value = form.phonetic,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(phonetic = newValue) } }
                    )
                }
                item {
                    FormField(
                        label = "Meaning",
                        value = form.meaning,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(meaning = newValue) } }
                    )
                }
                item {
                    FormField(
                        label = "Description (English)",
                        value = form.definition,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(definition = newValue) } },
                        minHeight = 80.dp
                    )
                }
                item {
                    FormField(
                        label = "Example",
                        value = form.example,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(example = newValue) } },
                        minHeight = 80.dp
                    )
                }
                item {
                    FormField(
                        label = "Collocation",
                        value = form.collocation,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(collocation = newValue) } }
                    )
                }
                item {
                    FormField(
                        label = "Related Words",
                        value = form.relatedWords,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(relatedWords = newValue) } }
                    )
                }
                item {
                    FormField(
                        label = "Note",
                        value = form.note,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(note = newValue) } },
                        minHeight = 80.dp
                    )
                }
                item {
                    FormField(
                        label = "Tags (comma separated)",
                        value = form.tags,
                        onValueChange = { newValue -> viewModel.updateCardForm { it.copy(tags = newValue) } }
                    )
                }
                
                item {
                    Box(modifier = Modifier.height(100.dp)) // Padding for bottom button
                }
            }
        }

        // Save Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp)
                .clickable { 
                    viewModel.saveCard(deckId)
                    onBack()
                },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xff0061ff),
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Save Card",
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color(0xff0061ff),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xfff8f9fa),
                unfocusedContainerColor = Color(0xfff8f9fa),
                focusedIndicatorColor = Color(0xff0061ff),
                unfocusedIndicatorColor = Color(0xffe0e0e0)
            )
        )
    }
}

@Preview(widthDp = 375, heightDp = 840)
@Composable
private fun CardManagementScreenPreview() {
    CardManagementScreen("deck_id", {})
 }