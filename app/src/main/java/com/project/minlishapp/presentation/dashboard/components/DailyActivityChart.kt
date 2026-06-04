package com.project.minlishapp.presentation.dashboard.components

import android.graphics.Paint
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.ui.theme.DarkText
import com.project.minlishapp.ui.theme.VeryLightGray
import com.project.minlishapp.ui.theme.VibrantBlue
import com.project.minlishapp.ui.theme.VibrantBlueLight
import androidx.compose.ui.graphics.Color

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
        animationProgress.snapTo(0f)
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
                val labelSpace = 18.dp.toPx()
                val chartHeight = height - labelSpace
                val barWidth = width / (data.size * 2f)
                val spacing = barWidth
                val gridLines = 4
                val gridSpacing = chartHeight / gridLines

                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                val barBrush = Brush.verticalGradient(
                    colors = listOf(VibrantBlueLight, VibrantBlue),
                    startY = 0f,
                    endY = height
                )

                val todayBarBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFB74D), Color(0xFFFF9800)),
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
                        val barHeight = (value / maxData) * chartHeight * currentProgress
                        val x = index * (barWidth + spacing) + spacing / 2
                        val y = height - barHeight

                        drawRoundRect(
                            brush = if (index == data.size - 1) todayBarBrush else barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                        )

                        if (value > 0f) {
                            val textPaint = Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 12.sp.toPx()
                                textAlign = Paint.Align.CENTER
                                alpha = (currentProgress * 255).toInt()
                            }
                            drawContext.canvas.nativeCanvas.drawText(
                                value.toInt().toString(),
                                x + barWidth / 2,
                                (y - 4.dp.toPx()).coerceAtLeast(12.sp.toPx()),
                                textPaint
                            )
                        }
                    }
                }
            }
    )

    val daysOfWeek = remember {
        val format = java.text.SimpleDateFormat("EEE\ndd", java.util.Locale.getDefault())
        (6 downTo 0).map { i ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            format.format(cal.time)
        }
    }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            Text(
                text = day,
                fontSize = 12.sp,
                color = if (index == daysOfWeek.size - 1) Color(0xFFFF9800) else com.project.minlishapp.ui.theme.GrayText,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}
