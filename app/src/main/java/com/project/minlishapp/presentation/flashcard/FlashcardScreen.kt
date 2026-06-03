package com.project.minlishapp.presentation.flashcard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import com.project.minlishapp.ui.theme.DarkText
import com.project.minlishapp.ui.theme.GrayText
import com.project.minlishapp.ui.theme.LightGraySurface
import com.project.minlishapp.ui.theme.SoftWhite
import com.project.minlishapp.ui.theme.VeryLightGray
import com.project.minlishapp.ui.theme.VibrantBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    LaunchedEffect(uiState.isFlipped, currentCard?.id) {
        if (uiState.isFlipped) {
            rotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        } else {
            rotation.snapTo(0f)
        }
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
        containerColor = SoftWhite,
        topBar = {
            Header(
                onBack = onBack,
                title = if (uiState.isSpacedRepetitionReview) {
                    "Ôn tập ngắt quãng"
                } else {
                    "Flashcard Learning"
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SoftWhite)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VibrantBlue
                    )
                }

                uiState.errorMessage != null && uiState.cards.isEmpty() -> {
                    EmptyState(
                        title = "Không thể tải thẻ",
                        message = uiState.errorMessage ?: "Đã xảy ra lỗi không xác định."
                    )
                }

                uiState.isSessionCompleted -> {
                    FlashcardCompletionState(
                        isSpacedRepetitionReview = uiState.isSpacedRepetitionReview,
                        isOfficialReview = uiState.isOfficialReview,
                        reviewedCount = uiState.completedReviewCount,
                        nextReviewTime = uiState.sessionNextReviewTime,
                        onDone = onBack
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
                            title = if (uiState.isSpacedRepetitionReview) {
                                "Đã ôn xong"
                            } else {
                                "Chưa có thẻ nào"
                            },
                            message = if (uiState.isSpacedRepetitionReview) {
                                "Bạn không còn từ đã học nào đến hạn ôn tập."
                            } else {
                                "Hãy thêm thẻ mới vào bộ từ trước khi học."
                            }
                        )
                        if (!uiState.isSpacedRepetitionReview) {
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
                }

                currentCard != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.isSpacedRepetitionReview) {
                            Text(
                                text = "Còn ${uiState.cards.size} từ cần ôn",
                                color = VibrantBlue,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            SummaryRow(
                                newWordsCount = uiState.newWordsCount,
                                dueWordsCount = uiState.dueWordsInDeckCount,
                                learnedWordsCount = uiState.learnedWordsCount
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .clickable(enabled = !uiState.isSubmitting) { viewModel.toggleFlip() },
                            shape = RoundedCornerShape(28.dp),
                            color = SoftWhite,
                            border = BorderStroke(1.dp, VeryLightGray),
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
                                            rotationY = if (uiState.isFlipped) rotation.value else 0f
                                            cameraDistance = with(density) { 28.dp.toPx() }
                                            if (uiState.isFlipped && rotation.value > 90f) scaleX = -1f
                                        }
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!uiState.isFlipped || rotation.value <= 90f) {
                                        FrontFace(word = currentCard.word)
                                    } else {
                                        BackFace(
                                            word = currentCard.word,
                                            pronunciation = currentCard.pronunciation,
                                            meaning = currentCard.meaning,
                                            descriptionEn = currentCard.descriptionEn.ifBlank { currentCard.definition },
                                            example = currentCard.example,
                                            collocation = currentCard.collocation,
                                            relatedWords = currentCard.relatedWords,
                                            note = currentCard.note
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "${uiState.completedReviewCount + 1}/${uiState.sessionTotalCount}",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelLarge,
                            color = VibrantBlue,
                            textAlign = TextAlign.Center
                        )

                        LinearProgressIndicatorWithLabel(
                            progress = (uiState.completedReviewCount + 1).toFloat() /
                                uiState.sessionTotalCount.coerceAtLeast(1).toFloat()
                        )
                        Text(
                            text = if (uiState.isOfficialReview) {
                                "Luyện chính thức: các mức nhớ sẽ cập nhật lịch ôn SM-2."
                            } else {
                                "Luyện tự do: các mức nhớ không đổi lịch ôn SM-2."
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = GrayText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )

                        if (!uiState.isFlipped) {
                            Button(
                                onClick = viewModel::toggleFlip,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantBlue,
                                    contentColor = SoftWhite
                                )
                            ) {
                                Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Xem đáp án")
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
    title: String = "Flashcard Learning",
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(color = SoftWhite)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.width(40.dp).height(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DarkText
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = title,
                color = DarkText,
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
        color = LightGraySurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = VibrantBlue)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = DarkText)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = GrayText)
        }
    }
}

@Composable
private fun FrontFace(word: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "TỪ CẦN NHỚ",
            color = GrayText,
            textAlign = TextAlign.Center,
            lineHeight = 1.43.em,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.7.sp)
        )
        Text(
            text = word,
            color = DarkText,
            textAlign = TextAlign.Center,
            lineHeight = 1.11.em,
            style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp)
        )
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(2.dp)
                .clip(shape = RoundedCornerShape(9999.dp))
                .background(color = VeryLightGray)
        )
    }
}

@Composable
private fun BackFace(
    word: String,
    pronunciation: String,
    meaning: String,
    descriptionEn: String,
    example: String,
    collocation: String,
    relatedWords: String,
    note: String
) {
    val hasDetails = listOf(
        meaning,
        descriptionEn,
        example,
        collocation,
        relatedWords,
        note
    ).any { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = word,
            color = VibrantBlue,
            textAlign = TextAlign.Center,
            lineHeight = 1.11.em,
            style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = pronunciation,
            color = GrayText,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp)
        )
        OptionalDetailRow(label = "Meaning", value = meaning)
        OptionalDetailRow(label = "Description EN", value = descriptionEn)
        OptionalDetailRow(label = "Example", value = example)
        OptionalDetailRow(label = "Collocation", value = collocation)
        OptionalDetailRow(label = "Related Words", value = relatedWords)
        OptionalDetailRow(label = "Note", value = note)
        if (!hasDetails) {
            Text(
                text = "No details yet.",
                color = GrayText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun OptionalDetailRow(label: String, value: String) {
    val displayValue = value.trim()
    if (displayValue.isNotEmpty()) {
        DetailRow(label = label, value = displayValue)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightGraySurface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = VibrantBlue)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = DarkText)
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
                border = BorderStroke(1.dp, VibrantBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantBlue)
            ) { Text("Again") }
            OutlinedButton(
                onClick = onHard,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, VibrantBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantBlue)
            ) { Text("Hard") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onGood,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue, contentColor = SoftWhite)
            ) { Text("Good") }
            Button(
                onClick = onEasy,
                enabled = enabled,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue, contentColor = SoftWhite)
            ) { Text("Easy") }
        }
    }
}

@Composable
private fun LinearProgressIndicatorWithLabel(
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Tiến độ học trong bộ từ", style = MaterialTheme.typography.labelLarge, color = DarkText)
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = VibrantBlue,
            trackColor = VeryLightGray
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

@Composable
private fun FlashcardCompletionState(
    isSpacedRepetitionReview: Boolean,
    isOfficialReview: Boolean,
    reviewedCount: Int,
    nextReviewTime: Date?,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VibrantBlue
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isSpacedRepetitionReview) {
                "Đã ôn xong các từ đến hạn"
            } else if (!isOfficialReview) {
                "Đã luyện tự do xong bộ từ"
            } else {
                "Đã ôn xong bộ từ"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = DarkText,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bạn vừa hoàn thành $reviewedCount từ.",
            style = MaterialTheme.typography.bodyLarge,
            color = GrayText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (!isOfficialReview) {
                "Phiên này không thay đổi lịch ôn SM-2."
            } else {
                nextReviewTime?.let {
                    "Các từ vừa ôn sẽ đến hạn sớm nhất lúc ${formatReviewDateTime(it)}."
                } ?: "Chưa có lịch ôn tiếp theo."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VibrantBlue,
                contentColor = SoftWhite
            )
        ) {
            Text(text = "Hoàn tất")
        }
    }
}

private fun formatReviewDateTime(date: Date): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
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
    val word = "accountability"
    val pronunciation = "/əˌkaʊntəˈbɪləti/"
    val meaning = "trách nhiệm giải trình"
    val descriptionEn = "the fact of being responsible for your actions"
    val example = "The manager emphasized accountability in every project."
    val collocation = "take accountability"
    val relatedWords = "responsibility, obligation"
    val note = "Common in business contexts"

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
                color = SoftWhite,
                border = BorderStroke(1.dp, VeryLightGray),
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showBackFace) {
                        BackFace(
                            word = word,
                            pronunciation = pronunciation,
                            meaning = meaning,
                            descriptionEn = descriptionEn,
                            example = example,
                            collocation = collocation,
                            relatedWords = relatedWords,
                            note = note
                        )
                    } else {
                        FrontFace(word = word)
                    }
                }
            }

            Text(
                text = "1/10",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelLarge,
                color = VibrantBlue,
                textAlign = TextAlign.Center
            )

            LinearProgressIndicatorWithLabel(progress = 0.1f)

            if (!showBackFace) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue, contentColor = SoftWhite)
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Xem đáp án")
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
