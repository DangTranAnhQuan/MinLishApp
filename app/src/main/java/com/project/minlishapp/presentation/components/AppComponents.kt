package com.project.minlishapp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import com.project.minlishapp.R

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIconRes: Int? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(24.dp))
                .background(color = Color(0xfff8f9fa))
                .border(
                    border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error else Color(0xffe2e8f0)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            leadingIconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xff64748b)),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(16.dp)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xff1e293b)
                ),
                keyboardOptions = keyboardOptions,
                visualTransformation = if (visualTransformation is PasswordVisualTransformation) {
                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
                } else visualTransformation,
                singleLine = true,
                cursorBrush = SolidColor(Color(0xff1a73e8)),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color(0xff64748b).copy(alpha = 0.7f),
                                style = TextStyle(fontSize = 15.sp),
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 1.dp, bottom = 2.5.dp)
            )
            
            if (visualTransformation is PasswordVisualTransformation) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Color(0xff64748b)
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownField(
    value: String,
    placeholder: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    leadingIconRes: Int? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(24.dp))
                .background(color = Color(0xfff8f9fa))
                .border(
                    border = BorderStroke(1.dp, Color(0xffe2e8f0)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            leadingIconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(0xff64748b)),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(16.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 1.dp, bottom = 2.5.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xff64748b).copy(alpha = 0.7f),
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                } else {
                    Text(
                        text = value,
                        color = Color(0xff1e293b),
                        style = TextStyle(fontSize = 15.sp),
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = option,
                                style = TextStyle(fontSize = 15.sp, color = Color(0xff1e293b))
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    isEnabled: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xff1a73e8),
            disabledContainerColor = Color(0xff1a73e8).copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth().height(56.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 1.5.em,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun SocialLoginButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xffe2e8f0)),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    color = Color(0xff1e293b),
                    textAlign = TextAlign.Center,
                    lineHeight = 1.5.em,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingTopBar(
    progress: Float,
    stepText: String,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = stepText,
                    color = Color(0xff1a73e8),
                    textAlign = TextAlign.End,
                    lineHeight = 1.43.em,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(Color(0xfff8f9fa))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xff1a73e8))
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        windowInsets = WindowInsets(0),
        modifier = modifier
    )
}

// Previews
@Preview(showBackground = true)
@Composable
fun AppTextFieldPreview() {
    AppTextField(
        value = "minlish@gmail.com",
        onValueChange = {},
        placeholder = "Enter your email",
        leadingIconRes = R.drawable.ic_email
    )
}

@Preview(showBackground = true)
@Composable
fun AppDropdownFieldPreview() {
    AppDropdownField(
        value = "IELTS",
        placeholder = "Select Target",
        options = listOf("IELTS", "TOEIC"),
        onOptionSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PrimaryButtonPreview() {
    PrimaryButton(
        text = "Start Learning",
        onClick = {},
        isEnabled = true,
        isLoading = false
    )
}

@Preview(showBackground = true)
@Composable
fun SocialLoginButtonPreview() {
    SocialLoginButton(
        text = "Continue with Google",
        iconRes = R.drawable.ic_google,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun OnboardingTopBarPreview() {
    OnboardingTopBar(
        progress = 0.5f,
        stepText = "1/2"
    )
}
