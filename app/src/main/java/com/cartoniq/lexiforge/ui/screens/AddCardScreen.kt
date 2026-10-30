package com.cartoniq.lexiforge.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cartoniq.lexiforge.data.CardEntity
import com.cartoniq.lexiforge.ui.components.DeckEditorDialog
import com.cartoniq.lexiforge.ui.components.GlassCard
import com.cartoniq.lexiforge.ui.components.ScreenHeader
import com.cartoniq.lexiforge.ui.components.SectionTitle
import com.cartoniq.lexiforge.ui.theme.Emerald
import com.cartoniq.lexiforge.ui.theme.ErrorRed
import com.cartoniq.lexiforge.util.DateUtils
import com.cartoniq.lexiforge.util.Languages
import com.cartoniq.lexiforge.viewmodel.DeckViewModel
import kotlinx.coroutines.launch

@Composable
fun AddCardScreen(
    onBack: () -> Unit,
    onStudyDeck: (Long) -> Unit,
    viewModel: DeckViewModel = viewModel()
) {
    val deck by viewModel.deck.collectAsStateWithLifecycle()
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val frontFocus = remember { FocusRequester() }

    var front by rememberSaveable { mutableStateOf("") }
    var back by rememberSaveable { mutableStateOf("") }
    var example by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf(-1L) }
    var query by rememberSaveable { mutableStateOf("") }
    var showEditDeck by rememberSaveable { mutableStateOf(false) }
    var showDeleteDeck by rememberSaveable { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<CardEntity?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importCsv(uri)
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) viewModel.exportCsv(uri)
    }

    LaunchedEffect(message) {
        val text = message
        if (text != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    val duplicate = front.isNotBlank() && cards.any {
        it.id != editingId && it.front.trim().equals(front.trim(), ignoreCase = true)
    }
    val canSave = front.isNotBlank() && back.isNotBlank() && !duplicate
    val filtered = remember(cards, query) {
        if (query.isBlank()) cards
        else cards.filter { it.front.contains(query, ignoreCase = true) || it.back.contains(query, ignoreCase = true) }
    }

    fun resetForm() {
        front = ""
        back = ""
        example = ""
        editingId = -1L
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = deck?.name ?: "Deck",
            onBack = onBack,
            actions = {
                IconButton(onClick = { onStudyDeck(viewModel.deckId) }, enabled = cards.isNotEmpty()) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Study this deck")
                }
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    SectionTitle(if (editingId >= 0) "Edit Card" else "Add Card")
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = front,
                        onValueChange = { front = it },
                        label = { Text("Front (word)") },
                        singleLine = true,
                        isError = duplicate,
                        supportingText = { if (duplicate) Text("This word is already in the deck") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(frontFocus)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = back,
                        onValueChange = { back = it },
                        label = { Text("Back (definition)") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = example,
                        onValueChange = { example = it },
                        label = { Text("Example (optional)") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val editing = cards.firstOrNull { it.id == editingId }
                                viewModel.saveCard(editing, front, back, example)
                                resetForm()
                                frontFocus.requestFocus()
                            },
                            enabled = canSave,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (editingId >= 0) "Save Changes" else "Save Card")
                        }
                        if (editingId >= 0) {
                            OutlinedButton(onClick = { resetForm() }, shape = RoundedCornerShape(14.dp)) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Text("Import CSV") }
                    OutlinedButton(
                        onClick = {
                            val safe = (deck?.name ?: "deck").replace(Regex("[^A-Za-z0-9_-]"), "_")
                            exportLauncher.launch("$safe.csv")
                        },
                        enabled = cards.isNotEmpty()
                    ) { Text("Export CSV") }
                    OutlinedButton(onClick = { showEditDeck = true }) { Text("Edit Deck") }
                    OutlinedButton(onClick = { showDeleteDeck = true }) {
                        Text("Delete Deck", color = ErrorRed)
                    }
                }
            }

            item {
                Text(
                    text = "Pronunciation: ${Languages.labelFor(deck?.languageTag ?: "")}   |   CSV format: front,back,example",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search ${cards.size} cards") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = if (cards.isEmpty()) "No cards yet. Add your first word above." else "No matches.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }

            items(filtered, key = { it.id }) { card ->
                GlassCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                card.front,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                card.back,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (card.example.isNotBlank()) {
                                Text(
                                    card.example,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                DateUtils.dueLabel(card),
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald
                            )
                        }
                        IconButton(onClick = {
                            front = card.front
                            back = card.back
                            example = card.example
                            editingId = card.id
                            scope.launch { listState.animateScrollToItem(0) }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit card", modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { cardToDelete = card }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete card", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    val currentDeck = deck
    if (showEditDeck && currentDeck != null) {
        DeckEditorDialog(
            title = "Edit Deck",
            confirmLabel = "Save",
            initialName = currentDeck.name,
            initialDescription = currentDeck.description,
            initialLanguage = currentDeck.languageTag,
            onDismiss = { showEditDeck = false },
            onConfirm = { name, description, language ->
                showEditDeck = false
                viewModel.updateDeck(name, description, language)
            }
        )
    }

    if (showDeleteDeck) {
        AlertDialog(
            onDismissRequest = { showDeleteDeck = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete this deck?") },
            text = { Text("All ${cards.size} cards and their history will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDeck = false
                    viewModel.deleteDeck { onBack() }
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDeck = false }) { Text("Cancel") } }
        )
    }

    val pendingDelete = cardToDelete
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { cardToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Delete card?") },
            text = { Text("\"${pendingDelete.front}\" will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCard(pendingDelete)
                    if (editingId == pendingDelete.id) resetForm()
                    cardToDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = { TextButton(onClick = { cardToDelete = null }) { Text("Cancel") } }
        )
    }
}
