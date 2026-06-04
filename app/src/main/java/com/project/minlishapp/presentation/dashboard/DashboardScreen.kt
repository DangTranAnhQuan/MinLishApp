package com.project.minlishapp.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.project.minlishapp.presentation.dashboard.components.*

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardContent(uiState = uiState, modifier = modifier)
}

@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(20.dp)
            .verticalScroll(scrollState)
    ) {
        LevelBadge(level = uiState.level, totalWordsLearned = uiState.totalWordsLearned)
        
        Spacer(modifier = Modifier.height(28.dp))
        DashboardCards(uiState = uiState)
        
        Spacer(modifier = Modifier.height(32.dp))
        // Daily Activity Chart
        DailyActivityChart(data = uiState.dailyActivityData)
        
        Spacer(modifier = Modifier.height(32.dp))
        // Retention Rate Chart
        RetentionLineChart(data = uiState.retentionData)
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val sampleState = DashboardUiState(
        totalWordsLearned = 350,
        currentStreak = 15,
        accuracy = 92.5f,
        dailyActivityData = listOf(10f, 25f, 15f, 40f, 30f, 50f, 20f),
        retentionData = listOf(90f, 85f, 80f, 82f, 78f, 75f, 88f)
    )
    com.project.minlishapp.ui.theme.MinLishAppTheme {
        DashboardContent(uiState = sampleState)
    }
}
