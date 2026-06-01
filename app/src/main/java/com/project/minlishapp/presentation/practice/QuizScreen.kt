package com.project.minlishapp.presentation.practice

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.minlishapp.domain.usecase.quiz.FillInBlankQuestion
import com.project.minlishapp.domain.usecase.quiz.MultipleChoiceQuestion

private val CorrectColor = Color(0xff15803d)
private val CorrectContainerColor = Color(0xffdcfce7)
private val IncorrectColor = Color(0xffb91c1c)
private val IncorrectContainerColor = Color(0xfffee2e2)

@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PracticeContent(
        uiState = uiState,
        onBack = onBack,
        onQuizTypeSelected = viewModel::selectQuizType,
        onMultipleChoiceAnswerSelected = viewModel::selectMultipleChoiceAnswer,
        onFillInBlankAnswerChanged = viewModel::onFillInBlankAnswerChange,
        onCheckFillInBlankAnswer = viewModel::checkFillInBlankAnswer,
        onNextQuestion = viewModel::showNextQuestion
    )
}

@Composable
private fun PracticeContent(
    uiState: PracticeUiState,
    onBack: () -> Unit,
    onQuizTypeSelected: (QuizType) -> Unit,
    onMultipleChoiceAnswerSelected: (String) -> Unit,
    onFillInBlankAnswerChanged: (String) -> Unit,
    onCheckFillInBlankAnswer: () -> Unit,
    onNextQuestion: () -> Unit
) {
    Scaffold(
        topBar = {
            PracticeHeader(onBack = onBack)
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

                uiState.errorMessage != null -> {
                    MessageCard(
                        title = "Không thể tải bài luyện tập",
                        message = uiState.errorMessage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                uiState.cards.isEmpty() -> {
                    MessageCard(
                        title = "Chưa có thẻ từ vựng",
                        message = "Hãy thêm thẻ vào bộ từ trước khi bắt đầu luyện tập.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Chọn dạng bài tập",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        QuizTypeSelector(
                            selectedQuizType = uiState.quizType,
                            onQuizTypeSelected = onQuizTypeSelected
                        )

                        uiState.questionMessage?.let { message ->
                            MessageCard(
                                title = "Chưa đủ dữ liệu",
                                message = message
                            )
                        }

                        when (uiState.quizType) {
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
                                    QuizType.MULTIPLE_CHOICE -> uiState.multipleChoiceQuestion?.correctAnswer.orEmpty()
                                    QuizType.FILL_IN_THE_BLANK -> uiState.fillInBlankQuestion?.correctAnswer.orEmpty()
                                },
                                correctMeaning = when (uiState.quizType) {
                                    QuizType.MULTIPLE_CHOICE -> null
                                    QuizType.FILL_IN_THE_BLANK -> uiState.fillInBlankQuestion?.meaning
                                }
                            )

                            Button(
                                onClick = onNextQuestion,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (uiState.hasAlternativeQuestion) {
                                        "Câu tiếp theo"
                                    } else {
                                        "Làm lại câu này"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.width(80.dp)
        ) {
            Text(text = "Quay lại")
        }
        Text(
            text = "Practice Quiz",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun QuizTypeSelector(
    selectedQuizType: QuizType,
    onQuizTypeSelected: (QuizType) -> Unit
) {
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = text)
        }
    }
}

@Composable
private fun MultipleChoiceQuiz(
    question: MultipleChoiceQuestion,
    selectedAnswer: String?,
    feedback: AnswerFeedback?,
    onAnswerSelected: (String) -> Unit
) {
    QuestionCard(
        title = "Chọn nghĩa đúng của từ:",
        question = question.word
    ) {
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
                else -> MaterialTheme.colorScheme.primary
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
                Text(
                    text = option,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
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
    QuestionCard(
        title = "Điền từ còn thiếu vào câu:",
        question = question.sentence
    ) {
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = feedback == null,
            label = { Text(text = "Từ cần điền") },
            isError = answerErrorMessage != null,
            supportingText = {
                answerErrorMessage?.let { Text(text = it) }
            },
            singleLine = true
        )

        Button(
            onClick = onCheckAnswer,
            modifier = Modifier.fillMaxWidth(),
            enabled = feedback == null,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Kiểm tra")
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
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = question,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
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
                Text(
                    text = "Đáp án đúng: $correctAnswer",
                    color = IncorrectColor
                )
            }
            correctMeaning?.takeIf { it.isNotBlank() }?.let { meaning ->
                Text(
                    text = "Nghĩa: $meaning",
                    color = if (isCorrect) CorrectColor else IncorrectColor
                )
            }
        }
    }
}

@Composable
private fun MessageCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "Multiple Choice")
@Composable
private fun MultipleChoicePreview() {
    MaterialTheme {
        PracticeContent(
            uiState = PracticeUiState(
                isLoading = false,
                cards = previewCards,
                multipleChoiceQuestion = MultipleChoiceQuestion(
                    cardId = "1",
                    word = "diligent",
                    options = listOf("cham chi", "ro rang", "tot bung", "luu loat"),
                    correctAnswer = "cham chi"
                )
            ),
            onBack = {},
            onQuizTypeSelected = {},
            onMultipleChoiceAnswerSelected = {},
            onFillInBlankAnswerChanged = {},
            onCheckFillInBlankAnswer = {},
            onNextQuestion = {}
        )
    }
}

@Preview(showBackground = true, name = "Fill In The Blank")
@Composable
private fun FillInBlankPreview() {
    MaterialTheme {
        PracticeContent(
            uiState = PracticeUiState(
                isLoading = false,
                cards = previewCards,
                quizType = QuizType.FILL_IN_THE_BLANK,
                fillInBlankQuestion = FillInBlankQuestion(
                    cardId = "1",
                    sentence = "His (_____) approach helped him pass the exam.",
                    correctAnswer = "diligent",
                    meaning = "chăm chỉ"
                )
            ),
            onBack = {},
            onQuizTypeSelected = {},
            onMultipleChoiceAnswerSelected = {},
            onFillInBlankAnswerChanged = {},
            onCheckFillInBlankAnswer = {},
            onNextQuestion = {}
        )
    }
}

private val previewCards = listOf(
    com.project.minlishapp.domain.model.Card(id = "1", word = "diligent")
)
