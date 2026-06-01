package com.project.minlishapp.presentation.vocabulary.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.presentation.vocabulary.VocabularyViewModel

@Composable
fun ImportExportScreen(
    deckId: String,
    onClose: () -> Unit,
    viewModel: VocabularyViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.importCsv(context, it, deckId) }
        }
    )

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(color = Color.White)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(width = 32.dp)
                            .requiredHeight(height = 4.dp)
                            .clip(shape = RoundedCornerShape(9999.dp))
                            .background(color = Color(0xffe5e7eb))
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Manage Data",
                        color = Color(0xff1c1b1f),
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onClose) {
                        Text(text = "", style = TextStyle(fontSize = 20.sp), color = Color(0xff49454f))
                    }
                }
            }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "OPTIONS",
                            color = Color(0xff49454f),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                        )
                        
                        // Import Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xfff8f9fa))
                                .clickable { launcher.launch(arrayOf("text/*", "application/vnd.ms-excel")) }
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = Color(0xffeff6ff),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "", color = Color(0xff0061ff), style = TextStyle(fontSize = 16.sp))
                                }
                            }
                            Text(
                                text = "Import CSV via SAF",
                                color = Color(0xff1c1b1f),
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(start = 16.dp).weight(1f)
                            )
                            Text(text = "", color = Color(0xffd1d5db), style = TextStyle(fontSize = 14.sp))
                        }

                        // Export Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xfff8f9fa))
                                .clickable { /* TODO: Export Logic */ }
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = Color(0xffeff6ff),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "", color = Color(0xff0061ff), style = TextStyle(fontSize = 16.sp))
                                }
                            }
                            Text(
                                text = "Export CSV to Downloads",
                                color = Color(0xff1c1b1f),
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(start = 16.dp).weight(1f)
                            )
                            Text(text = "", color = Color(0xffd1d5db), style = TextStyle(fontSize = 14.sp))
                        }
                    }

                    if (uiState.isImporting) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Importing vocabulary...",
                                    color = Color(0xff49454f),
                                    style = TextStyle(fontSize = 14.sp)
                                )
                                Text(
                                    text = "${(uiState.importProgress * 100).toInt()}%",
                                    color = Color(0xff0061ff),
                                    style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                            LinearProgressIndicator(
                                progress = uiState.importProgress,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = Color(0xff0061ff),
                                trackColor = Color(0xffe0e0e0)
                            )
                            Text(
                                text = uiState.importStatus.uppercase(),
                                color = Color(0xff49454f),
                                style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                    
                    if (uiState.importStatus.contains("Success") || uiState.importStatus.contains("Error")) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (uiState.importStatus.contains("Success")) Color(0xfff0fdf4) else Color(0xfffef2f2))
                                .border(1.dp, if (uiState.importStatus.contains("Success")) Color(0xffdcfce7) else Color(0xfffee2e2), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (uiState.importStatus.contains("Success")) "" else "",
                                color = if (uiState.importStatus.contains("Success")) Color(0xff2e7d32) else Color(0xffdc2626),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = if (uiState.importStatus.contains("Success")) "Success" else "Status",
                                    color = if (uiState.importStatus.contains("Success")) Color(0xff2e7d32) else Color(0xffdc2626),
                                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = uiState.importStatus,
                                    color = Color(0xff49454f),
                                    style = TextStyle(fontSize = 12.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 375, heightDp = 840)
@Composable
private fun ImportExportScreenPreview() {
    ImportExportScreen("deck_id", {})
 }