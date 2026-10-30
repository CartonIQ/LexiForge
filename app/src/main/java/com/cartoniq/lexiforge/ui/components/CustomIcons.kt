package com.cartoniq.lexiforge.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.cartoniq.lexiforge.ui.theme.StreakFire

@Composable
fun FlameIcon(active: Boolean, modifier: Modifier = Modifier.size(28.dp)) {
    val transition = rememberInfiniteTransition(label = "flame")
    val flicker by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameFlicker"
    )
    val scaleY = if (active) flicker else 1f
    val outer = if (active) StreakFire else Color(0xFF475569)
    val inner = if (active) Color(0xFFFDE68A) else Color(0xFF64748B)

    Canvas(modifier) {
        val s = size.width / 24f
        scale(scaleX = 1f, scaleY = scaleY, pivot = Offset(size.width / 2f, size.height)) {
            val outerPath = Path().apply {
                moveTo(12f * s, 1.5f * s)
                cubicTo(13f * s, 5f * s, 19f * s, 8.5f * s, 19f * s, 14.5f * s)
                cubicTo(19f * s, 19f * s, 15.8f * s, 22.5f * s, 12f * s, 22.5f * s)
                cubicTo(8.2f * s, 22.5f * s, 5f * s, 19f * s, 5f * s, 14.5f * s)
                cubicTo(5f * s, 11.5f * s, 6.5f * s, 9.5f * s, 8f * s, 8f * s)
                cubicTo(8.3f * s, 10f * s, 9.2f * s, 11f * s, 10.2f * s, 11.2f * s)
                cubicTo(10f * s, 7f * s, 10.5f * s, 3.8f * s, 12f * s, 1.5f * s)
                close()
            }
            val innerPath = Path().apply {
                moveTo(12f * s, 12f * s)
                cubicTo(14f * s, 14f * s, 16f * s, 15.5f * s, 16f * s, 18f * s)
                cubicTo(16f * s, 20.2f * s, 14.2f * s, 21.8f * s, 12f * s, 21.8f * s)
                cubicTo(9.8f * s, 21.8f * s, 8f * s, 20.2f * s, 8f * s, 18f * s)
                cubicTo(8f * s, 16f * s, 10f * s, 14.5f * s, 12f * s, 12f * s)
                close()
            }
            drawPath(outerPath, outer)
            drawPath(innerPath, inner)
        }
    }
}

@Composable
fun SpeakerIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        val s = size.width / 24f
        val body = Path().apply {
            moveTo(3f * s, 9f * s)
            lineTo(7f * s, 9f * s)
            lineTo(12f * s, 4f * s)
            lineTo(12f * s, 20f * s)
            lineTo(7f * s, 15f * s)
            lineTo(3f * s, 15f * s)
            close()
        }
        drawPath(body, tint)
        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(8f * s, 8f * s),
            size = Size(8f * s, 8f * s),
            style = Stroke(width = 2f * s, cap = StrokeCap.Round)
        )
        drawArc(
            color = tint,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(4f * s, 4f * s),
            size = Size(16f * s, 16f * s),
            style = Stroke(width = 2f * s, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatsIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        val s = size.width / 24f
        val w = 4f * s
        fun bar(x: Float, top: Float) {
            drawRoundRect(
                color = tint,
                topLeft = Offset(x * s, top * s),
                size = Size(w, (21f - top) * s),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * s, 1.5f * s)
            )
        }
        bar(3.5f, 12f)
        bar(10f, 4f)
        bar(16.5f, 8f)
    }
}
