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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.ui.theme.DarkText
import com.project.minlishapp.ui.theme.SoftWhite
import com.project.minlishapp.ui.theme.VibrantBlue

@Composable
fun RetentionLineChart(
    data: List<Float>, 
    modifier: Modifier = Modifier
) {
    Text(
        text = "Retention Rate",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkText,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
        )
    }

    val maxData = 100f // Percentage
    val gradientColorStart = VibrantBlue.copy(alpha = 0.2f)
    val gradientColorEnd = VibrantBlue.copy(alpha = 0.0f)

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .drawWithCache {
                if (data.isEmpty()) return@drawWithCache onDrawBehind {}

                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1).coerceAtLeast(1)
                
                val points = data.mapIndexed { index, value ->
                    Offset(
                        x = index * stepX,
                        y = height - ((value / maxData) * height)
                    )
                }

                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cx = (p1.x + p2.x) / 2
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                }
                
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                val shadowPath = Path().apply {
                    addPath(linePath)
                    translate(Offset(0f, 8f)) 
                }

                val fillBrush = Brush.verticalGradient(
                    colors = listOf(gradientColorStart, gradientColorEnd),
                    startY = 0f,
                    endY = height
                )

                val lineStroke = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val shadowStroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val dotStroke = Stroke(width = 2.5f.dp.toPx())

                onDrawBehind {
                    val currentAlpha = animationProgress.value

                    drawPath(path = fillPath, brush = fillBrush, style = Fill, alpha = currentAlpha)
                    drawPath(path = shadowPath, color = VibrantBlue.copy(alpha = 0.15f), style = shadowStroke, alpha = currentAlpha)
                    drawPath(path = linePath, color = VibrantBlue, style = lineStroke, alpha = currentAlpha)
                    
                    points.forEach { point ->
                        drawCircle(color = SoftWhite, radius = 5.dp.toPx(), center = point, alpha = currentAlpha)
                        drawCircle(color = VibrantBlue, radius = 5.dp.toPx(), center = point, style = dotStroke, alpha = currentAlpha)
                    }
                }
            }
    )
}
