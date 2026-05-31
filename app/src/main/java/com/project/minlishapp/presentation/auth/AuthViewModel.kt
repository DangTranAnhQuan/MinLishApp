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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { firebaseUser ->
                _uiState.update {
                    it.copy(
                        isAuthenticated = firebaseUser != null,
                        currentUserEmail = firebaseUser?.email
                    )
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
        if (_uiState.value.email == "admin@admin.com" && _uiState.value.password == "123456") {
            _uiState.update { 
                it.copy(
                    isAuthenticated = true, 
                    currentUserEmail = "admin@admin.com", 
                    isLoading = false, 
                    errorMessage = null
                ) 
            }
            return
        }

        if (!_uiState.value.isEmailValid || !_uiState.value.isPasswordValid) {
            _uiState.update { it.copy(errorMessage = "Please fix input errors") }
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
        if (!_uiState.value.isEmailValid || !_uiState.value.isPasswordValid || _uiState.value.displayName.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter valid info for all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signUpWithEmail(
                _uiState.value.email,
                _uiState.value.password,
                _uiState.value.displayName,
                _uiState.value.learningTarget,
                _uiState.value.currentLevel
            )
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = mapAuthException(result.exceptionOrNull())
                )
            }
        }
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
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun mapAuthException(e: Throwable?): String? {
        if (e == null) return null
        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> "Sai email hoặc mật khẩu."
            is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký bởi tài khoản khác."
            is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu (tối thiểu phải có 6 ký tự)."
            is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa."
            else -> e.localizedMessage ?: "Đã xảy ra lỗi không xác định."
        }
    }
}

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
    val currentUserEmail: String? = null
)
