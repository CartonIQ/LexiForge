package com.cartoniq.lexiforge.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.lexiforge.ui.components.GlassCard
import com.cartoniq.lexiforge.ui.components.ScreenHeader
import com.cartoniq.lexiforge.ui.components.SectionTitle
import com.cartoniq.lexiforge.ui.theme.ErrorRed
import com.cartoniq.lexiforge.util.AppConstants
import com.cartoniq.lexiforge.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPrivacy by rememberSaveable { mutableStateOf(false) }
    var showReset by rememberSaveable { mutableStateOf(false) }
    var goalDraft by remember(state.dailyGoal) { mutableStateOf(state.dailyGoal.toFloat()) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            Toast.makeText(context, "Notification permission is needed for reminders", Toast.LENGTH_LONG).show()
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Settings", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Daily goal")
                Spacer(Modifier.height(4.dp))
                Text(
                    "${goalDraft.toInt()} cards per day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = goalDraft,
                    onValueChange = { goalDraft = it },
                    valueRange = 5f..100f,
                    steps = 18,
                    onValueChangeFinished = { viewModel.setDailyGoal(goalDraft.toInt()) }
                )
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Reminders")
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    title = "Daily reminder",
                    subtitle = "A nudge to keep your streak alive",
                    checked = state.reminderEnabled,
                    onChange = { enabled ->
                        val needsPermission = enabled && Build.VERSION.SDK_INT >= 33 &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        if (needsPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setReminderEnabled(enabled)
                        }
                    }
                )
                if (state.reminderEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Remind me at",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> viewModel.setReminderTime(hour, minute) },
                                state.reminderHour,
                                state.reminderMinute,
                                DateFormat.is24HourFormat(context)
                            ).show()
                        }) {
                            Text(String.format("%02d:%02d", state.reminderHour, state.reminderMinute))
                        }
                    }
                }
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("Study")
                Spacer(Modifier.height(8.dp))
                ToggleRow(
                    title = "Auto-pronounce",
                    subtitle = "Speak each word when its card appears",
                    checked = state.autoSpeak,
                    onChange = viewModel::setAutoSpeak
                )
                ToggleRow(
                    title = "Haptic feedback",
                    subtitle = "Vibrate when you flip a card",
                    checked = state.haptics,
                    onChange = viewModel::setHaptics
                )
            }

            GlassCard(Modifier.fillMaxWidth()) {
                SectionTitle("About & data")
                Spacer(Modifier.height(4.dp))
                ActionRow("Privacy Policy") { showPrivacy = true }
                ActionRow("Reset Progress", color = ErrorRed) { showReset = true }
                ActionRow("Contact: ${AppConstants.CONTACT_EMAIL}") {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${AppConstants.CONTACT_EMAIL}")
                        putExtra(Intent.EXTRA_SUBJECT, "LexiForge feedback")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "LexiForge v${AppConstants.VERSION} by CartoniQ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Privacy Policy") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(AppConstants.PRIVACY_POLICY, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Close") } }
        )
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Reset progress?") },
            text = {
                Text("All cards go back to new, and your streak, XP, level and review history are cleared. Your decks and cards are kept.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showReset = false
                    viewModel.resetProgress {
                        Toast.makeText(context, "Progress reset", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Reset", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ActionRow(
    label: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}
