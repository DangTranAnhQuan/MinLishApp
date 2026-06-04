package com.project.minlishapp.presentation.practice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.domain.model.PracticeSessionMode
import com.project.minlishapp.domain.usecase.quiz.FillInBlankQuestion
import com.project.minlishapp.domain.usecase.quiz.MultipleChoiceQuestion
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import com.project.minlishapp.domain.usecase.srs.ReviewSchedule
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PrimaryBlue = Color(0xff0066ff)
private val DarkText = Color(0xff1f2937)
private val GrayText = Color(0xff6b7280)
private val LightSurface = Color(0xfff8f9fa)
private val LightBorder = Color(0xffe5e7eb)
private val CorrectColor = Color(0xff15803d)
private val CorrectContainerColor = Color(0xffdcfce7)
private val IncorrectColor = Color(0xffb91c1c)
private val IncorrectContainerColor = Color(0xfffee2e2)

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit = {},
    onStartFlashcardReview: (String?, PracticeReviewMode) -> Unit = { _, _ -> },
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val handleBack = {
        if (uiState.phase == PracticePhase.SETUP) {
            onBack()
        } else {
            viewModel.returnToSetup()
        }
    }

    BackHandler(onBack = handleBack)

    PracticeContent(
        uiState = uiState,
        onBack = handleBack,
        onNavigateToDashboard = onNavigateToDashboard,
        onStartFlashcardReview = onStartFlashcardReview,
        onSessionModeSelected = viewModel::selectSessionMode,
        onDeckSelected = viewModel::selectDeck,
        onQuizTypeSelected = viewModel::selectQuizType,
        onReviewModeSelected = viewModel::selectReviewMode,
        onStartSession = viewModel::startSession,
        onMultipleChoiceAnswerSelected = viewModel::selectMultipleChoiceAnswer,
        onFillInBlankAnswerChanged = viewModel::onFillInBlankAnswerChange,
        onCheckFillInBlankAnswer = viewModel::checkFillInBlankAnswer,
        onReviewGradeSelected = viewModel::reviewCurrentAnswer,
        onRetrySaveResult = viewModel::retrySaveResult,
        onContinueSession = viewModel::continueSession,
        onRestartSession = viewModel::restartSession,
        onReturnToSetup = viewModel::returnToSetup
    )
}

@Composable
private fun PracticeContent(
    uiState: PracticeUiState,
    onBack: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onStartFlashcardReview: (String?, PracticeReviewMode) -> Unit,
    onSessionModeSelected: (PracticeSessionMode) -> Unit,
    onDeckSelected: (String) -> Unit,
    onQuizTypeSelected: (QuizType) -> Unit,
    onReviewModeSelected: (PracticeReviewMode) -> Unit,
    onStartSession: () -> Unit,
    onMultipleChoiceAnswerSelected: (String) -> Unit,
    onFillInBlankAnswerChanged: (String) -> Unit,
    onCheckFillInBlankAnswer: () -> Unit,
    onReviewGradeSelected: (ReviewGrade) -> Unit,
    onRetrySaveResult: () -> Unit,
    onContinueSession: () -> Unit,
    onRestartSession: () -> Unit,
    onReturnToSetup: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = { PracticeHeader(onBack = onBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryBlue
                    )
                }

                uiState.errorMessage != null -> {
                    CenteredMessage(
                        title = "Không thể tải bài luyện tập",
                        message = uiState.errorMessage
                    )
                }

                uiState.phase == PracticePhase.SETUP -> {
                    PracticeSetup(
                        uiState = uiState,
                        onStartFlashcardReview = onStartFlashcardReview,
                        onSessionModeSelected = onSessionModeSelected,
                        onDeckSelected = onDeckSelected,
                        onQuizTypeSelected = onQuizTypeSelected,
                        onReviewModeSelected = onReviewModeSelected,
                        onStartSession = onStartSession
                    )
                }

                uiState.phase == PracticePhase.IN_PROGRESS -> {
                    ActivePracticeSession(
                        uiState = uiState,
                        onMultipleChoiceAnswerSelected = onMultipleChoiceAnswerSelected,
                        onFillInBlankAnswerChanged = onFillInBlankAnswerChanged,
                        onCheckFillInBlankAnswer = onCheckFillInBlankAnswer,
                        onReviewGradeSelected = onReviewGradeSelected,
                        onRetrySaveResult = onRetrySaveResult,
                        onContinueSession = onContinueSession
                    )
                }

                else -> {
                    CompletedPracticeSession(
                        uiState = uiState,
                        onRestartSession = onRestartSession,
                        onReturnToSetup = onReturnToSetup,
                        onNavigateToDashboard = onNavigateToDashboard
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeSetup(
    uiState: PracticeUiState,
    onStartFlashcardReview: (String?, PracticeReviewMode) -> Unit,
    onSessionModeSelected: (PracticeSessionMode) -> Unit,
    onDeckSelected: (String) -> Unit,
    onQuizTypeSelected: (QuizType) -> Unit,
    onReviewModeSelected: (PracticeReviewMode) -> Unit,
    onStartSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SessionModeSelector(
            selectedSessionMode = uiState.sessionMode,
            onSessionModeSelected = onSessionModeSelected
        )

        if (uiState.sessionMode == PracticeSessionMode.DECK_PRACTICE) {
            if (uiState.decks.isEmpty()) {
                MessageCard(
                    title = "Chưa có bộ từ",
                    message = "Hãy tạo deck và thêm thẻ trước khi bắt đầu luyện tập."
                )
            } else {
                DeckSelector(
                    decks = uiState.decks,
                    selectedDeck = uiState.selectedDeck,
                    onDeckSelected = onDeckSelected
                )
            }
            ReviewModeSelector(
                selectedReviewMode = uiState.reviewMode,
                onReviewModeSelected = onReviewModeSelected
            )
        }

        QuizTypeSelector(
            selectedQuizType = uiState.quizType,
            onQuizTypeSelected = onQuizTypeSelected
        )

        if (uiState.sessionMode == PracticeSessionMode.SPACED_REPETITION) {
            SpacedRepetitionPlanCard(uiState = uiState)
        } else {
            DailyLearningPlanCard(uiState = uiState)
        }

        uiState.setupMessage?.let { message ->
            MessageCard(title = "Chưa thể bắt đầu", message = message)
        }

        if (uiState.quizType == QuizType.FLASHCARD) {
            PrimaryButton(
                text = if (uiState.sessionMode == PracticeSessionMode.SPACED_REPETITION) {
                    "Ôn Flashcard (${uiState.sessionQuestionCount} từ)"
                } else {
                    "Học Flashcard (${uiState.sessionQuestionCount} từ)"
                },
                enabled = uiState.canStartSession,
                onClick = {
                    onStartFlashcardReview(
                        uiState.selectedDeckId.takeIf {
                            uiState.sessionMode == PracticeSessionMode.DECK_PRACTICE
                        },
                        uiState.reviewMode
                    )
                }
            )
        } else {
            PrimaryButton(
                text = "Bắt đầu luyện tập (${uiState.sessionQuestionCount} câu)",
                enabled = uiState.canStartSession,
                onClick = onStartSession
            )
        }
    }
}

@Composable
private fun ActivePracticeSession(
    uiState: PracticeUiState,
    onMultipleChoiceAnswerSelected: (String) -> Unit,
    onFillInBlankAnswerChanged: (String) -> Unit,
    onCheckFillInBlankAnswer: () -> Unit,
    onReviewGradeSelected: (ReviewGrade) -> Unit,
    onRetrySaveResult: () -> Unit,
    onContinueSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CompactSessionProgress(uiState = uiState)

        when (uiState.quizType) {
            QuizType.FLASHCARD -> Unit

            QuizType.MULTIPLE_CHOICE -> {
                uiState.multipleChoiceQuestion?.let { question ->
                    MultipleChoiceQuiz(
                        question = question,
                        selectedAnswer = uiState.selectedMultipleChoiceAnswer,
                        feedback = uiState.feedback,
                        onAnswerSelected = onMultipleChoiceAnswerSelected
                    )
                }
            }

            QuizType.FILL_IN_THE_BLANK -> {
                uiState.fillInBlankQuestion?.let { question ->
                    FillInBlankQuiz(
                        question = question,
                        answer = uiState.fillInBlankAnswer,
                        feedback = uiState.feedback,
                        answerErrorMessage = uiState.answerErrorMessage,
                        onAnswerChanged = onFillInBlankAnswerChanged,
                        onCheckAnswer = onCheckFillInBlankAnswer
                    )
                }
            }
        }

        uiState.feedback?.let { feedback ->
            FeedbackCard(
                feedback = feedback,
                correctAnswer = when (uiState.quizType) {
                    QuizType.FLASHCARD -> ""
                    QuizType.MULTIPLE_CHOICE -> uiState.multipleChoiceQuestion?.correctAnswer.orEmpty()
                    QuizType.FILL_IN_THE_BLANK -> uiState.fillInBlankQuestion?.correctAnswer.orEmpty()
                },
                correctMeaning = uiState.fillInBlankQuestion?.meaning
            )
            if (uiState.selectedReviewGrade == null) {
                uiState.resultSaveErrorMessage?.let { message ->
                    MessageCard(title = "Chưa thể lưu kết quả", message = message)
                }
                Sm2GradeActions(
                    feedback = feedback,
                    isOfficialReview = uiState.isOfficialReview,
                    allowedGrades = uiState.allowedReviewGrades,
                    onReviewGradeSelected = onReviewGradeSelected
                )
            } else {
                SaveResultStatus(
                    uiState = uiState,
                    onRetrySaveResult = onRetrySaveResult
                )
            }
            if (uiState.isResultSaved) {
                PrimaryButton(
                    text = if (uiState.isLastQuestion) "Hoàn thành phiên" else "Câu tiếp theo",
                    onClick = onContinueSession
                )
            }
        }
    }
}

@Composable
private fun CompletedPracticeSession(
    uiState: PracticeUiState,
    onRestartSession: () -> Unit,
    onReturnToSetup: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = CorrectColor,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "Đã hoàn thành phiên",
            color = DarkText,
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = uiState.sessionTitle,
            color = GrayText,
            style = MaterialTheme.typography.bodyLarge
        )
        CompletionScoreCard(uiState = uiState)
        ReviewScheduleCard(reviewSchedule = uiState.reviewSchedule)
        if (uiState.sessionMode == PracticeSessionMode.DECK_PRACTICE) {
            PrimaryButton(text = "Luyện lại bộ từ này", onClick = onRestartSession)
            OutlinedActionButton(text = "Tạo phiên khác", onClick = onReturnToSetup)
        } else {
            PrimaryButton(text = "Tạo phiên khác", onClick = onReturnToSetup)
        }
        OutlinedActionButton(text = "Xem Tổng quan", onClick = onNavigateToDashboard)
    }
}

@Composable
private fun PracticeHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = DarkText
            )
        }
        Text(
            text = "Practice Quiz",
            modifier = Modifier.weight(1f),
            color = DarkText,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun DeckSelector(
    decks: List<Deck>,
    selectedDeck: Deck?,
    onDeckSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, LightBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText)
        ) {
            Text(
                text = selectedDeck?.title ?: "Chọn bộ từ cần luyện",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            decks.forEach { deck ->
                DropdownMenuItem(
                    text = { Text(deck.title.ifBlank { "Bộ từ chưa đặt tên" }) },
                    onClick = {
                        expanded = false
                        onDeckSelected(deck.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionModeSelector(
    selectedSessionMode: PracticeSessionMode,
    onSessionModeSelected: (PracticeSessionMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SessionModeButton(
            title = "Ôn theo lịch SM-2",
            description = "Các từ đã học đang đến hạn.",
            isSelected = selectedSessionMode == PracticeSessionMode.SPACED_REPETITION,
            onClick = { onSessionModeSelected(PracticeSessionMode.SPACED_REPETITION) }
        )
        SessionModeButton(
            title = "Luyện theo bộ từ",
            description = "Toàn bộ từ trong một bộ từ.",
            isSelected = selectedSessionMode == PracticeSessionMode.DECK_PRACTICE,
            onClick = { onSessionModeSelected(PracticeSessionMode.DECK_PRACTICE) }
        )
    }
}

@Composable
private fun SessionModeButton(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else LightBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color(0xffeff6ff) else Color.White,
            contentColor = DarkText
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, color = DarkText, fontWeight = FontWeight.Bold)
            Text(
                text = description,
                color = GrayText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ReviewModeSelector(
    selectedReviewMode: PracticeReviewMode,
    onReviewModeSelected: (PracticeReviewMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cách ghi nhận kết quả",
            color = DarkText,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReviewModeButton(
                modifier = Modifier.weight(1f),
                title = "Chính thức",
                description = "Cập nhật lịch SM-2.",
                isSelected = selectedReviewMode == PracticeReviewMode.OFFICIAL,
                onClick = { onReviewModeSelected(PracticeReviewMode.OFFICIAL) }
            )
            ReviewModeButton(
                modifier = Modifier.weight(1f),
                title = "Tự do",
                description = "Không đổi lịch ôn.",
                isSelected = selectedReviewMode == PracticeReviewMode.FREE_PRACTICE,
                onClick = { onReviewModeSelected(PracticeReviewMode.FREE_PRACTICE) }
            )
        }
    }
}

@Composable
private fun ReviewModeButton(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else LightBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) Color(0xffeff6ff) else Color.White,
            contentColor = DarkText
        ),
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, color = GrayText, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun QuizTypeSelector(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        QuizTypeButton(
            text = "Flashcard",
            isSelected = selectedQuizType == QuizType.FLASHCARD,
            onClick = { onQuizTypeSelected(QuizType.FLASHCARD) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuizTypeButton(
            text = "Trắc nghiệm",
            isSelected = selectedQuizType == QuizType.MULTIPLE_CHOICE,
            onClick = { onQuizTypeSelected(QuizType.MULTIPLE_CHOICE) },
            modifier = Modifier.weight(1f)
            )
            QuizTypeButton(
                text = "Điền từ",
                isSelected = selectedQuizType == QuizType.FILL_IN_THE_BLANK,
                onClick = { onQuizTypeSelected(QuizType.FILL_IN_THE_BLANK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuizTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, LightBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun CompactSessionProgress(uiState: PracticeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Câu ${uiState.currentQuestionIndex + 1}/${uiState.totalQuestionCount}",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.correctAnswerCount} đúng · ${uiState.incorrectAnswerCount} sai",
                color = GrayText,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun DailyLearningPlanCard(uiState: PracticeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Hôm nay", color = DarkText, fontWeight = FontWeight.Bold)
            Text(
                text = if (uiState.isOfficialReview) {
                    "Luyện chính thức: kết quả sẽ cập nhật lịch ôn SM-2."
                } else {
                    "Luyện tự do: kết quả không đổi lịch ôn SM-2."
                },
                color = GrayText,
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LearningPlanMetric(
                    modifier = Modifier.weight(1f),
                    label = "Từ mới",
                    value = uiState.newWordsCount
                )
                LearningPlanMetric(
                    modifier = Modifier.weight(1f),
                    label = "Cần ôn",
                    value = uiState.reviewSchedule.dueNowCount
                )
            }
            uiState.reviewSchedule.nextReviewTime?.let { nextReviewTime ->
                Text(
                    text = "Đợt gần nhất: ${formatReviewDateTime(nextReviewTime)} · " +
                        "${uiState.reviewSchedule.nextReviewCount} từ trong ngày",
                    color = GrayText,
                    style = MaterialTheme.typography.bodySmall
                )
            } ?: Text(
                text = "Chưa có đợt ôn tiếp theo.",
                color = GrayText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SpacedRepetitionPlanCard(uiState: PracticeUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Lịch ôn sắp tới", color = DarkText, fontWeight = FontWeight.Bold)
            SpacedRepetitionForecastChart(buckets = uiState.reviewForecast)
            Text(
                text = "Cần ôn ngay: ${uiState.reviewSchedule.dueNowCount} từ",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
            uiState.reviewSchedule.nextReviewTime?.let { nextReviewTime ->
                Text(
                    text = "Đợt gần nhất: ${formatReviewDateTime(nextReviewTime)} · " +
                        "${uiState.reviewSchedule.nextReviewCount} từ trong ngày",
                    color = GrayText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LearningPlanMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: Int
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                color = PrimaryBlue,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Text(text = label, color = GrayText, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun Sm2GradeActions(
    feedback: AnswerFeedback,
    isOfficialReview: Boolean,
    allowedGrades: Set<ReviewGrade>,
    onReviewGradeSelected: (ReviewGrade) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Bạn nhớ từ này thế nào?", color = DarkText, fontWeight = FontWeight.Bold)
            Text(
                text = if (isOfficialReview) {
                    "Chế độ chính thức: mức bạn chọn sẽ cập nhật lịch ôn SM-2."
                } else {
                    "Luyện tự do: mức bạn chọn chỉ dùng trong phiên này, không đổi lịch SM-2."
                },
                color = GrayText,
                style = MaterialTheme.typography.bodySmall
            )
            if (feedback == AnswerFeedback.INCORRECT) {
                Text(
                    text = "Vì câu trả lời sai, Good/Easy bị khóa để tránh đẩy lịch ôn quá xa.",
                    color = IncorrectColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Sm2GradeButton(
                    modifier = Modifier.weight(1f),
                    text = "Again",
                    enabled = ReviewGrade.AGAIN in allowedGrades,
                    onClick = { onReviewGradeSelected(ReviewGrade.AGAIN) }
                )
                Sm2GradeButton(
                    modifier = Modifier.weight(1f),
                    text = "Hard",
                    enabled = ReviewGrade.HARD in allowedGrades,
                    onClick = { onReviewGradeSelected(ReviewGrade.HARD) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Sm2GradeButton(
                    modifier = Modifier.weight(1f),
                    text = "Good",
                    enabled = ReviewGrade.GOOD in allowedGrades,
                    onClick = { onReviewGradeSelected(ReviewGrade.GOOD) }
                )
                Sm2GradeButton(
                    modifier = Modifier.weight(1f),
                    text = "Easy",
                    enabled = ReviewGrade.EASY in allowedGrades,
                    onClick = { onReviewGradeSelected(ReviewGrade.EASY) }
                )
            }
        }
    }
}

@Composable
private fun Sm2GradeButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 9.dp)
    ) {
        Text(text = text, color = if (enabled) PrimaryBlue else GrayText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MultipleChoiceQuiz(
    question: MultipleChoiceQuestion,
    selectedAnswer: String?,
    feedback: AnswerFeedback?,
    onAnswerSelected: (String) -> Unit
) {
    QuestionCard(title = "Chọn nghĩa đúng của từ:", question = question.word) {
        question.options.forEach { option ->
            val isCorrectAnswer = option == question.correctAnswer
            val isSelectedAnswer = option == selectedAnswer
            val containerColor = when {
                feedback != null && isCorrectAnswer -> CorrectContainerColor
                feedback != null && isSelectedAnswer -> IncorrectContainerColor
                else -> Color.Transparent
            }
            val contentColor = when {
                feedback != null && isCorrectAnswer -> CorrectColor
                feedback != null && isSelectedAnswer -> IncorrectColor
                else -> PrimaryBlue
            }

            OutlinedButton(
                onClick = { onAnswerSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                enabled = feedback == null,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, contentColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor,
                    disabledContentColor = contentColor
                )
            ) {
                Text(text = option, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
        }
    }
}

@Composable
private fun FillInBlankQuiz(
    question: FillInBlankQuestion,
    answer: String,
    feedback: AnswerFeedback?,
    answerErrorMessage: String?,
    onAnswerChanged: (String) -> Unit,
    onCheckAnswer: () -> Unit
) {
    QuestionCard(title = "Điền từ còn thiếu vào câu:", question = question.sentence) {
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = feedback == null,
            label = { Text(text = "Từ cần điền") },
            isError = answerErrorMessage != null,
            supportingText = { answerErrorMessage?.let { Text(text = it) } },
            singleLine = true
        )
        if (feedback == null) {
            PrimaryButton(
                text = "Kiểm tra",
                onClick = onCheckAnswer
            )
        }
    }
}

@Composable
private fun QuestionCard(
    title: String,
    question: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LightBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = GrayText)
            Text(text = question, color = DarkText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun FeedbackCard(
    feedback: AnswerFeedback,
    correctAnswer: String,
    correctMeaning: String?
) {
    val isCorrect = feedback == AnswerFeedback.CORRECT
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isCorrect) CorrectContainerColor else IncorrectContainerColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isCorrect) "Chính xác" else "Chưa chính xác",
                color = if (isCorrect) CorrectColor else IncorrectColor,
                fontWeight = FontWeight.Bold
            )
            if (!isCorrect) {
                Text(text = "Đáp án đúng: $correctAnswer", color = IncorrectColor)
            }
            correctMeaning?.takeIf { it.isNotBlank() }?.let { meaning ->
                Text(text = "Nghĩa: $meaning", color = if (isCorrect) CorrectColor else IncorrectColor)
            }
        }
    }
}

@Composable
private fun SaveResultStatus(
    uiState: PracticeUiState,
    onRetrySaveResult: () -> Unit
) {
    when {
        uiState.isSavingResult -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryBlue)
                Text(text = "Đang lưu kết quả luyện tập...", color = GrayText)
            }
        }

        uiState.resultSaveErrorMessage != null -> {
            MessageCard(title = "Chưa lưu được kết quả", message = uiState.resultSaveErrorMessage)
            PrimaryButton(text = "Thử lưu lại", onClick = onRetrySaveResult)
        }

        uiState.isResultSaved && !uiState.isOfficialReview -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Đã ghi nhận trong phiên luyện tự do.",
                    color = CorrectColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lịch ôn SM-2 và thống kê học tập không thay đổi.",
                    color = GrayText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.isResultSaved -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Đã lưu kết quả và cập nhật lịch SM-2.",
                    color = CorrectColor,
                    fontWeight = FontWeight.Bold
                )
                uiState.lastNextReviewTime?.let { nextReviewTime ->
                    Text(
                        text = "Ease factor: ${formatEaseFactor(uiState.lastEaseFactor ?: 2.5)} · " +
                            "Khoảng cách: ${uiState.lastReviewIntervalDays ?: 0} ngày",
                        color = GrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Ôn lại lúc: ${formatReviewDateTime(nextReviewTime)}",
                        color = GrayText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewScheduleCard(reviewSchedule: ReviewSchedule) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Lịch ôn SM-2 của bạn", color = DarkText, fontWeight = FontWeight.Bold)
            Text(
                text = "Cần ôn ngay: ${reviewSchedule.dueNowCount} từ",
                color = if (reviewSchedule.dueNowCount > 0) IncorrectColor else GrayText,
                style = MaterialTheme.typography.bodyMedium
            )
            reviewSchedule.nextReviewTime?.let { nextReviewTime ->
                Text(
                    text = "Đợt gần nhất: ${formatReviewDateTime(nextReviewTime)} · " +
                        "${reviewSchedule.nextReviewCount} từ trong ngày",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            } ?: Text(
                text = "Chưa có đợt ôn tiếp theo trong tương lai.",
                color = GrayText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CompletionScoreCard(uiState: PracticeUiState) {
    val total = uiState.completedAnswerCount
    val accuracy = if (total == 0) 0 else uiState.correctAnswerCount * 100 / total
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$accuracy%", color = PrimaryBlue, style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold))
            Text(text = "Độ chính xác trong phiên", color = GrayText)
            Text(text = "${uiState.correctAnswerCount} đúng · ${uiState.incorrectAnswerCount} sai", color = DarkText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, color = DarkText, fontWeight = FontWeight.Bold)
            Text(text = message, color = GrayText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    message: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, color = DarkText, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(text = message, color = GrayText, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color.White)
    ) {
        Text(text = text)
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PrimaryBlue),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
    ) {
        Text(text = text)
    }
}

private val QuizType.label: String
    get() = when (this) {
        QuizType.FLASHCARD -> "Flashcard"
        QuizType.MULTIPLE_CHOICE -> "Trắc nghiệm"
        QuizType.FILL_IN_THE_BLANK -> "Điền từ"
    }

private fun formatReviewDateTime(date: Date): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
}

private fun formatEaseFactor(easeFactor: Double): String {
    return String.format(Locale.getDefault(), "%.2f", easeFactor)
}

@Preview(showBackground = true, name = "Practice Setup")
@Composable
private fun PracticeSetupPreview() {
    MaterialTheme {
        PracticeContent(
            uiState = previewSetupState,
            onBack = {},
            onNavigateToDashboard = {},
            onStartFlashcardReview = { _, _ -> },
            onSessionModeSelected = {},
            onDeckSelected = {},
            onQuizTypeSelected = {},
            onReviewModeSelected = {},
            onStartSession = {},
            onMultipleChoiceAnswerSelected = {},
            onFillInBlankAnswerChanged = {},
            onCheckFillInBlankAnswer = {},
            onReviewGradeSelected = {},
            onRetrySaveResult = {},
            onContinueSession = {},
            onRestartSession = {},
            onReturnToSetup = {}
        )
    }
}

@Preview(showBackground = true, name = "Practice Question")
@Composable
private fun PracticeQuestionPreview() {
    MaterialTheme {
        PracticeContent(
            uiState = previewSetupState.copy(
                phase = PracticePhase.IN_PROGRESS,
                quizType = QuizType.MULTIPLE_CHOICE,
                queueCardIds = listOf("1", "2", "3"),
                multipleChoiceQuestion = MultipleChoiceQuestion(
                    cardId = "1",
                    word = "diligent",
                    options = listOf("chăm chỉ", "rõ ràng", "tốt bụng", "lưu loát"),
                    correctAnswer = "chăm chỉ"
                )
            ),
            onBack = {},
            onNavigateToDashboard = {},
            onStartFlashcardReview = { _, _ -> },
            onSessionModeSelected = {},
            onDeckSelected = {},
            onQuizTypeSelected = {},
            onReviewModeSelected = {},
            onStartSession = {},
            onMultipleChoiceAnswerSelected = {},
            onFillInBlankAnswerChanged = {},
            onCheckFillInBlankAnswer = {},
            onReviewGradeSelected = {},
            onRetrySaveResult = {},
            onContinueSession = {},
            onRestartSession = {},
            onReturnToSetup = {}
        )
    }
}

private val previewDeck = Deck(id = "deck-1", title = "IELTS Vocabulary")
private val previewSetupState = PracticeUiState(
    isLoading = false,
    decks = listOf(previewDeck),
    selectedDeckId = previewDeck.id,
    practiceCards = listOf(Card(id = "1", deckId = previewDeck.id, word = "diligent")),
    availableQuestionCount = 12,
    sessionQuestionCount = 4
)
