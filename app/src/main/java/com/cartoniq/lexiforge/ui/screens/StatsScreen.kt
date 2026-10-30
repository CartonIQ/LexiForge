package com.cartoniq.lexiforge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.lexiforge.ui.components.GlassCard
import com.cartoniq.lexiforge.ui.components.ScreenHeader
import com.cartoniq.lexiforge.ui.components.SectionTitle
import com.cartoniq.lexiforge.ui.components.StatTile
import com.cartoniq.lexiforge.ui.theme.Amber
import com.cartoniq.lexiforge.ui.theme.EasyBlue
import com.cartoniq.lexiforge.ui.theme.Emerald
import com.cartoniq.lexiforge.ui.theme.StreakFire
import com.cartoniq.lexiforge.viewmodel.StatsViewModel

@Composable
fun StatsScreen(onBack: () -> Unit, viewModel: StatsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = state.stats

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Statistics", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Total Reviews", "${state.totalReviews}", Modifier.weight(1f))
                StatTile("Retention", "${state.retentionPercent}%", Modifier.weight(1f), Emerald)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Current Streak", "${stats.currentStreak}", Modifier.weight(1f), StreakFire)
                StatTile("Best Streak", "${stats.longestStreak}", Modifier.weight(1f), StreakFire)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Level", "${stats.level}", Modifier.weight(1f), Amber)
                StatTile("Total XP", "${stats.totalXp}", Modifier.weight(1f), Amber)
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Last 7 days")
                Spacer(Modifier.height(12.dp))
                WeekChart(state.last7Days)
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Card maturity")
                Spacer(Modifier.height(12.dp))
                MaturityBar(state.newCards, state.youngCards, state.matureCards)
                Spacer(Modifier.height(12.dp))
                LegendRow("New", state.newCards, EasyBlue)
                LegendRow("Learning (under 21 days)", state.youngCards, Amber)
                LegendRow("Mature (21+ days)", state.matureCards, Emerald)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeekChart(data: List<Pair<String, Int>>) {
    val max = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { entry ->
            val label = entry.first
            val value = entry.second
            val barHeight = maxOf(4f, value.toFloat() / max * 120f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    "$value",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(barHeight.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (value > 0) Emerald else Color.White.copy(alpha = 0.10f))
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MaturityBar(newCards: Int, young: Int, mature: Int) {
    val total = newCards + young + mature
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        if (total > 0) {
            if (newCards > 0) Box(Modifier.weight(newCards.toFloat()).fillMaxHeight().background(EasyBlue))
            if (young > 0) Box(Modifier.weight(young.toFloat()).fillMaxHeight().background(Amber))
            if (mature > 0) Box(Modifier.weight(mature.toFloat()).fillMaxHeight().background(Emerald))
        }
    }
}

@Composable
private fun LegendRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
