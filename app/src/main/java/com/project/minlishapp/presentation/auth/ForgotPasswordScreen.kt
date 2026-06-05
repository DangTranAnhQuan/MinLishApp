package com.project.minlishapp.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.project.minlishapp.R
import com.project.minlishapp.presentation.components.AppTextField
import com.project.minlishapp.presentation.components.PrimaryButton

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(0.8f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "Reset Your Password",
                color = Color(0xff1e293b),
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your email address and we'll send you a link to reset your password",
                color = Color(0xff64748b),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth(0.9f),
                lineHeight = 1.43.em,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ForgotPasswordForm(
                    uiState = uiState,
                    onEmailChange = { viewModel.onResetPasswordEmailChange(it) }
                )

                Text(
                    text = "We'll send a password reset link to the provided email. Check your inbox and spam folder.",
                    color = Color(0xff64748b),
                    style = TextStyle(fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth()
                )

                ForgotPasswordActions(
                    uiState = uiState,
                    onSendClick = { viewModel.sendPasswordResetEmail() }
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
        Text(
            text = "← Back to Login",
            color = Color(0xff1a73e8),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .clickable { onNavigateBack() }
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
private fun ForgotPasswordForm(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppTextField(
            value = uiState.resetPasswordEmail,
            onValueChange = onEmailChange,
            placeholder = "Enter your email",
            leadingIconRes = R.drawable.ic_email,
            isError = uiState.resetPasswordEmailError != null,
            errorMessage = uiState.resetPasswordEmailError,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )
        )
    }
}

@Composable
private fun ForgotPasswordActions(
    uiState: AuthUiState,
    onSendClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.isResetPasswordSuccess) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xffd1fae5)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "✓ Reset link sent successfully! Check your email.",
                    color = Color(0xff065f46),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        uiState.errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xffffe0e0)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = it,
                    color = Color(0xffc41c3b),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val email = uiState.resetPasswordEmail.trim()
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        val isEmailValid = email.matches(emailRegex)

        PrimaryButton(
            text = "Send Reset Link",
            onClick = onSendClick,
            isEnabled = isEmailValid && !uiState.isResetPasswordLoading && !uiState.isResetPasswordSuccess,
            isLoading = uiState.isResetPasswordLoading
        )
    }
}

