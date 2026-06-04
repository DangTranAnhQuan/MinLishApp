package com.project.minlishapp.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.presentation.dashboard.DashboardUiState
import com.project.minlishapp.ui.theme.DarkText
import com.project.minlishapp.ui.theme.GrayText
import com.project.minlishapp.ui.theme.LightGraySurface

@Composable
fun DashboardCards(
    uiState: DashboardUiState, 
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Words",
            value = uiState.totalWordsLearned.toString(),
            icon = Icons.Default.Book,
            iconColor = Color(0xFF9C27B0) // Purple
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "${uiState.currentStreak} 🔥",
            icon = Icons.Default.LocalFireDepartment,
            iconColor = Color(0xFFFF9800) // Orange
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Accuracy",
            value = String.format("%.2f%%", uiState.accuracy),
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF4CAF50) // Green
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.LightGray,
                spotColor = Color.LightGray
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGraySurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = GrayText,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
