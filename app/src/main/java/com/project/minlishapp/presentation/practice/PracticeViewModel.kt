package com.project.minlishapp.presentation.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.usecase.quiz.FillInBlankQuestion
import com.project.minlishapp.domain.usecase.quiz.GenerateQuizUseCase
import com.project.minlishapp.domain.usecase.quiz.MultipleChoiceQuestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val authRepository: AuthRepository,
    private val generateQuizUseCase: GenerateQuizUseCase
) : ViewModel() {

    private val deckId: String = savedStateHandle.get<String>("deckId").orEmpty()

    private val _uiState = MutableStateFlow(PracticeUiState(deckId = deckId))
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        observeDeckCards()
    }

    private fun observeDeckCards() {
        viewModelScope.launch {
            runCatching {
                val userIdFlow = authRepository.currentUser.flatMapLatest { user ->
                    val userId = user?.uid ?: ""
                    
                    val deckCardsFlow = if (deckId.isNotBlank()) {
                        cardRepository.getCardsInDeck(deckId)
                    } else if (userId.isNotBlank()) {
                        cardRepository.getCardsByUser(userId)
                    } else {
                        kotlinx.coroutines.flow.flowOf(emptyList())
                    }

                    combine(
                        deckCardsFlow,
                        cardRepository.getAllCards()
                    ) { deckCards, systemCards ->
                        deckCards to systemCards
                    }
                }

                userIdFlow.collectLatest { (deckCards, systemCards) ->
                    _uiState.update {
                        it.copy(
                            cards = deckCards,
                            systemCards = systemCards,
                            isLoading = false,
                            errorMessage = if (deckId.isBlank() && deckCards.isEmpty()) {
                                "Hãy thêm từ vựng hoặc chọn bộ từ để bắt đầu luyện tập."
                            } else null
                        )
                    }
                    if (deckCards.isNotEmpty()) {
                        showNextQuestion()
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage
                            ?: "Không thể tải dữ liệu bài luyện tập."
                    )
                }
            }
        }
    }

    fun selectQuizType(quizType: QuizType) {
        if (_uiState.value.quizType != quizType) {
            _uiState.update { it.copy(quizType = quizType) }
            showNextQuestion()
        }
    }

    fun selectMultipleChoiceAnswer(answer: String) {
        val state = _uiState.value
        val question = state.multipleChoiceQuestion ?: return
        if (state.feedback != null) return

        _uiState.update {
            it.copy(
                selectedMultipleChoiceAnswer = answer,
                feedback = if (answer == question.correctAnswer) {
                    AnswerFeedback.CORRECT
                } else {
                    AnswerFeedback.INCORRECT
                }
            )
        }
    }

    fun onFillInBlankAnswerChange(answer: String) {
        _uiState.update {
            it.copy(
                fillInBlankAnswer = answer,
                answerErrorMessage = null
            )
        }
    }

    fun checkFillInBlankAnswer() {
        val state = _uiState.value
        val question = state.fillInBlankQuestion ?: return
        val answer = state.fillInBlankAnswer.trim()

        if (answer.isBlank()) {
            _uiState.update {
                it.copy(answerErrorMessage = "Hãy nhập từ cần điền.")
            }
            return
        }

        _uiState.update {
            it.copy(
                feedback = if (answer.equals(question.correctAnswer, ignoreCase = true)) {
                    AnswerFeedback.CORRECT
                } else {
                    AnswerFeedback.INCORRECT
                },
                answerErrorMessage = null
            )
        }
    }

    fun showNextQuestion() {
        when (_uiState.value.quizType) {
            QuizType.MULTIPLE_CHOICE -> showNextMultipleChoiceQuestion()
            QuizType.FILL_IN_THE_BLANK -> showNextFillInBlankQuestion()
        }
    }

    private fun showNextMultipleChoiceQuestion() {
        val state = _uiState.value
        val eligibleQuestionCount = generateQuizUseCase.countMultipleChoiceQuestions(
            cards = state.cards,
            distractorCards = state.systemCards
        )
        val question = generateQuizUseCase.generateMultipleChoice(
            cards = state.cards,
            distractorCards = state.systemCards,
            excludedCardId = state.multipleChoiceQuestion?.cardId
        )
        _uiState.update {
            it.copy(
                multipleChoiceQuestion = question,
                fillInBlankQuestion = null,
                selectedMultipleChoiceAnswer = null,
                fillInBlankAnswer = "",
                feedback = null,
                hasAlternativeQuestion = eligibleQuestionCount > 1,
                answerErrorMessage = null,
                questionMessage = when {
                    question == null && it.cards.isNotEmpty() -> {
                        "Cần ít nhất 4 nghĩa khác nhau để tạo câu hỏi trắc nghiệm. " +
                            "Firestore hiện có ${countDistinctMeanings(it.systemCards)} nghĩa hợp lệ."
                    }
                    eligibleQuestionCount == 1 -> {
                        "Hiện chỉ có 1 câu trắc nghiệm hợp lệ. Hãy thêm thẻ để luyện tập nhiều câu hơn."
                    }
                    else -> null
                }
            )
        }
    }

    private fun countDistinctMeanings(cards: List<Card>): Int {
        return cards
            .map { it.meaning.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .size
    }

    private fun showNextFillInBlankQuestion() {
        val state = _uiState.value
        val eligibleQuestionCount = generateQuizUseCase.countFillInBlankQuestions(state.cards)
        val question = generateQuizUseCase.generateFillInBlank(
            cards = state.cards,
            excludedCardId = state.fillInBlankQuestion?.cardId
        )
        _uiState.update {
            it.copy(
                multipleChoiceQuestion = null,
                fillInBlankQuestion = question,
                selectedMultipleChoiceAnswer = null,
                fillInBlankAnswer = "",
                feedback = null,
                hasAlternativeQuestion = eligibleQuestionCount > 1,
                answerErrorMessage = null,
                questionMessage = when {
                    question == null && it.cards.isNotEmpty() -> {
                        "Cần ít nhất 1 thẻ có câu ví dụ chứa từ khóa để tạo bài điền từ."
                    }
                    eligibleQuestionCount == 1 -> {
                        "Hiện chỉ có 1 câu điền từ hợp lệ. Hãy thêm thẻ để luyện tập nhiều câu hơn."
                    }
                    else -> null
                }
            )
        }
    }
}

enum class QuizType {
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK
}

enum class AnswerFeedback {
    CORRECT,
    INCORRECT
}

data class PracticeUiState(
    val deckId: String = "",
    val cards: List<Card> = emptyList(),
    val systemCards: List<Card> = emptyList(),
    val quizType: QuizType = QuizType.MULTIPLE_CHOICE,
    val multipleChoiceQuestion: MultipleChoiceQuestion? = null,
    val fillInBlankQuestion: FillInBlankQuestion? = null,
    val selectedMultipleChoiceAnswer: String? = null,
    val fillInBlankAnswer: String = "",
    val feedback: AnswerFeedback? = null,
    val hasAlternativeQuestion: Boolean = false,
    val isLoading: Boolean = true,
    val questionMessage: String? = null,
    val answerErrorMessage: String? = null,
    val errorMessage: String? = null
)
