package com.cartoniq.lexiforge.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cartoniq.lexiforge.data.UserStatsEntity
import com.cartoniq.lexiforge.lexiApp
import com.cartoniq.lexiforge.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val stats: UserStatsEntity = UserStatsEntity(),
    val totalReviews: Int = 0,
    val retentionPercent: Int = 0,
    val last7Days: List<Pair<String, Int>> = emptyList(),
    val newCards: Int = 0,
    val youngCards: Int = 0,
    val matureCards: Int = 0
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = application.lexiApp().repository
    private val today = DateUtils.today()

    val uiState: StateFlow<StatsUiState> = combine(
        repo.observeStats(),
        repo.observeTotalReviews(),
        repo.observeSuccessfulReviews(),
        repo.observeDailyCounts(today - 6),
        combine(repo.observeNewCount(), repo.observeYoungCount(), repo.observeMatureCount()) { a, b, c ->
            Triple(a, b, c)
        }
    ) { stats, total, success, daily, breakdown ->
        val byDay = daily.associate { it.day to it.total }
        val week = (6 downTo 0).map { offset ->
            val d = today - offset
            Pair(DateUtils.shortDayLabel(d), byDay[d] ?: 0)
        }
        StatsUiState(
            stats = stats,
            totalReviews = total,
            retentionPercent = if (total == 0) 0 else (success * 100) / total,
            last7Days = week,
            newCards = breakdown.first,
            youngCards = breakdown.second,
            matureCards = breakdown.third
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
