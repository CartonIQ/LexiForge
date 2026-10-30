package com.cartoniq.lexiforge.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.lexiforge.ui.theme.Emerald

@Composable
fun Flashcard(
    front: String,
    back: String,
    example: String,
    flipped: Boolean,
    onFlip: () -> Unit,
    onSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "flipRotation"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
                scaleX = pressScale
                scaleY = pressScale
            }
            .glass(RoundedCornerShape(28.dp), borderAlpha = 0.22f)
            .clickable(interactionSource = interaction, indication = null, onClick = onFlip)
    ) {
        if (rotation <= 90f) {
            CardFace(
                label = "WORD",
                text = front,
                subText = null,
                isFront = true,
                onSpeak = onSpeak
            )
        } else {
            // Counter-rotate so the back face is not mirrored.
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                CardFace(
                    label = "DEFINITION",
                    text = back,
                    subText = example,
                    isFront = false,
                    onSpeak = onSpeak
                )
            }
        }
    }
}

@Composable
private fun CardFace(
    label: String,
    text: String,
    subText: String?,
    isFront: Boolean,
    onSpeak: () -> Unit
) {
    Box(Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.TopStart).padding(top = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Emerald,
            letterSpacing = 1.5.sp
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onSpeak),
            contentAlignment = Alignment.Center
        ) {
            SpeakerIcon(tint = MaterialTheme.colorScheme.onBackground)
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                style = if (isFront) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (!subText.isNullOrBlank()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (isFront) {
            Text(
                text = "Tap to flip",
                modifier = Modifier.align(Alignment.BottomCenter),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.45f)
            )
        }
    }
}
