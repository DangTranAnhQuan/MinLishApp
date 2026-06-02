package com.project.minlishapp.presentation.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.Deck
import com.project.minlishapp.domain.model.PracticeAttempt
import com.project.minlishapp.domain.model.PracticeQuizType
import com.project.minlishapp.domain.model.PracticeSessionMode
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.DeckRepository
import com.project.minlishapp.domain.repository.PracticeRepository
import com.project.minlishapp.domain.usecase.quiz.ApplyPracticeAnswerUseCase
import com.project.minlishapp.domain.usecase.quiz.BuildPracticeQueueUseCase
import com.project.minlishapp.domain.usecase.quiz.FillInBlankQuestion
import com.project.minlishapp.domain.usecase.quiz.GenerateQuizUseCase
import com.project.minlishapp.domain.usecase.quiz.MultipleChoiceQuestion
import com.project.minlishapp.domain.usecase.srs.GetReviewScheduleUseCase
import com.project.minlishapp.domain.usecase.srs.GetReviewForecastUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewForecastBucket
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import com.project.minlishapp.domain.usecase.srs.ReviewSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val deckRepository: DeckRepository,
    private val authRepository: AuthRepository,
    private val practiceRepository: PracticeRepository,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val buildPracticeQueueUseCase: BuildPracticeQueueUseCase,
    private val applyPracticeAnswerUseCase: ApplyPracticeAnswerUseCase,
    private val getReviewScheduleUseCase: GetReviewScheduleUseCase,
    private val getReviewForecastUseCase: GetReviewForecastUseCase
) : ViewModel() {

    private val initialDeckId = savedStateHandle.get<String>("deckId")
        .orEmpty()
        .takeUnless { it == DEBUG_DECK_ID }
        .orEmpty()
    private var pendingSubmission: PendingPracticeSubmission? = null

    private val _uiState = MutableStateFlow(
        PracticeUiState(
            selectedDeckId = initialDeckId,
            sessionMode = if (initialDeckId.isBlank()) {
                PracticeSessionMode.SPACED_REPETITION
            } else {
                PracticeSessionMode.DECK_PRACTICE
            }
        )
    )
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    init {
        observePracticeData()
    }

    private fun observePracticeData() {
        viewModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { user ->
                    val userId = user?.uid.orEmpty()
                    if (userId.isBlank()) {
                        flowOf(PracticeData())
                    } else {
                        combine(
                            deckRepository.getDecks(userId),
                            cardRepository.getCardsByUser(userId)
                        ) { decks, cards ->
                            PracticeData(userId = userId, decks = decks, cards = cards)
                        }
                    }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage
                                ?: "Không thể tải dữ liệu luyện tập."
                        )
                    }
                }
                .collect { data ->
                    _uiState.update { currentState ->
                        val selectedDeckId = currentState.selectedDeckId
                            .takeIf { id -> data.decks.any { it.id == id } }
                            .orEmpty()
                        refreshSetupDetails(
                            currentState.copy(
                                userId = data.userId,
                                decks = data.decks,
                                userCards = data.cards,
                                selectedDeckId = selectedDeckId,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    }
                }
        }
    }

    fun selectDeck(deckId: String) {
        if (_uiState.value.phase != PracticePhase.SETUP) return
        _uiState.update {
            refreshSetupDetails(
                it.copy(
                    selectedDeckId = deckId,
                    setupMessage = null
                )
            )
        }
    }

    fun selectSessionMode(sessionMode: PracticeSessionMode) {
        if (_uiState.value.phase != PracticePhase.SETUP) return
        _uiState.update {
            refreshSetupDetails(
                it.copy(
                    sessionMode = sessionMode,
                    setupMessage = null
                )
            )
        }
    }

    fun selectQuizType(quizType: QuizType) {
        if (_uiState.value.phase != PracticePhase.SETUP) return
        _uiState.update {
            refreshSetupDetails(
                it.copy(
                    quizType = quizType,
                    setupMessage = null
                )
            )
        }
    }

    fun startSession() {
        val state = _uiState.value
        if (state.userId.isBlank()) {
            _uiState.update { it.copy(setupMessage = "Bạn cần đăng nhập để bắt đầu luyện tập.") }
            return
        }
        if (state.quizType == QuizType.FLASHCARD) {
            _uiState.update { it.copy(setupMessage = "Hãy dùng nút Flashcard để mở màn lật thẻ.") }
            return
        }
        if (state.sessionMode == PracticeSessionMode.DECK_PRACTICE && state.selectedDeckId.isBlank()) {
            _uiState.update { it.copy(setupMessage = "Hãy chọn một bộ từ trước khi bắt đầu.") }
            return
        }

        val queueCardIds = buildPracticeQueueUseCase(
            cards = state.practiceCards,
            distractorCards = distractorCardsFor(state),
            quizType = state.quizType.toDomain(),
            sessionMode = state.sessionMode,
            currentTimeMs = System.currentTimeMillis()
        )
        if (queueCardIds.isEmpty()) {
            _uiState.update { it.copy(setupMessage = unavailableMessage(state)) }
            return
        }

        pendingSubmission = null
        _uiState.update {
            it.copy(
                phase = PracticePhase.IN_PROGRESS,
                sessionId = UUID.randomUUID().toString(),
                queueCardIds = queueCardIds,
                currentQuestionIndex = 0,
                correctAnswerCount = 0,
                incorrectAnswerCount = 0,
                setupMessage = null
            ).clearedAnswer()
        }
        showCurrentQuestion()
    }

    fun selectMultipleChoiceAnswer(answer: String) {
        val state = _uiState.value
        val question = state.multipleChoiceQuestion ?: return
        if (state.phase != PracticePhase.IN_PROGRESS || state.feedback != null) return

        submitAnswer(
            feedback = if (answer == question.correctAnswer) {
                AnswerFeedback.CORRECT
            } else {
                AnswerFeedback.INCORRECT
            },
            selectedMultipleChoiceAnswer = answer
        )
    }

    fun onFillInBlankAnswerChange(answer: String) {
        if (_uiState.value.feedback != null) return
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
        if (state.phase != PracticePhase.IN_PROGRESS || state.feedback != null) return

        val answer = state.fillInBlankAnswer.trim()
        if (answer.isBlank()) {
            _uiState.update { it.copy(answerErrorMessage = "Hãy nhập từ cần điền.") }
            return
        }

        submitAnswer(
            feedback = if (answer.equals(question.correctAnswer, ignoreCase = true)) {
                AnswerFeedback.CORRECT
            } else {
                AnswerFeedback.INCORRECT
            }
        )
    }

    fun continueSession() {
        val state = _uiState.value
        if (state.phase != PracticePhase.IN_PROGRESS || !state.isResultSaved) return

        if (state.isLastQuestion) {
            _uiState.update {
                it.copy(
                    phase = PracticePhase.COMPLETED,
                    multipleChoiceQuestion = null,
                    fillInBlankQuestion = null
                ).clearedAnswer()
            }
            return
        }

        _uiState.update {
            it.copy(currentQuestionIndex = it.currentQuestionIndex + 1).clearedAnswer()
        }
        showCurrentQuestion()
    }

    fun retrySaveResult() {
        pendingSubmission?.let(::persistPracticeSubmission)
    }

    fun reviewCurrentAnswer(grade: ReviewGrade) {
        val state = _uiState.value
        if (
            state.phase != PracticePhase.IN_PROGRESS ||
            state.feedback == null ||
            state.selectedReviewGrade != null
        ) {
            return
        }

        val cardId = when (state.quizType) {
            QuizType.FLASHCARD -> null
            QuizType.MULTIPLE_CHOICE -> state.multipleChoiceQuestion?.cardId
            QuizType.FILL_IN_THE_BLANK -> state.fillInBlankQuestion?.cardId
        } ?: return

        _uiState.update { it.copy(selectedReviewGrade = grade) }
        savePracticeAttempt(cardId, state.feedback, grade)
    }

    fun restartSession() {
        if (_uiState.value.phase != PracticePhase.COMPLETED) return
        startSession()
    }

    fun returnToSetup() {
        if (_uiState.value.phase == PracticePhase.SETUP) return
        pendingSubmission = null
        _uiState.update {
            refreshSetupDetails(
                it.copy(
                    phase = PracticePhase.SETUP,
                    sessionId = "",
                    queueCardIds = emptyList(),
                    currentQuestionIndex = 0,
                    correctAnswerCount = 0,
                    incorrectAnswerCount = 0,
                    multipleChoiceQuestion = null,
                    fillInBlankQuestion = null
                ).clearedAnswer()
            )
        }
    }

    private fun submitAnswer(
        feedback: AnswerFeedback,
        selectedMultipleChoiceAnswer: String? = null
    ) {
        _uiState.update {
            it.copy(
                selectedMultipleChoiceAnswer = selectedMultipleChoiceAnswer,
                feedback = feedback,
                correctAnswerCount = it.correctAnswerCount + if (feedback == AnswerFeedback.CORRECT) 1 else 0,
                incorrectAnswerCount = it.incorrectAnswerCount + if (feedback == AnswerFeedback.INCORRECT) 1 else 0,
                isSavingResult = false,
                isResultSaved = false,
                resultSaveErrorMessage = null,
                answerErrorMessage = null
            )
        }
    }

    private fun showCurrentQuestion() {
        val state = _uiState.value
        val cardId = state.queueCardIds.getOrNull(state.currentQuestionIndex) ?: return
        val card = state.practiceCards.firstOrNull { it.id == cardId } ?: return

        _uiState.update {
            when (it.quizType) {
                QuizType.FLASHCARD -> it

                QuizType.MULTIPLE_CHOICE -> it.copy(
                    multipleChoiceQuestion = generateQuizUseCase.generateMultipleChoiceForCard(
                        answerCard = card,
                        distractorCards = distractorCardsFor(it)
                    ),
                    fillInBlankQuestion = null
                )

                QuizType.FILL_IN_THE_BLANK -> it.copy(
                    multipleChoiceQuestion = null,
                    fillInBlankQuestion = generateQuizUseCase.generateFillInBlankForCard(card)
                )
            }
        }
    }

    private fun savePracticeAttempt(
        cardId: String,
        feedback: AnswerFeedback,
        grade: ReviewGrade
    ) {
        val state = _uiState.value
        if (state.userId.isBlank() || state.sessionId.isBlank()) {
            _uiState.update {
                it.copy(
                    selectedReviewGrade = null,
                    isSavingResult = false,
                    isResultSaved = false,
                    resultSaveErrorMessage = "Không thể xác định phiên luyện tập để lưu kết quả."
                )
            }
            return
        }

        val currentCard = state.practiceCards.firstOrNull { it.id == cardId }
        if (currentCard == null) {
            _uiState.update {
                it.copy(
                    selectedReviewGrade = null,
                    isSavingResult = false,
                    isResultSaved = false,
                    resultSaveErrorMessage = "Không tìm thấy thẻ để cập nhật lịch ôn tập."
                )
            }
            return
        }

        val reviewResult = applyPracticeAnswerUseCase(
            card = currentCard,
            grade = grade
        )
        val attempt = PracticeAttempt(
            id = UUID.randomUUID().toString(),
            sessionId = state.sessionId,
            userId = state.userId,
            deckId = currentCard.deckId,
            cardId = cardId,
            quizType = state.quizType.toDomain(),
            sessionMode = state.sessionMode,
            isCorrect = feedback == AnswerFeedback.CORRECT,
            qualityScore = reviewResult.grade.qualityScore,
            sm2IntervalDays = reviewResult.reviewedCard.sm2Interval,
            sm2EaseFactor = reviewResult.reviewedCard.sm2EaseFactor,
            nextReviewTime = reviewResult.reviewedCard.nextReviewTime
        )
        val submission = PendingPracticeSubmission(
            attempt = attempt,
            reviewedCard = reviewResult.reviewedCard
        )
        pendingSubmission = submission
        persistPracticeSubmission(submission)
    }

    private fun persistPracticeSubmission(submission: PendingPracticeSubmission) {
        if (_uiState.value.isSavingResult) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSavingResult = true,
                    resultSaveErrorMessage = null
                )
            }
            runCatching {
                practiceRepository.saveReviewedAttempt(
                    attempt = submission.attempt,
                    reviewedCard = submission.reviewedCard
                )
            }.onSuccess {
                if (pendingSubmission?.attempt?.id == submission.attempt.id) {
                    pendingSubmission = null
                    _uiState.update {
                        val updatedUserCards = it.userCards.replaceCard(submission.reviewedCard)
                        refreshSetupDetails(
                            it.copy(
                                userCards = updatedUserCards,
                                isSavingResult = false,
                                isResultSaved = true,
                                resultSaveErrorMessage = null,
                                lastReviewIntervalDays = submission.reviewedCard.sm2Interval,
                                lastEaseFactor = submission.reviewedCard.sm2EaseFactor,
                                lastNextReviewTime = submission.reviewedCard.nextReviewTime
                            )
                        )
                    }
                }
            }.onFailure { throwable ->
                if (pendingSubmission?.attempt?.id == submission.attempt.id) {
                    _uiState.update {
                        it.copy(
                            isSavingResult = false,
                            isResultSaved = false,
                            resultSaveErrorMessage = throwable.localizedMessage
                                ?: "Không thể lưu kết quả luyện tập."
                        )
                    }
                }
            }
        }
    }

    private fun refreshSetupDetails(state: PracticeUiState): PracticeUiState {
        val learnedCards = state.userCards.filter { it.sm2Interval > 0 }
        val practiceCards = when (state.sessionMode) {
            PracticeSessionMode.SPACED_REPETITION -> learnedCards
            PracticeSessionMode.DECK_PRACTICE -> {
                state.userCards.filter { it.deckId == state.selectedDeckId }
            }
        }
        val distractorCards = distractorCardsFor(state.copy(practiceCards = practiceCards))
        val eligibleCards = when (state.quizType) {
            QuizType.FLASHCARD -> practiceCards.filter { it.word.isNotBlank() }

            QuizType.MULTIPLE_CHOICE -> {
                generateQuizUseCase.eligibleMultipleChoiceCards(practiceCards, distractorCards)
            }

            QuizType.FILL_IN_THE_BLANK -> {
                generateQuizUseCase.eligibleFillInBlankCards(practiceCards)
            }
        }
            .distinctBy { it.id }
            .sortedBy { it.nextReviewTime.time }
            .distinctBy { it.word.trim().lowercase() }
        val now = System.currentTimeMillis()

        return state.copy(
            practiceCards = practiceCards,
            availableQuestionCount = eligibleCards.size,
            sessionQuestionCount = buildPracticeQueueUseCase(
                cards = practiceCards,
                distractorCards = distractorCards,
                quizType = state.quizType.toDomain(),
                sessionMode = state.sessionMode,
                currentTimeMs = now
            ).size,
            newWordsCount = state.userCards.count { it.sm2Interval == 0 },
            reviewSchedule = getReviewScheduleUseCase(learnedCards, now),
            reviewForecast = getReviewForecastUseCase(learnedCards, now)
        )
    }

    private fun distractorCardsFor(state: PracticeUiState): List<Card> {
        val distinctPracticeMeanings = state.practiceCards
            .map { it.meaning.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .size
        return if (distinctPracticeMeanings >= REQUIRED_MULTIPLE_CHOICE_MEANINGS) {
            state.practiceCards
        } else {
            state.userCards
        }
    }

    private fun unavailableMessage(state: PracticeUiState): String {
        val scopeMessage = when (state.sessionMode) {
            PracticeSessionMode.SPACED_REPETITION -> "Không có từ đã học đến hạn"
            PracticeSessionMode.DECK_PRACTICE -> "Không có từ hợp lệ trong bộ từ đã chọn"
        }
        return when (state.quizType) {
            QuizType.FLASHCARD -> {
                "$scopeMessage cho Flashcard."
            }

            QuizType.MULTIPLE_CHOICE -> {
                "$scopeMessage hoặc chưa đủ dữ liệu cho câu trắc nghiệm. Cần ít nhất 4 nghĩa khác nhau trong kho từ của bạn."
            }

            QuizType.FILL_IN_THE_BLANK -> {
                "$scopeMessage cho bài điền từ. Bài điền từ cần câu ví dụ chứa chính từ khóa."
            }
        }
    }

    private fun PracticeUiState.clearedAnswer(): PracticeUiState {
        return copy(
            selectedMultipleChoiceAnswer = null,
            fillInBlankAnswer = "",
            feedback = null,
            selectedReviewGrade = null,
            isSavingResult = false,
            isResultSaved = false,
            resultSaveErrorMessage = null,
            lastReviewIntervalDays = null,
            lastEaseFactor = null,
            lastNextReviewTime = null,
            answerErrorMessage = null
        )
    }

    private fun List<Card>.replaceCard(updatedCard: Card): List<Card> {
        return map { card -> if (card.id == updatedCard.id) updatedCard else card }
    }

    private companion object {
        const val DEBUG_DECK_ID = "debug_deck"
        const val REQUIRED_MULTIPLE_CHOICE_MEANINGS = 4
    }
}

private data class PracticeData(
    val userId: String = "",
    val decks: List<Deck> = emptyList(),
    val cards: List<Card> = emptyList()
)

private data class PendingPracticeSubmission(
    val attempt: PracticeAttempt,
    val reviewedCard: Card
)

enum class PracticePhase {
    SETUP,
    IN_PROGRESS,
    COMPLETED
}

enum class QuizType {
    FLASHCARD,
    MULTIPLE_CHOICE,
    FILL_IN_THE_BLANK;

    fun toDomain(): PracticeQuizType {
        return when (this) {
            FLASHCARD -> PracticeQuizType.FLASHCARD
            MULTIPLE_CHOICE -> PracticeQuizType.MULTIPLE_CHOICE
            FILL_IN_THE_BLANK -> PracticeQuizType.FILL_IN_THE_BLANK
        }
    }
}

enum class AnswerFeedback {
    CORRECT,
    INCORRECT
}

data class PracticeUiState(
    val userId: String = "",
    val decks: List<Deck> = emptyList(),
    val userCards: List<Card> = emptyList(),
    val selectedDeckId: String = "",
    val practiceCards: List<Card> = emptyList(),
    val sessionMode: PracticeSessionMode = PracticeSessionMode.SPACED_REPETITION,
    val quizType: QuizType = QuizType.FLASHCARD,
    val phase: PracticePhase = PracticePhase.SETUP,
    val sessionId: String = "",
    val queueCardIds: List<String> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val availableQuestionCount: Int = 0,
    val sessionQuestionCount: Int = 0,
    val newWordsCount: Int = 0,
    val correctAnswerCount: Int = 0,
    val incorrectAnswerCount: Int = 0,
    val multipleChoiceQuestion: MultipleChoiceQuestion? = null,
    val fillInBlankQuestion: FillInBlankQuestion? = null,
    val selectedMultipleChoiceAnswer: String? = null,
    val fillInBlankAnswer: String = "",
    val feedback: AnswerFeedback? = null,
    val selectedReviewGrade: ReviewGrade? = null,
    val isSavingResult: Boolean = false,
    val isResultSaved: Boolean = false,
    val resultSaveErrorMessage: String? = null,
    val lastReviewIntervalDays: Int? = null,
    val lastEaseFactor: Double? = null,
    val lastNextReviewTime: java.util.Date? = null,
    val reviewSchedule: ReviewSchedule = ReviewSchedule(),
    val reviewForecast: List<ReviewForecastBucket> = emptyList(),
    val isLoading: Boolean = true,
    val setupMessage: String? = null,
    val answerErrorMessage: String? = null,
    val errorMessage: String? = null
) {
    val selectedDeck: Deck?
        get() = decks.firstOrNull { it.id == selectedDeckId }

    val sessionTitle: String
        get() = when (sessionMode) {
            PracticeSessionMode.SPACED_REPETITION -> "Ôn theo lịch SM-2"
            PracticeSessionMode.DECK_PRACTICE -> selectedDeck?.title.orEmpty()
        }

    val canStartSession: Boolean
        get() = sessionQuestionCount > 0 &&
            (sessionMode == PracticeSessionMode.SPACED_REPETITION || selectedDeck != null)

    val completedAnswerCount: Int
        get() = correctAnswerCount + incorrectAnswerCount

    val totalQuestionCount: Int
        get() = queueCardIds.size

    val isLastQuestion: Boolean
        get() = totalQuestionCount > 0 && currentQuestionIndex == totalQuestionCount - 1

    val hasUnsavedResult: Boolean
        get() = feedback != null && !isResultSaved

    val progress: Float
        get() = if (totalQuestionCount == 0) 0f else completedAnswerCount.toFloat() / totalQuestionCount
}
