package com.project.minlishapp.presentation.flashcard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDragHandleDefaults.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import com.project.minlishapp.domain.model.Card as FlashcardCard
import com.project.minlishapp.domain.usecase.srs.ReviewGrade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    onBack: () -> Unit,
    viewModel: FlashcardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentCard = uiState.cards.getOrNull(uiState.currentCardIndex)
    val rotation = remember { Animatable(0f) }
    val density = LocalDensity.current

    LaunchedEffect(uiState.currentCardIndex) {
        rotation.snapTo(0f)
    }

    LaunchedEffect(uiState.isFlipped, uiState.currentCardIndex) {
        rotation.animateTo(
            targetValue = if (uiState.isFlipped) 180f else 0f,
            animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
        )
    }

    // Tự động ẩn tin nhắn thông báo sau 2 giây
    LaunchedEffect(uiState.statusMessage) {
        if (uiState.statusMessage != null) {
            kotlinx.coroutines.delay(2000L)
            viewModel.clearStatusMessage()
        }
    }

    // Tự động ẩn tin nhắn lỗi sau 3 giây
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000L)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            Header(onBack = onBack)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.errorMessage != null && uiState.cards.isEmpty() -> {
                    EmptyState(
                        title = "Không thể tải thẻ",
                        message = uiState.errorMessage ?: "Đã xảy ra lỗi không xác định."
                    )
                }

                uiState.cards.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyState(
                            title = "Chưa có thẻ nào",
                            message = "Hãy thêm thẻ mới vào bộ từ trước khi học."
                        )
                        Button(
                            onClick = viewModel::insertTestData,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(top = 16.dp)
                        ) {
                            Text("DEBUG: Insert Test Cards")
                        }
                    }
                }

                currentCard != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryRow(
                            newWordsCount = uiState.newWordsCount,
                            dueWordsCount = uiState.dueWordsCount,
                            learnedWordsCount = uiState.learnedWordsCount
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .clickable(enabled = !uiState.isSubmitting) { viewModel.toggleFlip() },
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xfff3f4f6)),
                            shadowElevation = 12.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            rotationY = rotation.value
                                            cameraDistance = with(density) { 28.dp.toPx() }
                                            if (rotation.value > 90f) scaleX = -1f
                                        }
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rotation.value <= 90f) {
                                        FrontFace(card = currentCard)
                                    } else {
                                        BackFace(card = currentCard)
                                    }
                                }
                            }
                        }

                        Text(
                            text = "${uiState.currentCardIndex + 1}/${uiState.cards.size}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xff0066ff),
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicatorWithLabel(
                            progress = (uiState.currentCardIndex + 1).toFloat() / uiState.cards.size.toFloat()
                        )

                        if (!uiState.isFlipped) {
                            Button(
                                onClick = viewModel::toggleFlip,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Lật thẻ để xem đáp án")
                            }
                        } else {
                            GradeActions(
                                enabled = !uiState.isSubmitting,
                                onAgain = { viewModel.reviewCurrentCard(ReviewGrade.AGAIN) },
                                onHard = { viewModel.reviewCurrentCard(ReviewGrade.HARD) },
                                onGood = { viewModel.reviewCurrentCard(ReviewGrade.GOOD) },
                                onEasy = { viewModel.reviewCurrentCard(ReviewGrade.EASY) }
                            )
                        }

                        if (uiState.isSubmitting) {
                            Text(
                                text = "Đang lưu kết quả ôn tập...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        uiState.statusMessage?.let { message ->
                            AssistChip(
                                onClick = viewModel::clearStatusMessage,
                                label = { Text(text = message) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
                                }
                            )
                        }

                        uiState.errorMessage?.let { message ->
                            AssistChip(
                                onClick = viewModel::clearErrorMessage,
                                label = { Text(text = message, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Header(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.width(40.dp).height(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xff1f2937)
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = "Flashcard Learning",
                color = Color(0xff1f2937),
                lineHeight = 1.56.em,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }

        Box(modifier = Modifier.width(40.dp).height(40.dp))
    }
}

@Composable
private fun SummaryRow(
    newWordsCount: Int,
    dueWordsCount: Int,
    learnedWordsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            title = "Từ mới",
            value = newWordsCount.toString(),
            icon = Icons.Filled.Star,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            title = "Cần ôn",
            value = dueWordsCount.toString(),
            icon = Icons.Filled.Refresh,
            modifier = Modifier.weight(1f)
        )
        StatChip(
            title = "Đã học",
            value = learnedWordsCount.toString(),
            icon = Icons.Filled.CheckCircle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xfff8f9fa),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xff0066ff))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color(0xff1f2937))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color(0xff6b7280))
        }
    }
}

@Composable
private fun FrontFace(card: FlashcardCard) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "TỪ CẦN NHỚ",
            color = Color(0xff6b7280),
            textAlign = TextAlign.Center,
            lineHeight = 1.43.em,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.7.sp)
        )
        Text(
            text = card.word,
            color = Color(0xff1f2937),
            textAlign = TextAlign.Center,
            lineHeight = 1.11.em,
            style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp)
        )
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(2.dp)
                .clip(shape = RoundedCornerShape(9999.dp))
                .background(color = Color(0xffe5e7eb))
        )
        Text(
            text = "Chạm vào thẻ hoặc nhấn nút bên\ndưới để lật và xem chi tiết.",
            color = Color(0xff6b7280),
            textAlign = TextAlign.Center,
            lineHeight = 1.63.em,
            style = TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun BackFace(card: FlashcardCard) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = card.word,
            color = Color(0xff0066ff),
            textAlign = TextAlign.Center,
            lineHeight = 1.11.em,
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = card.pronunciation,
            color = Color(0xff6b7280),
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp)
        )
        DetailRow(label = "Meaning", value = card.meaning)
        DetailRow(label = "Description EN", value = card.descriptionEn)
        DetailRow(label = "Example", value = card.example)
        DetailRow(label = "Collocation", value = card.collocation)
        DetailRow(label = "Related Words", value = card.relatedWords)
        DetailRow(label = "Note", value = card.note.ifBlank { "Không có ghi chú" })
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xfff8f9fa),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color(0xff0066ff))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = Color(0xff374151))
        }
    }
}

@Composable
private fun GradeActions(
    enabled: Boolean,
    onAgain: () -> Unit,
    onHard: () -> Unit,
    onGood: () -> Unit,
    onEasy: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Đánh giá mức độ ghi nhớ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onAgain,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xff0066ff))
            ) { Text("Again") }
            OutlinedButton(
                onClick = onHard,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xff0066ff))
            ) { Text("Hard") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onGood,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff0066ff), contentColor = Color.White)
            ) { Text("Good") }
            Button(
                onClick = onEasy,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff0066ff), contentColor = Color.White)
            ) { Text("Easy") }
        }
    }
}

@Composable
private fun LinearProgressIndicatorWithLabel(
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Tiến độ học trong bộ từ", style = MaterialTheme.typography.labelLarge, color = Color(0xff374151))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xff0066ff),
            trackColor = Color(0xffe5e7eb)
        )
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true, name = "Front State")
@Composable
private fun FlashcardPreviewFront() {
    MaterialTheme {
        FlashcardPreviewContent(showBackFace = false)
    }
}

@Preview(showBackground = true, name = "Back State")
@Composable
private fun FlashcardPreviewBack() {
    MaterialTheme {
        FlashcardPreviewContent(showBackFace = true)
    }
}

@Composable
private fun FlashcardPreviewContent(showBackFace: Boolean) {
    val previewCard = FlashcardCard(
        word = "accountability",
        pronunciation = "/əˌkaʊntəˈbɪləti/",
        meaning = "trách nhiệm giải trình",
        descriptionEn = "the fact of being responsible for your actions",
        example = "The manager emphasized accountability in every project.",
        collocation = "take accountability",
        relatedWords = "responsibility, obligation",
        note = "Common in business contexts"
    )
    Column(modifier = Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Header(onBack = {})
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryRow(newWordsCount = 5, dueWordsCount = 12, learnedWordsCount = 20)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xfff3f4f6)),
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showBackFace) {
                        BackFace(card = previewCard)
                    } else {
                        FrontFace(card = previewCard)
                    }
                }
            }

            Text(
                text = "1/10",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xff0066ff),
                textAlign = TextAlign.Center
            )

            LinearProgressIndicatorWithLabel(progress = 0.1f)

            if (!showBackFace) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff0066ff), contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Lật thẻ để xem đáp án")
                }
            } else {
                GradeActions(
                    enabled = true,
                    onAgain = {},
                    onHard = {},
                    onGood = {},
                    onEasy = {}
                )
            }
        }
    }
}