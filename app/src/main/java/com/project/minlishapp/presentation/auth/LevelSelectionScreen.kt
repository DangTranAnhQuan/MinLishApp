package com.project.minlishapp.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.minlishapp.presentation.components.OnboardingTopBar
import com.project.minlishapp.presentation.components.PrimaryButton

@Composable
fun LevelSelectionScreen(
    onNavigateNext: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isProfileSetupSuccess) {
        if (uiState.isProfileSetupSuccess) {
            onNavigateNext()
            viewModel.resetProfileSetupSuccess()
        }
    }
//Start
//    LevelSelectionContent(
//        uiState = uiState,
//        onLevelSelected = viewModel::onCurrentLevelChange,
//        onStartLearning = viewModel::completeProfileSetup,
//        modifier = modifier
//    )
//}
//
//@Composable
//fun LevelSelectionContent(
//    uiState: AuthUiState,
//    onLevelSelected: (String) -> Unit,
//    onStartLearning: () -> Unit,
//    modifier: Modifier = Modifier
//) {
    //End
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        containerColor = Color.White,
        topBar = {
            OnboardingTopBar(
                progress = 1.0f,
                stepText = "2/2"
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    PrimaryButton(
                        text = "Start Learning",
                        onClick = {
                            viewModel.completeProfileSetup()
//                            onNavigateNext()
                        },
//                        onClick = onStartLearning,
                        isEnabled = uiState.currentLevel.isNotEmpty(),
                        isLoading = false
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            LevelSelectionHeader()
            LevelSelectionList(
                uiState = uiState,
                onLevelSelected = { viewModel.onCurrentLevelChange(it) }
//                onLevelSelected = onLevelSelected
            )
        }
    }
}

@Composable
private fun LevelSelectionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Text(
            text = "What is your current level?",
            color = Color(0xff1e293b),
            textAlign = TextAlign.Center,
            lineHeight = 1.33.em,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Don't worry, you can change this anytime in your settings.",
            color = Color(0xff64748b),
            textAlign = TextAlign.Center,
            lineHeight = 1.5.em,
            style = TextStyle(fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LevelSelectionList(
    uiState: AuthUiState,
    onLevelSelected: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(uiState.levels) { levelInfo ->
            LevelCard(
                code = levelInfo.code,
                title = levelInfo.title,
                description = levelInfo.desc,
                isSelected = uiState.currentLevel == levelInfo.code,
                onClick = { onLevelSelected(levelInfo.code) }
            )
        }
    }
}

@Composable
private fun LevelCard(
    code: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xff1a73e8) else Color(0xffe2e8f0)
    val backgroundColor = if (isSelected) Color(0xfff0f7ff) else Color(0xfff8f9fa)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xff1a73e8) else Color(0xffe2e8f0)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = code,
                color = if (isSelected) Color.White else Color(0xff64748b),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color(0xff1e293b),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xff64748b),
                style = TextStyle(
                    fontSize = 14.sp
                )
            )
        }
        
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(99.dp))
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xffe2e8f0),
                    shape = RoundedCornerShape(99.dp)
                )
                .background(if (isSelected) Color(0xff1a73e8) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
//Start
//@Preview(showBackground = true)
//@Composable
//fun LevelSelectionScreenPreview() {
//    LevelSelectionContent(
//        uiState = AuthUiState(currentLevel = "B1"),
//        onLevelSelected = {},
//        onStartLearning = {}
//    )
//}
//End