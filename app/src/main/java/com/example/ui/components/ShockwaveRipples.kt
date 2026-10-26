package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RedAlert
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ShockwaveRipples(
    isTriggered: Boolean,
    modifier: Modifier = Modifier
) {
    val ripple1 = remember { Animatable(0f) }
    val ripple2 = remember { Animatable(0f) }
    val ripple3 = remember { Animatable(0f) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            launch {
                ripple1.snapTo(0f)
                ripple1.animateTo(1f, animationSpec = tween(700, easing = LinearEasing))
            }
            launch {
                delay(120)
                ripple2.snapTo(0f)
                ripple2.animateTo(1f, animationSpec = tween(700, easing = LinearEasing))
            }
            launch {
                delay(240)
                ripple3.snapTo(0f)
                ripple3.animateTo(1f, animationSpec = tween(700, easing = LinearEasing))
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension * 0.7f

        val ripples = listOf(
            ripple1.value to RedAlert,
            ripple2.value to AmberWarning,
            ripple3.value to CyanPrimary
        )

        ripples.forEach { (progress, color) ->
            if (progress in 0.01f..0.99f) {
                val radius = maxRadius * progress
                val alpha = (1f - progress).coerceIn(0f, 1f)
                drawCircle(
                    color = color.copy(alpha = alpha * 0.7f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = (4f * (1f - progress) + 1f).dp.toPx())
                )
            }
        }
    }
}
