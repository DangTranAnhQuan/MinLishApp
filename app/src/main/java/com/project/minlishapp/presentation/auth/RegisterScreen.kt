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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
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
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.isRegisterSuccess) {
        if (uiState.isRegisterSuccess) {
            onRegisterSuccess()
            viewModel.resetRegisterSuccess()
        }
    }
//Start
//    RegisterContent(
//        uiState = uiState,
//        onDisplayNameChange = viewModel::onDisplayNameChange,
//        onEmailChange = viewModel::onEmailChange,
//        onPasswordChange = viewModel::onPasswordChange,
//        onSignUpClick = viewModel::signUp,
//        onNavigateToLogin = onNavigateToLogin,
//        onGoogleLoginClick = viewModel::loginWithCredential,
//        onShowError = viewModel::showError,
//        modifier = modifier
//    )
//}
//
//@Composable
//fun RegisterContent(
//    uiState: AuthUiState,
//    onDisplayNameChange: (String) -> Unit,
//    onEmailChange: (String) -> Unit,
//    onPasswordChange: (String) -> Unit,
//    onSignUpClick: () -> Unit,
//    onNavigateToLogin: () -> Unit,
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
        RegisterHeader()
        Spacer(modifier = Modifier.weight(1f))
        RegisterForm(
            uiState = uiState,
            onDisplayNameChange = { viewModel.onDisplayNameChange(it) },
            onEmailChange = { viewModel.onEmailChange(it) },
            onPasswordChange = { viewModel.onPasswordChange(it) }
//            onDisplayNameChange = onDisplayNameChange,
//            onEmailChange = onEmailChange,
//            onPasswordChange = onPasswordChange
        )
        Spacer(modifier = Modifier.weight(1f))
        RegisterActions(
            uiState = uiState,
            onSignUpClick = { viewModel.signUp() }
//            onSignUpClick = onSignUpClick
        )
        Spacer(modifier = Modifier.weight(0.8f))
        RegisterSocialSection(
            context = context,
            coroutineScope = coroutineScope,
            onGoogleLoginClick = { credential -> viewModel.loginWithCredential(credential) },
            onShowError = { error -> viewModel.showError(error) }
//            onGoogleLoginClick = onGoogleLoginClick,
//            onShowError = onShowError
        )
        Spacer(modifier = Modifier.weight(0.8f))
        RegisterFooter(
            onNavigateToLogin = onNavigateToLogin
        )
        Spacer(modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun RegisterHeader() {
    Column(
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
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Top),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Create Account",
                    color = Color(0xff1e293b),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.33.em,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Start your journey with us today.",
                    color = Color(0xff64748b),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.5.em,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun RegisterForm(
    uiState: AuthUiState,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Full Name
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Full Name",
                    color = Color(0xff1e293b),
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )
                AppTextField(
                    value = uiState.displayName,
                    onValueChange = onDisplayNameChange,
                    placeholder = "Enter your full name",
                    leadingIconRes = R.drawable.ic_fullname,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            }

            // Email
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier.fillMaxWidth()
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
                        .padding(start = 4.dp)
                )
                AppTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = "Enter your email",
                    leadingIconRes = R.drawable.ic_email,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
            
            // Password
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                modifier = Modifier.fillMaxWidth()
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
                        .padding(start = 4.dp)
                )
                AppTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = "Create a password",
                    leadingIconRes = R.drawable.ic_password,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Text(
                    text = "Must be at least 8 characters long.",
                    color = Color(0xff64748b),
                    lineHeight = 1.5.em,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                )
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
}

@Composable
private fun RegisterActions(
    uiState: AuthUiState,
    onSignUpClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        PrimaryButton(
            text = "Sign Up",
            onClick = onSignUpClick,
            isEnabled = !uiState.isLoading && uiState.isEmailValid && uiState.isPasswordValid && uiState.email.isNotEmpty() && uiState.password.isNotEmpty() && uiState.displayName.trim().isNotEmpty(),
            isLoading = uiState.isLoading
        )
    }
}

@Composable
private fun RegisterSocialSection(
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
                    text = "OR JOIN WITH",
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
private fun RegisterFooter(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account?",
                color = Color(0xff64748b),
                textAlign = TextAlign.Center,
                lineHeight = 1.5.em,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
            )
            Text(
                text = "Login",
                color = Color(0xff1a73e8),
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                lineHeight = 1.5.em,
                style = TextStyle(fontSize = 15.sp),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .clickable { onNavigateToLogin() }
            )
        }
    }
}
//Start
//@Preview(showBackground = true)
//@Composable
//fun RegisterScreenEmptyPreview() {
//    RegisterContent(
//        uiState = AuthUiState(),
//        onDisplayNameChange = {},
//        onEmailChange = {},
//        onPasswordChange = {},
//        onSignUpClick = {},
//        onNavigateToLogin = {},
//        onGoogleLoginClick = {},
//        onShowError = {}
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun RegisterScreenLoadingPreview() {
//    RegisterContent(
//        uiState = AuthUiState(isLoading = true, email = "test@example.com", password = "password"),
//        onDisplayNameChange = {},
//        onEmailChange = {},
//        onPasswordChange = {},
//        onSignUpClick = {},
//        onNavigateToLogin = {},
//        onGoogleLoginClick = {},
//        onShowError = {}
//    )
//}
//End