package com.project.minlishapp.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.minlishapp.domain.model.User
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.UserRepository
import com.project.minlishapp.domain.repository.DailyStatRepository
import com.project.minlishapp.presentation.auth.LevelInfo
import com.project.minlishapp.presentation.auth.SharedLearningTargets
import com.project.minlishapp.presentation.auth.SharedLevels
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val dailyStatRepository: DailyStatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.currentUser
                .flatMapLatest { firebaseUser ->
                    if (firebaseUser != null) {
                        _uiState.update { it.copy(email = firebaseUser.email ?: "") }
                        viewModelScope.launch {
                            dailyStatRepository.getAllStats(firebaseUser.uid).collect { stats ->
                                val totalCorrect = stats.sumOf { it.correctReviews }
                                val totalReviews = stats.sumOf { it.totalReviews }
                                val accuracy = if (totalReviews > 0) {
                                    (totalCorrect.toFloat() / totalReviews) * 100f
                                } else {
                                    0f
                                }
                                _uiState.update { it.copy(overallAccuracy = accuracy) }
                            }
                        }
                        userRepository.getUser(firebaseUser.uid)
                    } else {
                        flowOf(null)
                    }
                }
                .collect { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                user = user,
                                name = user.name,
                                learningTarget = user.learningTarget,
                                currentLevel = user.currentLevel,
                                profilePictureUrl = user.profilePictureUrl,
                                email = user.email
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, isSuccess = false, errorMessage = null) }
    }

    fun onLearningTargetChange(target: String) {
        _uiState.update { it.copy(learningTarget = target, isSuccess = false, errorMessage = null) }
    }

    fun onCurrentLevelChange(level: String) {
        _uiState.update { it.copy(currentLevel = level, isSuccess = false, errorMessage = null) }
    }

    fun onProfilePictureChange(url: String) {
        val currentUser = _uiState.value.user
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy người dùng hiện tại") }
            return
        }

        _uiState.update { it.copy(profilePictureUrl = url, isSuccess = false, errorMessage = null) }

        viewModelScope.launch {
            try {
                val updatedUser = currentUser.copy(
                    profilePictureUrl = url
                )
                userRepository.saveUser(updatedUser)

                _uiState.update { it.copy(user = updatedUser, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = e.localizedMessage ?: "Tự động lưu ảnh đại diện thất bại")
                }
            }
        }
    }

    fun updateUserProfile() {
        val currentUser = _uiState.value.user
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "Không tìm thấy người dùng hiện tại") }
            return
        }

        if (_uiState.value.name.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Họ tên không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isSuccess = false, errorMessage = null) }
            try {
                val updatedUser = currentUser.copy(
                    name = _uiState.value.name.trim(),
                    learningTarget = _uiState.value.learningTarget,
                    currentLevel = _uiState.value.currentLevel,
                    profilePictureUrl = _uiState.value.profilePictureUrl
                )
                userRepository.saveUser(updatedUser)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Cập nhật thất bại"
                    )
                }
            }
        }
    }

    fun resetSuccessState() {
        _uiState.update { it.copy(isSuccess = false) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { ProfileUiState() }
        }
    }
}



data class ProfileUiState(
    val user: User? = null,
    val name: String = "",
    val learningTarget: String = "",
    val currentLevel: String = "",
    val profilePictureUrl: String? = null,
    val email: String = "",
    val overallAccuracy: Float = 0f,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val learningTargets: List<String> = SharedLearningTargets,
    val levels: List<LevelInfo> = SharedLevels
)
