package com.cartoniq.lexiforge.ui.screens

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.lexiforge.ads.AdManager
import com.cartoniq.lexiforge.domain.SM2Algorithm
import com.cartoniq.lexiforge.ui.components.ConfettiOverlay
import com.cartoniq.lexiforge.ui.components.Flashcard
import com.cartoniq.lexiforge.ui.components.StatTile
import com.cartoniq.lexiforge.ui.theme.Amber
import com.cartoniq.lexiforge.ui.theme.EasyBlue
import com.cartoniq.lexiforge.ui.theme.Emerald
import com.cartoniq.lexiforge.ui.theme.ErrorRed
import com.cartoniq.lexiforge.ui.theme.MidnightBackground
import com.cartoniq.lexiforge.util.findActivity
import com.cartoniq.lexiforge.viewmodel.StudyUiState
import com.cartoniq.lexiforge.viewmodel.StudyViewModel

@Composable
fun StudyScreen(
    adManager: AdManager,
    onExit: () -> Unit,
    viewModel: StudyViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var interstitialShown by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.complete, state.levelUpTo) {
        if (state.complete && state.levelUpTo == null && !interstitialShown) {
            interstitialShown = true
            val host = activity
            if (host != null) adManager.showInterstitial(host) { }
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
            state.totalCards == 0 -> EmptyStudy(onExit)
            state.complete -> SessionComplete(
                state = state,
                adManager = adManager,
                activity = activity,
                onClaimBonus = viewModel::claimBonusXp,
                onDone = onExit
            )
            else -> StudyContent(
                state = state,
                hapticsEnabled = viewModel.hapticsEnabled,
                onSpeak = viewModel::speakCurrent,
                onRate = viewModel::rate,
                onExit = onExit
            )
        }

        val levelUp = state.levelUpTo
        if (levelUp != null) {
            LevelUpOverlay(level = levelUp, onContinue = viewModel::dismissLevelUp)
        }
    }
}

@Composable
private fun EmptyStudy(onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nothing due right now", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Come back later or add more cards.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onExit) { Text("Back") }
    }
}

@Composable
private fun StudyContent(
    state: StudyUiState,
    hapticsEnabled: Boolean,
    onSpeak: () -> Unit,
    onRate: (Int) -> Unit,
    onExit: () -> Unit
) {
    val item = state.current ?: return
    val haptic = LocalHapticFeedback.current
    var flipped by rememberSaveable(item.token) { mutableStateOf(false) }
    var revealed by rememberSaveable(item.token) { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(targetValue = state.progress, label = "sessionProgress")

    val doFlip: () -> Unit = {
        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        flipped = !flipped
        revealed = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "Exit session")
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Emerald,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${state.doneCount}/${state.totalCards}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = if (item.isRetry) "Try this one again" else " ",
            style = MaterialTheme.typography.labelLarge,
            color = Amber,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))

        key(item.token) {
            Flashcard(
                front = item.card.front,
                back = item.card.back,
                example = item.card.example,
                flipped = flipped,
                onFlip = doFlip,
                onSpeak = onSpeak,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        if (revealed) {
            RatingRow(previews = state.previews, onRate = onRate)
        } else {
            Button(
                onClick = doFlip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Show Answer", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private class RatingSpec(
    val label: String,
    val quality: Int,
    val container: Color,
    val content: Color
)

@Composable
private fun RatingRow(previews: List<String>, onRate: (Int) -> Unit) {
    val specs = listOf(
        RatingSpec("Again", SM2Algorithm.AGAIN, ErrorRed, Color.White),
        RatingSpec("Hard", SM2Algorithm.HARD, Amber, MidnightBackground),
        RatingSpec("Good", SM2Algorithm.GOOD, Emerald, MidnightBackground),
        RatingSpec("Easy", SM2Algorithm.EASY, EasyBlue, Color.White)
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        specs.forEachIndexed { index, spec ->
            Button(
                onClick = { onRate(spec.quality) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = spec.container,
                    contentColor = spec.content
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(spec.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val preview = previews.getOrNull(index)
                    if (preview != null) {
                        Text(preview, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionComplete(
    state: StudyUiState,
    adManager: AdManager,
    activity: Activity?,
    onClaimBonus: () -> Unit,
    onDone: () -> Unit
) {
    val accuracy = if (state.reviewedCount == 0) 0 else (state.correctCount * 100) / state.reviewedCount

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Session Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "You earned ${state.sessionXp} XP!",
                style = MaterialTheme.typography.titleLarge,
                color = Emerald
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Cards", "${state.reviewedCount}", Modifier.weight(1f))
                StatTile("Accuracy", "$accuracy%", Modifier.weight(1f))
                StatTile("Streak", "${state.streak}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(28.dp))

            if (state.bonusClaimed) {
                Text(
                    "Bonus XP claimed - doubled!",
                    style = MaterialTheme.typography.labelLarge,
                    color = Amber
                )
                Spacer(Modifier.height(16.dp))
            } else if (state.sessionXp > 0 && adManager.rewardedReady && activity != null) {
                FilledTonalButton(
                    onClick = {
                        adManager.showRewarded(activity, onReward = onClaimBonus, onClosed = { })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Watch ad to double XP (+${state.sessionXp})")
                }
                Spacer(Modifier.height(12.dp))
            }

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Done", style = MaterialTheme.typography.titleMedium)
            }
        }
        ConfettiOverlay(Modifier.fillMaxSize())
    }
}

@Composable
private fun LevelUpOverlay(level: Int, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.78f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            ),
        contentAlignment = Alignment.Center
    ) {
        key(level) { ConfettiOverlay(Modifier.fillMaxSize()) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                "LEVEL UP!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Amber
            )
            Spacer(Modifier.height(8.dp))
            Text("You reached level $level", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "+1 streak freeze earned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))
            OutlinedButton(onClick = onContinue, shape = RoundedCornerShape(16.dp)) {
                Text("Continue")
            }
        }
    }
}
