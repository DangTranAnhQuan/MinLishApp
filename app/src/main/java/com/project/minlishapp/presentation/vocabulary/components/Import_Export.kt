package com.project.minlishapp.presentation.vocabulary.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val deckTitle = uiState.decks.firstOrNull { it.id == deckId }?.title ?: "deck"

    val csvPicker = rememberLauncherForActivityResult(
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .requiredWidth(36.dp)
                            .requiredHeight(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xffe5e7eb))
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Import / Export",
                            color = Color(0xff1c1b1f),
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "CSV format for MinLish cards",
                            color = Color(0xff6b7280),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedButton(onClick = onClose, shape = RoundedCornerShape(14.dp)) {
                        Text("Close")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xfff8fafc)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("CSV columns", fontWeight = FontWeight.Bold, color = Color(0xff1f2937))
                        Text(
                            text = "Word, Pronunciation, Meaning, Definition, DescriptionEn, Example, Collocation, RelatedWords, Note, ImageUrl, AudioUrl, AudioUrlUs, AudioUrlUk, Tags",
                            color = Color(0xff4b5563),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Button(
                    onClick = {
                        csvPicker.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "text/*",
                                "application/vnd.ms-excel"
                            )
                        )
                    },
                    enabled = !uiState.isImporting && !uiState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff0061ff))
                ) {
                    Text("Import CSV")
                }

                OutlinedButton(
                    onClick = { viewModel.exportDeck(context, deckId, deckTitle) },
                    enabled = !uiState.isImporting && !uiState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Export CSV to Downloads")
                }

                if (uiState.isImporting || uiState.isExporting) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                            Text(
                                text = if (uiState.isImporting) "Importing..." else "Exporting...",
                                color = Color(0xff4b5563),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (uiState.isImporting) {
                            LinearProgressIndicator(
                                progress = uiState.importProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = Color(0xff0061ff),
                                trackColor = Color(0xffe5e7eb)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = Color(0xff0061ff),
                                trackColor = Color(0xffe5e7eb)
                            )
                        }
                    }
                }

                uiState.importStatus.takeIf { it.isNotBlank() }?.let { status ->
                    val isSuccess = status.contains("completed", ignoreCase = true)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) Color(0xffecfdf5) else Color(0xfffffbeb)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(14.dp),
                            color = if (isSuccess) Color(0xff047857) else Color(0xff92400e),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Preview(widthDp = 375, heightDp = 840)
@Composable
private fun ImportExportScreenPreview() {
    ImportExportScreen("deck_id", {})
}
