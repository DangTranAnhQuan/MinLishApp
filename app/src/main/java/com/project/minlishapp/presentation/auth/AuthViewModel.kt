package com.project.minlishapp.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.project.minlishapp.domain.repository.AuthRepository
import com.project.minlishapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser
                .flatMapLatest { firebaseUser ->
                    if (firebaseUser != null) {
                        _uiState.update {
                            it.copy(
                                currentUserEmail = firebaseUser.email,
                                displayName = if (it.displayName.isEmpty()) firebaseUser.displayName ?: "" else it.displayName
                            )
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
                                isAuthenticated = true,
                                isProfileComplete = user.name.isNotEmpty(),
                                isCheckingAuth = false
                            )
                        }
                    } else {
                        val isAuth = authRepository.currentUser.first() != null
                        if (isAuth) {
                            // User exists in Auth but not in Firestore yet
                            _uiState.update {
                                it.copy(
                                    isAuthenticated = true,
                                    isProfileComplete = false,
                                    isCheckingAuth = false
                                )
                            }
                        } else {
                            // Not authenticated at all
                            _uiState.update {
                                it.copy(
                                    isAuthenticated = false,
                                    isProfileComplete = null,
                                    isCheckingAuth = false,
                                    currentUserEmail = null
                                )
                            }
                        }
                    }
                }
        }
    }

    fun onEmailChange(email: String) {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        val isEmailValid = email.matches(emailRegex)
        _uiState.update {
            it.copy(
                email = email,
                isEmailValid = isEmailValid,
                emailError = if (isEmailValid || email.isEmpty()) null else "Invalid email format",
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        val isPasswordValid = password.length >= 6
        _uiState.update {
            it.copy(
                password = password,
                isPasswordValid = isPasswordValid,
                passwordError = if (isPasswordValid || password.isEmpty()) null else "Password must be at least 6 characters",
                errorMessage = null
            )
        }
    }

    fun onDisplayNameChange(displayName: String) {
        _uiState.update {
            it.copy(
                displayName = displayName,
                errorMessage = null
            )
        }
    }

    fun onLearningTargetChange(learningTarget: String) {
        _uiState.update {
            it.copy(
                learningTarget = learningTarget,
                errorMessage = null
            )
        }
    }

    fun onCurrentLevelChange(currentLevel: String) {
        _uiState.update {
            it.copy(
                currentLevel = currentLevel,
                errorMessage = null
            )
        }
    }

    fun login() {
        if (!_uiState.value.isEmailValid || !_uiState.value.isPasswordValid) {
            _uiState.update { it.copy(errorMessage = "Please correct the input errors") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithEmail(_uiState.value.email, _uiState.value.password)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = mapAuthException(result.exceptionOrNull())
                )
            }
        }
    }

    fun signUp() {
        if (!_uiState.value.isEmailValid || !_uiState.value.isPasswordValid) {
            _uiState.update { it.copy(errorMessage = "Please correct the input errors") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signUpBasic(
                _uiState.value.email,
                _uiState.value.password
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRegisterSuccess = result.isSuccess,
                    errorMessage = mapAuthException(result.exceptionOrNull())
                )
            }
        }
    }

    fun completeProfileSetup() {
        val displayName = _uiState.value.displayName.trim()
            .ifBlank { _uiState.value.currentUserEmail?.substringBefore("@") }
            ?.takeIf { it.isNotBlank() }
            ?: "Learner"

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.completeProfileSetup(
                displayName,
                _uiState.value.learningTarget,
                _uiState.value.currentLevel
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isProfileSetupSuccess = result.isSuccess,
                    errorMessage = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun resetRegisterSuccess() {
        _uiState.update { it.copy(isRegisterSuccess = false) }
    }

    fun resetProfileSetupSuccess() {
        _uiState.update { it.copy(isProfileSetupSuccess = false) }
    }

    fun loginWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithCredential(credential)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = mapAuthException(result.exceptionOrNull())
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            resetState()
        }
    }

    fun resetState() {
        _uiState.update {
            AuthUiState(
                learningTargets = SharedLearningTargets,
                levels = SharedLevels,
                isCheckingAuth = false // Maintain checked state
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapAuthException(e: Throwable?): String? {
        if (e == null) return null
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
            is FirebaseAuthUserCollisionException -> "This email is already registered."
            is FirebaseAuthWeakPasswordException -> "Password is too weak."
            is FirebaseAuthInvalidUserException -> "Account does not exist or has been disabled."
            else -> e.localizedMessage ?: "An unknown error occurred."
        }
    }
}

data class LevelInfo(
    val code: String,
    val title: String,
    val desc: String
)

val SharedLearningTargets = listOf("IELTS", "TOEIC", "Communication", "Career")

val SharedLevels = listOf(
    LevelInfo("A1", "Beginner", "Just starting out"),
    LevelInfo("A2", "Elementary", "Can understand basic phrases"),
    LevelInfo("B1", "Intermediate", "Can hold simple conversations"),
    LevelInfo("B2", "Upper Intermediate", "Can speak fluently"),
    LevelInfo("C1", "Advanced", "Can express complex ideas"),
    LevelInfo("C2", "Proficient", "Near native speaker")
)

data class AuthUiState(
    val email: String = "",
    val isEmailValid: Boolean = false,
    val emailError: String? = null,
    val password: String = "",
    val isPasswordValid: Boolean = false,
    val passwordError: String? = null,
    val displayName: String = "",
    val learningTarget: String = "IELTS",
    val currentLevel: String = "A1",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val isCheckingAuth: Boolean = true,
    val isRegisterSuccess: Boolean = false,
    val isProfileSetupSuccess: Boolean = false,
    val isProfileComplete: Boolean? = null,
    val currentUserEmail: String? = null,
    val learningTargets: List<String> = SharedLearningTargets,
    val levels: List<LevelInfo> = SharedLevels
)
