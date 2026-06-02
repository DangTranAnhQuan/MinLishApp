package com.project.minlishapp.presentation.vocabulary.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.presentation.vocabulary.VocabularyViewModel

@Composable
fun DeckManagementScreen(
    onImportExportClick: () -> Unit,
    onDeckClick: (String) -> Unit,
    onAddCardClick: (String) -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedDeckIdForImport by remember { mutableStateOf<String?>(null) }

    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedDeckIdForImport?.let { deckId ->
                viewModel.importCsv(context, it, deckId)
            }
        }
    }

    if (uiState.isImporting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Importing Cards") },
            text = {
                Column {
                    LinearProgressIndicator(
                        progress = { uiState.importProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(uiState.importStatus)
                }
            },
            confirmButton = { }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "My Decks",
                    color = Color(0xff1c1b1f),
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.55).sp
                    )
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // Error/Status Message
                uiState.errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { viewModel.onSearchQueryChange(uiState.searchQuery) /* hack to clear msg */ }) {
                                Text("OK")
                            }
                        }
                    }
                }

                // Search Bar
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search decks...", color = Color(0xff49454f).copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xff49454f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xfff8f9fa)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                // Decks Grid
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.decks.filter { it.title.contains(uiState.searchQuery, ignoreCase = true) }) { deck ->
                            DeckItem(
                                deck = deck,
                                onClick = { onDeckClick(deck.id) },
                                onDelete = { viewModel.deleteDeck(deck.id) },
                                onImport = {
                                    selectedDeckIdForImport = deck.id
                                    csvPickerLauncher.launch("text/*")
                                },
                                onExport = {
                                    viewModel.exportDeck(context, deck.id, deck.title)
                                }
                            )
                        }
                        item {
                            NewDeckCard(onClick = { viewModel.addDeck("New Deck", "", emptyList()) })
                        }
                    }
                }
            }
        }

        // FAB for quickly adding card to a default or last used deck
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xff0061ff),
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(16.dp))
                .clickable { uiState.decks.firstOrNull()?.let { onAddCardClick(it.id) } }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.requiredSize(size = 56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun DeckItem(
    deck: Deck, 
    onClick: () -> Unit, 
    onDelete: () -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xfff9fafb)),
        modifier = Modifier
            .requiredHeight(180.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                Column {
                    Text(
                        text = deck.title,
                        color = Color(0xff1c1b1f),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xff0061ff).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = "${deck.wordCount} cards",
                            color = Color(0xff0061ff).copy(alpha = 0.7f),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                    Text(
                        text = deck.description,
                        color = Color(0xff49454f),
                        style = TextStyle(fontSize = 13.sp),
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    deck.tags.take(2).forEach { tag ->
                        TagItem(tag)
                    }
                }
            }
            
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color(0xff49454f))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Import CSV") }, 
                        onClick = { onImport(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Export CSV") }, 
                        onClick = { onExport(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") }, 
                        onClick = { onDelete(); showMenu = false }
                    )
                }
            }
        }
    }
}

@Composable
fun TagItem(tag: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xffe3f2fd),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "#$tag",
            color = Color(0xff0061ff),
            style = TextStyle(fontSize = 10.sp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun NewDeckCard(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(2.dp, Color(0xfff3f4f6)),
        modifier = Modifier
            .requiredHeight(180.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color(0xffeff6ff),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xff0061ff),
                    modifier = Modifier.padding(8.dp)
                )
            }
            Text(
                text = "New Deck",
                color = Color(0xff0061ff),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun BottomNav(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(height = 64.dp)
            .background(color = Color.White)
            .shadow(elevation = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        NavItem("Decks", "", true)
        NavItem("Learn", "", false)
        NavItem("Stats", "", false)
        NavItem("Profile", "", false)
    }
}

@Composable
fun NavItem(label: String, icon: String, selected: Boolean) {
    val color = if (selected) Color(0xff0061ff) else Color(0xff49454f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = icon, color = color, style = TextStyle(fontSize = 20.sp))
        Text(text = label, color = color, style = TextStyle(fontSize = 10.sp))
    }
}

@Preview(widthDp = 375, heightDp = 840)
@Composable
private fun DeckManagementScreenPreview() {
    DeckManagementScreen({}, {}, {})
 }