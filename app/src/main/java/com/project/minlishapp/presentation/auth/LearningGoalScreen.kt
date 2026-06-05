package com.project.minlishapp.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
import com.project.minlishapp.R
import com.project.minlishapp.presentation.components.OnboardingTopBar
import com.project.minlishapp.presentation.components.PrimaryButton

@Composable
fun LearningGoalScreen(
    onNavigateNext: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        containerColor = Color.White,
        topBar = {
            OnboardingTopBar(
                progress = 0.5f,
                stepText = "1/2"
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
                        text = "Continue",
                        onClick = onNavigateNext,
                        isEnabled = uiState.learningTarget.isNotEmpty(),
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
            LearningGoalHeader()
            LearningGoalGrid(
                uiState = uiState,
                onGoalSelected = { viewModel.onLearningTargetChange(it) }
            )
        }
    }
}

@Composable
private fun LearningGoalHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 32.dp)
    ) {
        Text(
            text = "Why are you learning English?",
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
            text = "Choose a goal to help us personalize your learning experience.",
            color = Color(0xff64748b),
            textAlign = TextAlign.Center,
            lineHeight = 1.5.em,
            style = TextStyle(fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LearningGoalGrid(
    uiState: AuthUiState,
    onGoalSelected: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(uiState.learningTargets) { goal ->
            GoalCard(
                title = goal,
                iconRes = getIconForGoal(goal),
                isSelected = uiState.learningTarget == goal,
                onClick = { onGoalSelected(goal) }
            )
        }
    }
}

private fun getIconForGoal(goal: String): Int {
    return when (goal) {
        "IELTS" -> R.drawable.ic_ielts
        "TOEIC" -> R.drawable.ic_toeic
        "Communication" -> R.drawable.ic_chat
        "Career" -> R.drawable.ic_career
        else -> R.drawable.ic_ielts
    }
}

@Composable
private fun GoalCard(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xff1a73e8) else Color(0xffe2e8f0)
    val backgroundColor = if (isSelected) Color(0xfff0f7ff) else Color(0xfff8f9fa)
    val iconColor = if (isSelected) Color(0xff1a73e8) else Color(0xff64748b)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = Color(0xff1a73e8),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(20.dp)
                    .background(Color.White, RoundedCornerShape(99.dp))
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (isSelected) Color.White else Color(0xfff0f2f5)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    colorFilter = ColorFilter.tint(iconColor),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = Color(0xff1e293b),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )
        }
    }
}