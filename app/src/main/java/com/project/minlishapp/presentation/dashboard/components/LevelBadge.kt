package com.project.minlishapp.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.presentation.dashboard.Level
import com.project.minlishapp.ui.theme.GrayText
import com.project.minlishapp.ui.theme.VeryLightGray
import com.project.minlishapp.ui.theme.VibrantBlue

@Composable
fun LevelBadge(
    level: Level, 
    totalWordsLearned: Int, 
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Current Level",
                fontSize = 14.sp,
                color = GrayText,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = level.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantBlue,
                fontFamily = FontFamily.SansSerif
            )
        }
        
        Box(contentAlignment = Alignment.Center) {
            val progress = when (level) {
                Level.Beginner -> totalWordsLearned / 150f
                Level.Intermediate -> (totalWordsLearned - 150) / 450f
                Level.Advanced -> 1f
            }
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(72.dp),
                color = VibrantBlue,
                strokeWidth = 8.dp,
                trackColor = VeryLightGray,
            )
            Text("🏆", fontSize = 28.sp)
        }
    }
}
