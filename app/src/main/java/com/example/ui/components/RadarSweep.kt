package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.FieldStatus
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarSweep(
    magnitude: Float,
    threshold: Float,
    status: FieldStatus,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )

    val isAlert = status == FieldStatus.TRIGGERED
    val primaryRadarColor = if (isAlert) RedAlert else CyanPrimary

    Box(
        modifier = modifier
            .size(240.dp)
            .testTag("radar_sweep_box"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Concentric Radar Rings
            val ringSteps = listOf(0.3f, 0.6f, 0.85f, 1.0f)
            ringSteps.forEach { step ->
                drawCircle(
                    color = Color(0xFF1E2836),
                    radius = maxRadius * step,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Radar Crosshairs
            drawLine(
                color = Color(0xFF1E2836),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1E2836),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.5.dp.toPx()
            )

            // Alert Pulse when triggered
            if (isAlert) {
                drawCircle(
                    color = RedAlert.copy(alpha = (1f - pulseRadiusFraction) * 0.45f),
                    radius = maxRadius * pulseRadiusFraction,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Rotating Radar Scanner Beam
            val sweepRad = Math.toRadians(sweepAngle.toDouble())
            val beamEnd = Offset(
                (center.x + maxRadius * cos(sweepRad)).toFloat(),
                (center.y + maxRadius * sin(sweepRad)).toFloat()
            )

            // Fan trail gradient
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to Color.Transparent,
                    (sweepAngle / 360f - 0.12f).coerceAtLeast(0f) to Color.Transparent,
                    (sweepAngle / 360f) to primaryRadarColor.copy(alpha = 0.35f),
                    1.0f to Color.Transparent
                ),
                startAngle = sweepAngle - 50f,
                sweepAngle = 50f,
                useCenter = true,
                topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
            )

            // Leading Scanner Line
            drawLine(
                color = primaryRadarColor,
                start = center,
                end = beamEnd,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Target Blip based on field strength
            val blipDistanceFraction = (magnitude / (threshold * 1.5f)).coerceIn(0.15f, 0.85f)
            val blipAngleRad = Math.toRadians(45.0)
            val blipPos = Offset(
                (center.x + maxRadius * blipDistanceFraction * cos(blipAngleRad)).toFloat(),
                (center.y + maxRadius * blipDistanceFraction * sin(blipAngleRad)).toFloat()
            )

            // Glowing Blip
            drawCircle(
                color = primaryRadarColor.copy(alpha = 0.3f),
                radius = 12.dp.toPx(),
                center = blipPos
            )
            drawCircle(
                color = primaryRadarColor,
                radius = 5.dp.toPx(),
                center = blipPos
            )

            // Center Pin
            drawCircle(
                color = primaryRadarColor,
                radius = 4.dp.toPx(),
                center = center
            )
        }
    }
}
