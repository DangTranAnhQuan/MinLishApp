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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Paint
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

    val maxData = data.maxOrNull()?.coerceAtLeast(10f) ?: 10f
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
                val yPadding = 16.dp.toPx()
                val availableHeight = height - yPadding * 2
                
                val points = data.mapIndexed { index, value ->
                    Offset(
                        x = index * stepX,
                        y = height - yPadding - ((value / maxData) * availableHeight)
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
                    
                    points.forEachIndexed { index, point ->
                        drawCircle(color = SoftWhite, radius = 5.dp.toPx(), center = point, alpha = currentAlpha)
                        drawCircle(color = VibrantBlue, radius = 5.dp.toPx(), center = point, style = dotStroke, alpha = currentAlpha)
                        
                        // Draw value above the point
                        val valueStr = data[index].toInt().toString()
                        val textPaint = Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 12.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            alpha = (currentAlpha * 255).toInt()
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            valueStr,
                            point.x,
                            point.y - 12.dp.toPx(),
                            textPaint
                        )
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
