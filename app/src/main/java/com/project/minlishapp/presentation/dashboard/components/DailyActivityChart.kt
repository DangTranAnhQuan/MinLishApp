package com.project.minlishapp.presentation.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.ui.theme.DarkText
import com.project.minlishapp.ui.theme.VeryLightGray
import com.project.minlishapp.ui.theme.VibrantBlue
import com.project.minlishapp.ui.theme.VibrantBlueLight

@Composable
fun DailyActivityChart(
    data: List<Float>, 
    modifier: Modifier = Modifier
) {
    Text(
        text = "Daily Activity",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
        )
    }

    val maxData = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .drawWithCache {
                if (data.isEmpty()) return@drawWithCache onDrawBehind {}

                val width = size.width
                val height = size.height
                val barWidth = width / (data.size * 2f)
                val spacing = barWidth
                val gridLines = 4
                val gridSpacing = height / gridLines
                
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val barBrush = Brush.verticalGradient(
                    colors = listOf(VibrantBlueLight, VibrantBlue),
                    startY = 0f, 
                    endY = height 
                )

                onDrawBehind {
                    for (i in 0..gridLines) {
                        drawLine(
                            color = VeryLightGray,
                            start = Offset(0f, height - (i * gridSpacing)),
                            end = Offset(width, height - (i * gridSpacing)),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashPathEffect
                        )
                    }

                    val currentProgress = animationProgress.value 
                    
                    data.forEachIndexed { index, value ->
                        val barHeight = (value / maxData) * height * currentProgress
                        val x = index * (barWidth + spacing) + spacing / 2
                        val y = height - barHeight

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )
                    }
                }
            }
    )
}

