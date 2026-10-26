package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FieldStatus
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.GreenNormal
import com.example.ui.theme.RedAlert
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EmfGauge(
    magnitude: Float,
    threshold: Float,
    status: FieldStatus,
    maxRange: Float = 250f,
    modifier: Modifier = Modifier
) {
    val animatedMagnitude by animateFloatAsState(
        targetValue = magnitude,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "magnitude_anim"
    )

    val progressFraction = (animatedMagnitude / maxRange).coerceIn(0f, 1f)
    val thresholdFraction = (threshold / maxRange).coerceIn(0f, 1f)

    val activeColor by animateColorAsState(
        targetValue = when {
            status == FieldStatus.TRIGGERED -> RedAlert
            animatedMagnitude > threshold * 0.75f -> AmberWarning
            else -> CyanPrimary
        },
        animationSpec = tween(durationMillis = 200),
        label = "active_color_anim"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .testTag("emf_gauge_box"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val strokeWidth = 18.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )

            val startAngle = 135f
            val sweepAngle = 270f

            // Background Track
            drawArc(
                color = Color(0xFF1E2633),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic Active Progress Arc
            val currentSweep = sweepAngle * progressFraction
            if (currentSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to CyanPrimary,
                        0.4f to AmberWarning,
                        0.8f to RedAlert,
                        1.0f to RedAlert
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Threshold Marker tick
            val thresholdAngleRad = Math.toRadians((startAngle + sweepAngle * thresholdFraction).toDouble())
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerMarker = Offset(
                (center.x + (radius + strokeWidth * 0.7f) * cos(thresholdAngleRad)).toFloat(),
                (center.y + (radius + strokeWidth * 0.7f) * sin(thresholdAngleRad)).toFloat()
            )
            val innerMarker = Offset(
                (center.x + (radius - strokeWidth * 0.7f) * cos(thresholdAngleRad)).toFloat(),
                (center.y + (radius - strokeWidth * 0.7f) * sin(thresholdAngleRad)).toFloat()
            )

            drawLine(
                color = RedAlert,
                start = innerMarker,
                end = outerMarker,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center Readout Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format(Locale.US, "%.1f", animatedMagnitude),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = activeColor
                )
            )
            Text(
                text = "ميكروتسلا (µT)",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(activeColor.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status == FieldStatus.TRIGGERED) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "تحذير",
                        tint = RedAlert,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }
                Text(
                    text = status.titleAr,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = activeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
