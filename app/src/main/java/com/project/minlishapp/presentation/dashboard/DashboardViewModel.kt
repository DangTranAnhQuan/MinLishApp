package com.project.minlishapp.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.CardRepository
import com.project.minlishapp.domain.repository.UserRepository
import com.project.minlishapp.domain.usecase.stat.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit.DAYS

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        DashboardUiState(
            totalWordsLearned = 0,
            currentStreak = 0,
            accuracy = 0f,
            dailyActivityData = emptyList(),
            retentionData = emptyList()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collectLatest { firebaseUser ->
                _uiState.value = DashboardUiState()
                if (firebaseUser != null) {
                    coroutineScope {
                        launch {
                            userRepository.getUser(firebaseUser.uid)
                                .catch { e -> android.util.Log.e("DashboardVM", "getUser error", e) }
                                .collect { user ->
                                if (user != null) {
                                    val effectiveStreak = calculateEffectiveStreak(user.currentStreak, user.lastLearnedDate)
                                    _uiState.update {
                                        it.copy(currentStreak = effectiveStreak)
                                    }
                                }
                            }
                        }
                        launch {
                            cardRepository.getLearnedCardsCount(firebaseUser.uid)
                                .catch { e -> android.util.Log.e("DashboardVM", "getLearnedCardsCount error", e) }
                                .collect { count ->
                                _uiState.update {
                                    it.copy(totalWordsLearned = count)
                                }
                            }
                        }
                        launch {
                            getDashboardStatsUseCase(firebaseUser.uid)
                                .catch { e -> android.util.Log.e("DashboardVM", "getDashboardStats error", e) }
                                .collect { stats ->
                                _uiState.update {
                                    it.copy(
                                        accuracy = stats.accuracy,
                                        dailyActivityData = stats.dailyActivityData,
                                        retentionData = stats.retentionData
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

internal fun calculateEffectiveStreak(
    currentStreak: Int,
    lastLearnedDate: Date?, 
    now: LocalDate = LocalDate.now(ZoneOffset.UTC) 
): Int {
    if (lastLearnedDate == null || currentStreak == 0) return 0
    val lastLearnedLocalDate = lastLearnedDate.toInstant()
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
    val daysBetween = DAYS.between(lastLearnedLocalDate, now)
    return when {
        daysBetween == 0L -> currentStreak 
        daysBetween == 1L -> currentStreak 
        else -> 0                         
    }
}
