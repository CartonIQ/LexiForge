package com.cartoniq.lexiforge.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.cartoniq.lexiforge.ui.theme.Amber
import com.cartoniq.lexiforge.ui.theme.EasyBlue
import com.cartoniq.lexiforge.ui.theme.Emerald
import com.cartoniq.lexiforge.ui.theme.ErrorRed
import com.cartoniq.lexiforge.ui.theme.StreakFire
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    val x: Float,
    val delay: Float,
    val speed: Float,
    val sway: Float,
    val phase: Float,
    val w: Float,
    val h: Float,
    val color: Color,
    val spin: Float
)

@Composable
fun ConfettiOverlay(modifier: Modifier = Modifier, durationMs: Int = 3400) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        val rnd = Random(42)
        val palette = listOf(Emerald, Amber, EasyBlue, ErrorRed, StreakFire, Color.White)
        List(110) {
            Particle(
                x = rnd.nextFloat(),
                delay = rnd.nextFloat() * 0.35f,
                speed = 0.75f + rnd.nextFloat() * 0.5f,
                sway = 1f + rnd.nextFloat() * 2f,
                phase = rnd.nextFloat() * 6.28f,
                w = 6f + rnd.nextFloat() * 6f,
                h = 10f + rnd.nextFloat() * 8f,
                color = palette[rnd.nextInt(palette.size)],
                spin = (rnd.nextFloat() - 0.5f) * 4f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
    }

    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        val swayPx = 28.dp.toPx()
        particles.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local > 0f && local < 1f) {
                val x = p.x * size.width + sin(p.phase + local * 6.28f * p.sway) * swayPx
                val y = -30f + local * (size.height + 60f) * p.speed
                val alpha = if (local > 0.8f) (1f - (local - 0.8f) / 0.2f) else 1f
                rotate(degrees = p.spin * local * 360f, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.w / 2f, y - p.h / 2f),
                        size = Size(p.w, p.h)
                    )
                }
            }
        }
    }
}
