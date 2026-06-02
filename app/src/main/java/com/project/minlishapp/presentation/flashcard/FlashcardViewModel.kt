package com.project.minlishapp.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.core.navigation.Screen
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.model.PracticeAttempt
import com.project.minlishapp.domain.model.PracticeQuizType
import com.project.minlishapp.domain.model.PracticeSessionMode
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.PracticeRepository
import com.project.minlishapp.domain.usecase.quiz.FilterUsableFlashcardsUseCase
import com.project.minlishapp.domain.usecase.srs.CalculateSm2NextReviewUseCase
import com.project.minlishapp.domain.usecase.srs.GetDueCardsUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
import com.project.minlishapp.domain.usecase.stat.UpdatePracticeStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import com.project.minlishapp.utils.TestDataInjection
import java.util.UUID
import java.util.concurrent.TimeoutException

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val cardRepository: CardRepository,
    private val practiceRepository: PracticeRepository,
    private val calculateSm2NextReviewUseCase: CalculateSm2NextReviewUseCase,
    private val getDueCardsUseCase: GetDueCardsUseCase,
    private val filterUsableFlashcardsUseCase: FilterUsableFlashcardsUseCase,
    private val updatePracticeStatsUseCase: UpdatePracticeStatsUseCase
) : ViewModel() {

    private val deckId: String = savedStateHandle.get<String>("deckId").orEmpty()
    private val isSpacedRepetitionReview =
        deckId == Screen.FlashcardLearning.SPACED_REPETITION_DECK_ID
    private val sessionId = UUID.randomUUID().toString()
    private val reviewedCardIds = mutableSetOf<String>()
    private var dueCardsJob: Job? = null

    private val _uiState = MutableStateFlow(
        FlashcardUiState(
            deckId = deckId,
            isSpacedRepetitionReview = isSpacedRepetitionReview
        )
    )
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        if (deckId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Thiếu deckId để học flashcard."
            )
        } else if (!isSpacedRepetitionReview) {
            observeDeckCards()
        }

        viewModelScope.launch {
            authRepository.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    dueCardsJob?.cancel()
                    if (userId.isNullOrBlank()) {
                        _uiState.value = _uiState.value.copy(
                            userId = null,
                            dueWordsCount = 0,
                            isLoading = if (isSpacedRepetitionReview) false else _uiState.value.isLoading,
                            errorMessage = if (isSpacedRepetitionReview) {
                                "Bạn cần đăng nhập để ôn tập."
                            } else {
                                _uiState.value.errorMessage
                            }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(userId = userId)
                        dueCardsJob = launch {
                            runCatching {
                                getDueCardsUseCase(userId, System.currentTimeMillis()).collectLatest { dueCards ->
                                    val learnedDueCards = dueCards
                                        .filter { it.sm2Interval > 0 }
                                        .sortedBy { it.nextReviewTime.time }
                                        .let(filterUsableFlashcardsUseCase::invoke)
                                        .filterNot { it.id in reviewedCardIds }
                                    _uiState.value = if (isSpacedRepetitionReview) {
                                        _uiState.value.copy(
                                            cards = learnedDueCards,
                                            currentCardIndex = _uiState.value.currentCardIndex
                                                .coerceAtMost((learnedDueCards.size - 1).coerceAtLeast(0)),
                                            isLoading = false,
                                            newWordsCount = 0,
                                            learnedWordsCount = learnedDueCards.size,
                                            dueWordsCount = learnedDueCards.size,
                                            dueWordsInDeckCount = learnedDueCards.size,
                                            sessionTotalCount = maxOf(
                                                _uiState.value.sessionTotalCount,
                                                learnedDueCards.size + reviewedCardIds.size
                                            ),
                                            errorMessage = null
                                        )
                                    } else {
                                        _uiState.value.copy(dueWordsCount = learnedDueCards.size)
                                    }
                                }
                            }.onFailure { throwable ->
                                // Log but don't crash - due cards are optional
                                _uiState.value = _uiState.value.copy(
                                    isLoading = if (isSpacedRepetitionReview) false else _uiState.value.isLoading,
                                    dueWordsCount = 0,
                                    errorMessage = if (isSpacedRepetitionReview) {
                                        throwable.localizedMessage ?: "Không thể tải từ cần ôn."
                                    } else {
                                        _uiState.value.errorMessage
                                    }
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun observeDeckCards() {
        viewModelScope.launch {
            runCatching {
                cardRepository.getCardsInDeck(deckId).collectLatest { cards ->
                    val usableCards = filterUsableFlashcardsUseCase(cards)
                    val remainingCards = usableCards.filterNot { it.id in reviewedCardIds }
                    val currentIndex = if (remainingCards.isEmpty()) {
                        0
                    } else {
                        _uiState.value.currentCardIndex.coerceIn(0, remainingCards.lastIndex)
                    }
                    val now = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cards = remainingCards,
                        currentCardIndex = currentIndex,
                        newWordsCount = usableCards.count { it.sm2Interval == 0 },
                        learnedWordsCount = usableCards.count { it.sm2Interval > 0 },
                        dueWordsInDeckCount = usableCards.count {
                            it.sm2Interval > 0 && it.nextReviewTime.time <= now
                        },
                        sessionTotalCount = maxOf(
                            _uiState.value.sessionTotalCount,
                            remainingCards.size + reviewedCardIds.size
                        ),
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                val errorMsg = when {
                    throwable.message?.contains("Unknown calling package", ignoreCase = true) == true ->
                        "Firebase unavailable (GMS error). Try clicking 'Insert Test Data' or use emulator with Play Services."
                    throwable.message?.contains("SecurityException", ignoreCase = true) == true ->
                        "Security error connecting to Firebase. Please check your credentials."
                    else -> throwable.localizedMessage ?: "Failed to load cards from database."
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cards = emptyList(),
                    errorMessage = errorMsg
                )
            }
        }
    }

    fun toggleFlip() {
        val currentCard = currentCard() ?: return
        if (currentCard.word.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                isFlipped = !_uiState.value.isFlipped,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun reviewCurrentCard(grade: ReviewGrade) {
        val currentCard = currentCard() ?: return
        val userId = _uiState.value.userId
        if (userId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Bạn cần đăng nhập để lưu kết quả ôn tập."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, statusMessage = null, errorMessage = null)
            runCatching {
                val updatedCard = calculateSm2NextReviewUseCase(currentCard, grade)
                val attempt = PracticeAttempt(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    userId = userId,
                    deckId = currentCard.deckId,
                    cardId = currentCard.id,
                    quizType = PracticeQuizType.FLASHCARD,
                    sessionMode = if (isSpacedRepetitionReview) {
                        PracticeSessionMode.SPACED_REPETITION
                    } else {
                        PracticeSessionMode.DECK_PRACTICE
                    },
                    isCorrect = grade.qualityScore >= ReviewGrade.GOOD.qualityScore,
                    qualityScore = grade.qualityScore,
                    sm2IntervalDays = updatedCard.sm2Interval,
                    sm2EaseFactor = updatedCard.sm2EaseFactor,
                    nextReviewTime = updatedCard.nextReviewTime
                )
                // Add timeout (8 seconds) to prevent hanging indefinitely when Firestore is unavailable
                val updateSuccess = withTimeoutOrNull(8000L) {
                    practiceRepository.saveReviewedAttempt(attempt, updatedCard)
                    true
                }
                if (updateSuccess == null) {
                    throw TimeoutException("Card update timed out after 8 seconds. Check Firestore connection.")
                }
                launch {
                    runCatching {
                        updatePracticeStatsUseCase(
                            userId = userId,
                            card = currentCard,
                            isCorrect = attempt.isCorrect
                        )
                    }.onFailure { throwable ->
                        android.util.Log.e(
                            "FlashcardViewModel",
                            "Unable to update dashboard stats.",
                            throwable
                        )
                    }
                }
                reviewedCardIds += updatedCard.id
                val currentState = _uiState.value
                val remainingCards = currentState.cards.filterNot { it.id == updatedCard.id }
                val nextIndex = currentState.currentCardIndex
                    .coerceAtMost((remainingCards.size - 1).coerceAtLeast(0))
                _uiState.value = currentState.copy(
                    cards = remainingCards,
                    currentCardIndex = nextIndex,
                    isFlipped = false,
                    isSubmitting = false,
                    isSessionCompleted = remainingCards.isEmpty(),
                    completedReviewCount = reviewedCardIds.size,
                    sessionTotalCount = maxOf(currentState.sessionTotalCount, reviewedCardIds.size),
                    sessionNextReviewTime = listOfNotNull(
                        currentState.sessionNextReviewTime,
                        updatedCard.nextReviewTime
                    ).minByOrNull { it.time },
                    dueWordsCount = if (isSpacedRepetitionReview) remainingCards.size else currentState.dueWordsCount,
                    dueWordsInDeckCount = if (isSpacedRepetitionReview) remainingCards.size else currentState.dueWordsInDeckCount,
                    learnedWordsCount = if (isSpacedRepetitionReview) remainingCards.size else currentState.learnedWordsCount,
                    statusMessage = when (grade) {
                        ReviewGrade.AGAIN -> "Đã đánh dấu cần ôn lại."
                        ReviewGrade.HARD -> "Đã cập nhật mức nhớ khó."
                        ReviewGrade.GOOD -> "Đã ghi nhận mức nhớ tốt."
                        ReviewGrade.EASY -> "Đã ghi nhận mức nhớ thành thạo."
                    }
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = throwable.localizedMessage ?: "Không thể cập nhật thẻ hiện tại."
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun insertTestData() {
        viewModelScope.launch {
            val userId = _uiState.value.userId.orEmpty()
            if (userId.isBlank() || deckId.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "userId or deckId missing; cannot insert test data"
                )
                return@launch
            }
            runCatching {
                TestDataInjection.insertSampleCardsForDeck(
                    repository = cardRepository,
                    deckId = deckId,
                    userId = userId
                )
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Successfully inserted 6 sample cards! Check now to see them."
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to insert test cards: ${throwable.localizedMessage}"
                )
            }
        }
    }

    private fun currentCard(): Card? {
        return _uiState.value.cards.getOrNull(_uiState.value.currentCardIndex)
    }
}

data class FlashcardUiState(
    val deckId: String = "",
    val isSpacedRepetitionReview: Boolean = false,
    val userId: String? = null,
    val cards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isFlipped: Boolean = false,
    val newWordsCount: Int = 0,
    val learnedWordsCount: Int = 0,
    val dueWordsCount: Int = 0,
    val dueWordsInDeckCount: Int = 0,
    val completedReviewCount: Int = 0,
    val sessionTotalCount: Int = 0,
    val sessionNextReviewTime: java.util.Date? = null,
    val isSessionCompleted: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)
