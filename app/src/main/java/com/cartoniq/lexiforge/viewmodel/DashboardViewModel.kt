package com.cartoniq.lexiforge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.lexiforge.data.DeckWithDue
import com.cartoniq.lexiforge.data.UserStatsEntity
import com.cartoniq.lexiforge.lexiApp
import com.cartoniq.lexiforge.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val stats: UserStatsEntity = UserStatsEntity(),
    val dueToday: Int = 0,
    val totalCards: Int = 0,
    val decks: List<DeckWithDue> = emptyList(),
    val loaded: Boolean = false
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = application.lexiApp().repository
    private val today = MutableStateFlow(DateUtils.today())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = today.flatMapLatest { day ->
        combine(
            repo.observeStats(),
            repo.observeDueCount(day),
            repo.observeTotalCards(),
            repo.observeDecksWithDue(day)
        ) { stats, due, total, decks ->
            DashboardUiState(stats = stats, dueToday = due, totalCards = total, decks = decks, loaded = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun refresh() {
        viewModelScope.launch {
            repo.rollover()
            today.value = DateUtils.today()
        }
    }

    fun createDeck(name: String, description: String, languageTag: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repo.createDeck(name, description, languageTag)
            onCreated(id)
        }
    }
}
