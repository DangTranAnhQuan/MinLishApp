package com.project.minlishapp.presentation.vocabulary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.presentation.vocabulary.*
import kotlinx.coroutines.launch

@Composable
fun CardManagementScreen(
    deckId: String,
    onBack: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val form = uiState.cardForm
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe error message and show snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 80.dp) // Offset to stay above the Save Card button
            ) 
        },
        containerColor = Color.White,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column {
                // Top Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color.White)
                        .padding(start = 12.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
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
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormField(
                                    label = "Word",
                                    value = form.word,
                                    onValueChange = { newValue -> viewModel.updateCardForm { it.copy(word = newValue) } }
                                )
                            }
                            IconButton(
                                onClick = { viewModel.fetchWordDetails(form.word) },
                                enabled = form.word.isNotBlank() && !form.isFetching,
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(
                                        if (form.word.isNotBlank()) Color(0xff0061ff).copy(alpha = 0.1f)
                                        else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                if (form.isFetching) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Fetch details",
                                        tint = if (form.word.isNotBlank()) Color(0xff0061ff) else Color.Gray
                                    )
                                }
                            }
                        }
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
                        viewModel.saveCard(deckId, onSaved = { message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }) 
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
