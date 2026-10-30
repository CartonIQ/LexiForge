package com.cartoniq.lexiforge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.cartoniq.lexiforge.data.AppPrefs
import com.cartoniq.lexiforge.data.CardEntity
import com.cartoniq.lexiforge.domain.SM2Algorithm
import com.cartoniq.lexiforge.lexiApp
import com.cartoniq.lexiforge.util.DateUtils
import com.cartoniq.lexiforge.util.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudyItem(val card: CardEntity, val isRetry: Boolean, val token: Int)

data class StudyUiState(
    val loading: Boolean = true,
    val current: StudyItem? = null,
    val totalCards: Int = 0,
    val doneCount: Int = 0,
    val sessionXp: Int = 0,
    val reviewedCount: Int = 0,
    val correctCount: Int = 0,
    val complete: Boolean = false,
    val levelUpTo: Int? = null,
    val streak: Int = 0,
    val bonusClaimed: Boolean = false,
    /** Interval labels for Again / Hard / Good / Easy. Empty for in-session retries. */
    val previews: List<String> = emptyList()
) {
    val progress: Float
        get() = if (totalCards == 0) 0f else doneCount.toFloat() / totalCards
}

class StudyViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repo = application.lexiApp().repository
    private val prefs = AppPrefs.get(application)
    private val tts = TtsManager(application)

    private val deckId: Long = savedStateHandle.get<Long>("deckId") ?: -1L
    val hapticsEnabled: Boolean = prefs.haptics

    private var deckLanguages: Map<Long, String> = emptyMap()
    private val queue = mutableListOf<StudyItem>()
    private var tokenCounter = 0
    private var busy = false

    private val _state = MutableStateFlow(StudyUiState())
    val state: StateFlow<StudyUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { start() }
    }

    private suspend fun start() {
        deckLanguages = repo.getDeckLanguages()
        val cards = repo.loadSession(if (deckId >= 0) deckId else null)
        val stats = repo.getStatsOnce()
        cards.shuffled().forEach { queue.add(StudyItem(it, false, tokenCounter++)) }
        _state.update {
            it.copy(loading = false, totalCards = cards.size, streak = stats.currentStreak)
        }
        if (cards.isNotEmpty()) advance()
    }

    private fun advance() {
        if (queue.isEmpty()) {
            _state.update { it.copy(current = null, complete = true, previews = emptyList()) }
            return
        }
        val next = queue.removeAt(0)
        _state.update { it.copy(current = next, previews = buildPreviews(next)) }
        if (prefs.autoSpeak) speakCurrent()
    }

    private fun buildPreviews(item: StudyItem): List<String> {
        if (item.isRetry) return emptyList()
        val c = item.card
        return listOf(
            "Retry",
            DateUtils.formatInterval(SM2Algorithm.previewInterval(c, SM2Algorithm.HARD)),
            DateUtils.formatInterval(SM2Algorithm.previewInterval(c, SM2Algorithm.GOOD)),
            DateUtils.formatInterval(SM2Algorithm.previewInterval(c, SM2Algorithm.EASY))
        )
    }

    fun rate(quality: Int) {
        val item = _state.value.current ?: return
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                val failed = quality < SM2Algorithm.PASS_THRESHOLD
                if (item.isRetry) {
                    if (failed) {
                        queue.add(StudyItem(item.card, true, tokenCounter++))
                    } else {
                        _state.update { it.copy(doneCount = it.doneCount + 1) }
                    }
                } else {
                    val outcome = repo.submitReview(item.card, quality)
                    if (failed) queue.add(StudyItem(item.card, true, tokenCounter++))
                    _state.update { s ->
                        s.copy(
                            doneCount = if (failed) s.doneCount else s.doneCount + 1,
                            sessionXp = s.sessionXp + outcome.xpGained,
                            reviewedCount = s.reviewedCount + 1,
                            correctCount = if (failed) s.correctCount else s.correctCount + 1,
                            streak = outcome.streak,
                            levelUpTo = if (outcome.levelsGained > 0) outcome.newLevel else s.levelUpTo
                        )
                    }
                }
                advance()
            } finally {
                busy = false
            }
        }
    }

    fun claimBonusXp() {
        val s = _state.value
        if (s.bonusClaimed || s.sessionXp <= 0) return
        val amount = s.sessionXp
        _state.update { it.copy(bonusClaimed = true) }
        viewModelScope.launch {
            val outcome = repo.awardBonusXp(amount)
            _state.update {
                it.copy(
                    sessionXp = it.sessionXp + outcome.xpGained,
                    levelUpTo = if (outcome.levelsGained > 0) outcome.newLevel else it.levelUpTo
                )
            }
        }
    }

    fun dismissLevelUp() {
        _state.update { it.copy(levelUpTo = null) }
    }

    fun speakCurrent() {
        val item = _state.value.current ?: return
        tts.speak(item.card.front, deckLanguages[item.card.deckId] ?: "")
    }

    override fun onCleared() {
        tts.shutdown()
        super.onCleared()
    }
}
