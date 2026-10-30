package com.cartoniq.lexiforge.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cartoniq.lexiforge.data.CardEntity
import com.cartoniq.lexiforge.data.DeckEntity
import com.cartoniq.lexiforge.lexiApp
import com.cartoniq.lexiforge.util.CsvUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeckViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repo = application.lexiApp().repository
    val deckId: Long = savedStateHandle.get<Long>("deckId") ?: -1L

    val deck: StateFlow<DeckEntity?> = repo.observeDeck(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val cards: StateFlow<List<CardEntity>> = repo.observeCards(deckId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun consumeMessage() {
        _message.value = null
    }

    fun saveCard(editing: CardEntity?, front: String, back: String, example: String) {
        viewModelScope.launch {
            val card = if (editing == null) {
                CardEntity(deckId = deckId, front = front.trim(), back = back.trim(), example = example.trim())
            } else {
                editing.copy(front = front.trim(), back = back.trim(), example = example.trim())
            }
            repo.saveCard(card)
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch { repo.deleteCard(card) }
    }

    fun updateDeck(name: String, description: String, languageTag: String) {
        val current = deck.value ?: return
        viewModelScope.launch {
            repo.updateDeck(current.copy(name = name, description = description, languageTag = languageTag))
        }
    }

    fun deleteDeck(onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteDeck(deckId)
            onDone()
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                try {
                    val lines = getApplication<Application>().contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readLines() }
                        ?: emptyList()
                    val cards = CsvUtils.parse(lines).map {
                        CardEntity(deckId = deckId, front = it.front, back = it.back, example = it.example)
                    }
                    repo.addCards(deckId, cards)
                    cards.size
                } catch (e: Exception) {
                    -1
                }
            }
            _message.value = if (count >= 0) "Imported $count cards" else "Couldn't read that file"
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val csv = CsvUtils.toCsv(repo.getCardsOnce(deckId))
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                        it.write(csv.toByteArray(Charsets.UTF_8))
                    }
                    true
                } catch (e: Exception) {
                    false
                }
            }
            _message.value = if (ok) "Deck exported" else "Export failed"
        }
    }
}
