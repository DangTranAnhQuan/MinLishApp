package com.project.minlishapp.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.project.minlishapp.R
import com.project.minlishapp.presentation.components.AppTextField
import com.project.minlishapp.presentation.components.PrimaryButton
import com.project.minlishapp.presentation.components.SocialLoginButton

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToProfileSetup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isAuthenticated, uiState.isProfileComplete, uiState.isCheckingAuth) {
        if (uiState.isAuthenticated && !uiState.isCheckingAuth) {
            if (uiState.isProfileComplete == true) {
                onLoginSuccess()
            } else if (uiState.isProfileComplete == false) {
                onNavigateToProfileSetup()
            }
        }
    }
    // Start
//
//    LoginContent(
//        uiState = uiState,
//        onEmailChange = viewModel::onEmailChange,
//        onPasswordChange = viewModel::onPasswordChange,
//        onLoginClick = viewModel::login,
//        onNavigateToRegister = onNavigateToRegister,
//        onGoogleLoginClick = viewModel::loginWithCredential,
//        onShowError = viewModel::showError,
//        modifier = modifier
//    )
//}
//
//@Composable
//fun LoginContent(
//    uiState: AuthUiState,
//    onEmailChange: (String) -> Unit,
//    onPasswordChange: (String) -> Unit,
//    onLoginClick: () -> Unit,
//    onNavigateToRegister: () -> Unit,
//    onGoogleLoginClick: (com.google.firebase.auth.AuthCredential) -> Unit,
//    onShowError: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//End
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(2f))
        LoginHeader()
        Spacer(modifier = Modifier.weight(1f))
        LoginForm(
            uiState = uiState,
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) }
//            onEmailChange = onEmailChange,
//            onPasswordChange = onPasswordChange
        )
        Spacer(modifier = Modifier.weight(1f))
        LoginActions(
            uiState = uiState,
            onLoginClick = { viewModel.login() }
//            onLoginClick = onLoginClick
        )
        Spacer(modifier = Modifier.weight(0.8f))
        SocialSection(
            context = context,
            coroutineScope = coroutineScope,
            onGoogleLoginClick = { credential -> viewModel.loginWithCredential(credential) },
            onShowError = { error -> viewModel.showError(error) }
//            onGoogleLoginClick = onGoogleLoginClick,
//            onShowError = onShowError
        )
        Spacer(modifier = Modifier.weight(0.8f))
        LoginFooter(
            onNavigateToRegister = onNavigateToRegister
        )
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun LoginHeader() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .height(72.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .size(64.dp)
                    .clip(shape = RoundedCornerShape(24.dp))
                    .background(color = Color(0xff1a73e8))
                    .shadow(elevation = 14.dp, shape = RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_minlish_logo),
                    contentDescription = "Logo",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MinLish",
                    color = Color(0xff1e293b),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.2.em,
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.75).sp
                    ),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Welcome back! Please enter your details to sign in.",
                    color = Color(0xff64748b),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = "Email",
                    color = Color(0xff1e293b),
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
            AppTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = "Enter your email",
                leadingIconRes = R.drawable.ic_email,
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = "Password",
                    color = Color(0xff1e293b),
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
            AppTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = "Enter your password",
                leadingIconRes = R.drawable.ic_password,
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Forgot Password?",
                    color = Color(0xff1a73e8),
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
        
        uiState.errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LoginActions(
    uiState: AuthUiState,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        PrimaryButton(
            text = "Login",
            onClick = onLoginClick,
            isEnabled = !uiState.isLoading && uiState.isEmailValid && uiState.isPasswordValid && uiState.email.isNotEmpty() && uiState.password.isNotEmpty(),
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun SocialSection(
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    onGoogleLoginClick: (com.google.firebase.auth.AuthCredential) -> Unit,
    onShowError: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            HorizontalDivider(
                color = Color(0xffe2e8f0),
                modifier = Modifier.weight(0.5f)
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "OR CONTINUE WITH",
                    color = Color(0xff64748b),
                    lineHeight = 1.33.em,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp
                    ),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
            HorizontalDivider(
                color = Color(0xffe2e8f0),
                modifier = Modifier.weight(0.5f)
            )
        }
        SocialLoginButton(
            text = "Continue with Google",
            iconRes = R.drawable.ic_google,
            onClick = {
                coroutineScope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("175372711746-skgdl6dng1l8hmcsjq14q9hg7lfu7alr.apps.googleusercontent.com")
                            .setAutoSelectEnabled(false)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = credentialManager.getCredential(
                            context = context,
                            request = request
                        )
                        val credential = result.credential
                        if (credential is CustomCredential) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            onGoogleLoginClick(firebaseCredential)
                        }
                    } catch (e: Exception) {
                        onShowError(e.localizedMessage ?: "Google Sign-In failed")
                    }
                }
            }
        )
    }
}

@Composable
private fun LoginFooter(onNavigateToRegister: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color(0xff64748b),
                textAlign = TextAlign.Center,
                lineHeight = 1.43.em,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
            )
            Text(
                text = "Sign Up",
                color = Color(0xff1a73e8),
                textAlign = TextAlign.Center,
                lineHeight = 1.43.em,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .clickable { onNavigateToRegister() }
            )
        }
    }
}
//Start
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun LoginScreenEmptyPreview() {
//    LoginContent(
//        uiState = AuthUiState(),
//        onEmailChange = {},
//        onPasswordChange = {},
//        onLoginClick = {},
//        onNavigateToRegister = {},
//        onGoogleLoginClick = {},
//        onShowError = {}
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun LoginScreenLoadingPreview() {
//    LoginContent(
//        uiState = AuthUiState(isLoading = true, email = "test@example.com", password = "password"),
//        onEmailChange = {},
//        onPasswordChange = {},
//        onLoginClick = {},
//        onNavigateToRegister = {},
//        onGoogleLoginClick = {},
//        onShowError = {}
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun LoginScreenErrorPreview() {
//    LoginContent(
//        uiState = AuthUiState(errorMessage = "Invalid email or password"),
//        onEmailChange = {},
//        onPasswordChange = {},
//        onLoginClick = {},
//        onNavigateToRegister = {},
//        onGoogleLoginClick = {},
//        onShowError = {}
//    )
//}
//End