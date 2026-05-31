package com.project.minlishapp.presentation.flashcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.model.Card
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.usecase.srs.CalculateSm2NextReviewUseCase
import com.project.minlishapp.domain.usecase.srs.GetDueCardsUseCase
import com.project.minlishapp.domain.usecase.srs.ReviewGrade
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
import java.util.concurrent.TimeoutException

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val cardRepository: CardRepository,
    private val calculateSm2NextReviewUseCase: CalculateSm2NextReviewUseCase,
    private val getDueCardsUseCase: GetDueCardsUseCase
) : ViewModel() {

    private val deckId: String = savedStateHandle.get<String>("deckId").orEmpty()
    private var dueCardsJob: Job? = null

    private val _uiState = MutableStateFlow(FlashcardUiState(deckId = deckId))
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    init {
        if (deckId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Thiếu deckId để học flashcard."
            )
        } else {
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
                            dueWordsCount = 0
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(userId = userId)
                        dueCardsJob = launch {
                            runCatching {
                                getDueCardsUseCase(userId, System.currentTimeMillis()).collectLatest { dueCards ->
                                    _uiState.value = _uiState.value.copy(dueWordsCount = dueCards.size)
                                }
                            }.onFailure { throwable ->
                                // Log but don't crash - due cards are optional
                                _uiState.value = _uiState.value.copy(dueWordsCount = 0)
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
                    val currentIndex = if (cards.isEmpty()) {
                        0
                    } else {
                        _uiState.value.currentCardIndex.coerceIn(0, cards.lastIndex)
                    }
                    val now = System.currentTimeMillis()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cards = cards,
                        currentCardIndex = currentIndex,
                        newWordsCount = cards.count { it.sm2Repetitions == 0 },
                        learnedWordsCount = cards.count { it.sm2Repetitions > 0 },
                        dueWordsInDeckCount = cards.count { it.nextReviewTime.time <= now },
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, statusMessage = null, errorMessage = null)
            runCatching {
                val updatedCard = calculateSm2NextReviewUseCase(currentCard, grade)
                // Add timeout (8 seconds) to prevent hanging indefinitely when Firestore is unavailable
                val updateSuccess = withTimeoutOrNull(8000L) {
                    cardRepository.updateCard(updatedCard)
                    true
                }
                if (updateSuccess == null) {
                    throw TimeoutException("Card update timed out after 8 seconds. Check Firestore connection.")
                }
                val nextIndex = if (_uiState.value.cards.size <= 1) {
                    0
                } else {
                    (_uiState.value.currentCardIndex + 1) % _uiState.value.cards.size
                }
                _uiState.value = _uiState.value.copy(
                    currentCardIndex = nextIndex,
                    isFlipped = false,
                    isSubmitting = false,
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
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

