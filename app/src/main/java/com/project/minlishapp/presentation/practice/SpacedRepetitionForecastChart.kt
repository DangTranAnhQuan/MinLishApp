package com.project.minlishapp.presentation.practice

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.minlishapp.domain.usecase.srs.ReviewForecastBucket

private val ForecastColors = listOf(
    Color(0xffef5350),
    Color(0xffffc107),
    Color(0xff4fc3f7),
    Color(0xff2f80ed),
    Color(0xff29439b)
)

@Composable
fun SpacedRepetitionForecastChart(
    buckets: List<ReviewForecastBucket>,
    modifier: Modifier = Modifier
) {
    val maxCount = buckets.maxOfOrNull(ReviewForecastBucket::count)?.coerceAtLeast(1) ?: 1
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        buckets.forEachIndexed { index, bucket ->
            val barHeight = (94f * bucket.count / maxCount).coerceAtLeast(6f).dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "${bucket.count} từ",
                    color = Color(0xff374151),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .width(38.dp)
                        .height(barHeight)
                ) {
                    drawRoundRect(
                        color = ForecastColors[index % ForecastColors.size],
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height + 12.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = bucket.label,
                    color = Color(0xff374151),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
