package com.cartoniq.lexiforge.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.lexiforge.ads.AdUnits
import com.cartoniq.lexiforge.data.DeckWithDue
import com.cartoniq.lexiforge.ui.components.BannerAd
import com.cartoniq.lexiforge.ui.components.DailyGoalRing
import com.cartoniq.lexiforge.ui.components.DeckEditorDialog
import com.cartoniq.lexiforge.ui.components.FlameIcon
import com.cartoniq.lexiforge.ui.components.GlassCard
import com.cartoniq.lexiforge.ui.components.SectionTitle
import com.cartoniq.lexiforge.ui.components.StatTile
import com.cartoniq.lexiforge.ui.components.StatsIcon
import com.cartoniq.lexiforge.ui.theme.Emerald
import com.cartoniq.lexiforge.ui.theme.StreakFire
import com.cartoniq.lexiforge.util.AppConstants
import com.cartoniq.lexiforge.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onStartReview: () -> Unit,
    onStudyDeck: (Long) -> Unit,
    onOpenDeck: (Long) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewDeck by rememberSaveable { mutableStateOf(false) }
    val stats = state.stats

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LexiForge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    FlameIcon(active = stats.currentStreak > 0)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${stats.currentStreak}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stats.currentStreak > 0) StreakFire else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onOpenStats) {
                        StatsIcon(tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            }

            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv ${stats.level}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald
                        )
                        Spacer(Modifier.width(12.dp))
                        LinearProgressIndicator(
                            progress = { stats.xp / AppConstants.XP_PER_LEVEL.toFloat() },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Emerald,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${stats.xp}/${AppConstants.XP_PER_LEVEL} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (stats.streakFreezes > 0) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Streak freezes: ${stats.streakFreezes}  (protects a missed day)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DailyGoalRing(
                        reviewed = stats.cardsReviewedToday,
                        goal = stats.dailyGoal,
                        modifier = Modifier.size(210.dp)
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Cards Due Today", "${state.dueToday}", Modifier.weight(1f), Emerald)
                    StatTile("Total Decks", "${state.decks.size}", Modifier.weight(1f))
                    StatTile("Best Streak", "${stats.longestStreak}", Modifier.weight(1f), StreakFire)
                }
            }

            item {
                Button(
                    onClick = onStartReview,
                    enabled = state.dueToday > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = if (state.dueToday > 0) "Start Review" else "All caught up!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Your Decks", Modifier.weight(1f))
                    TextButton(onClick = { showNewDeck = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Deck")
                    }
                }
            }

            if (state.loaded && state.decks.isEmpty()) {
                item {
                    Text(
                        text = "No decks yet. Create one and start adding words!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }
            }

            items(state.decks, key = { it.id }) { deck ->
                DeckRow(
                    deck = deck,
                    onOpen = { onOpenDeck(deck.id) },
                    onStudy = { onStudyDeck(deck.id) }
                )
            }
        }

        BannerAd(unitId = AdUnits.BANNER)
    }

    if (showNewDeck) {
        DeckEditorDialog(
            title = "New Deck",
            confirmLabel = "Create",
            onDismiss = { showNewDeck = false },
            onConfirm = { name, description, language ->
                showNewDeck = false
                viewModel.createDeck(name, description, language) { id -> onOpenDeck(id) }
            }
        )
    }
}

@Composable
private fun DeckRow(deck: DeckWithDue, onOpen: () -> Unit, onStudy: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = deck.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (deck.description.isNotBlank()) {
                    Text(
                        text = deck.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${deck.cardCount} cards  •  ${deck.dueCount} due",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (deck.dueCount > 0) Emerald else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (deck.dueCount > 0) {
                FilledTonalButton(
                    onClick = onStudy,
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Study")
                }
            }
        }
    }
}
