package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import java.util.Locale

@Composable
fun WaveformChart(
    points: List<Float>,
    threshold: Float,
    peak: Float,
    min: Float,
    avg: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(18.dp)
            .testTag("waveform_chart_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مخطط التغير الحي (آخر 60 قراءة)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "الحد: ${threshold.toInt()} µT",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = RedAlert,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F151E))
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.isEmpty()) return@Canvas

                val width = size.width
                val height = size.height

                val maxVal = maxOf(threshold * 1.3f, points.maxOrNull() ?: 100f, 100f)
                val minVal = 0f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                // Draw Horizontal Grid Lines
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = height * (i.toFloat() / gridSteps)
                    drawLine(
                        color = Color(0xFF1B2432),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Threshold line
                val thresholdY = height - ((threshold - minVal) / range * height)
                if (thresholdY in 0f..height) {
                    drawLine(
                        color = RedAlert.copy(alpha = 0.8f),
                        start = Offset(0f, thresholdY),
                        end = Offset(width, thresholdY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // Plot Waveform Path
                val stepX = if (points.size > 1) width / (points.size - 1) else width
                val linePath = Path()
                val fillPath = Path()

                points.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = height - ((value - minVal) / range * height).coerceIn(0f, height)

                    if (index == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw Gradient Fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CyanPrimary.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Draw Smooth Line
                drawPath(
                    path = linePath,
                    color = CyanPrimary,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw current point glow dot
                if (points.isNotEmpty()) {
                    val lastX = (points.size - 1) * stepX
                    val lastY = height - ((points.last() - minVal) / range * height).coerceIn(0f, height)
                    drawCircle(
                        color = CyanPrimary.copy(alpha = 0.4f),
                        radius = 8.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = CyanPrimary,
                        radius = 4.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Stats summary chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "الذروة (أعلى قراءة)",
                value = String.format(Locale.US, "%.1f", peak),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "المعدل المتوسط",
                value = String.format(Locale.US, "%.1f", avg),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "أدنى قراءة",
                value = String.format(Locale.US, "%.1f", min),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131A24))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value µT",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
