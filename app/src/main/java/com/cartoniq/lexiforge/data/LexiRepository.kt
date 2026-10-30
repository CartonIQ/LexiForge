package com.cartoniq.lexiforge.data

import androidx.room.withTransaction
import com.cartoniq.lexiforge.domain.SM2Algorithm
import com.cartoniq.lexiforge.util.AppConstants
import com.cartoniq.lexiforge.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ReviewOutcome(
    val xpGained: Int,
    val levelsGained: Int,
    val newLevel: Int,
    val streak: Int
)

class LexiRepository(
    private val db: AppDatabase,
    private val prefs: AppPrefs
) {
    private val deckDao = db.deckDao()
    private val cardDao = db.cardDao()
    private val statsDao = db.statsDao()

    // ---------- Initialisation ----------

    suspend fun initialize() {
        if (statsDao.getStats() == null) statsDao.upsertStats(UserStatsEntity())
        rollover()
        if (!prefs.seeded) {
            if (deckDao.deckCount() == 0) seedStarterDeck()
            prefs.seeded = true
        }
    }

    private suspend fun seedStarterDeck() {
        val deckId = deckDao.insertDeck(
            DeckEntity(
                name = "Spanish Starter",
                description = "Everyday words to get you going",
                languageTag = "es-ES"
            )
        )
        val words = listOf(
            Triple("hola", "hello", "¡Hola! ¿Cómo estás?"),
            Triple("gracias", "thank you", "Muchas gracias por tu ayuda."),
            Triple("por favor", "please", "Un café, por favor."),
            Triple("buenos días", "good morning", "Buenos días, señora."),
            Triple("adiós", "goodbye", "Adiós, hasta mañana."),
            Triple("agua", "water", "Quiero un vaso de agua."),
            Triple("amigo", "friend", "Él es mi mejor amigo."),
            Triple("casa", "house, home", "Mi casa es tu casa."),
            Triple("libro", "book", "Estoy leyendo un libro interesante."),
            Triple("comida", "food", "La comida está deliciosa.")
        )
        words.forEach {
            cardDao.insert(CardEntity(deckId = deckId, front = it.first, back = it.second, example = it.third))
        }
        deckDao.refreshCardCount(deckId)
    }

    /** Keeps streak honest across days; consumes streak freezes for missed days. */
    suspend fun rollover() {
        db.withTransaction {
            val today = DateUtils.today()
            var s = statsDao.getStats() ?: UserStatsEntity()
            if (s.lastReviewDate in 0 until today) {
                val missed = (today - s.lastReviewDate - 1).toInt()
                if (missed > 0) {
                    s = if (s.streakFreezes >= missed) {
                        s.copy(streakFreezes = s.streakFreezes - missed, lastReviewDate = today - 1)
                    } else {
                        s.copy(currentStreak = 0)
                    }
                }
                s = s.copy(cardsReviewedToday = 0)
                statsDao.upsertStats(s)
            }
        }
    }

    // ---------- Observers ----------

    fun observeStats(): Flow<UserStatsEntity> = statsDao.observeStats().map { it ?: UserStatsEntity() }
    suspend fun getStatsOnce(): UserStatsEntity = statsDao.getStats() ?: UserStatsEntity()

    fun observeDecksWithDue(today: Long): Flow<List<DeckWithDue>> = deckDao.observeDecksWithDue(today)
    fun observeDeck(id: Long): Flow<DeckEntity?> = deckDao.observeDeck(id)
    fun observeCards(deckId: Long): Flow<List<CardEntity>> = cardDao.observeCardsInDeck(deckId)
    fun observeDueCount(today: Long): Flow<Int> = cardDao.observeDueCount(today)
    fun observeTotalCards(): Flow<Int> = cardDao.observeTotalCards()
    fun observeNewCount(): Flow<Int> = cardDao.observeNewCount()
    fun observeYoungCount(): Flow<Int> = cardDao.observeYoungCount()
    fun observeMatureCount(): Flow<Int> = cardDao.observeMatureCount()
    fun observeDailyCounts(fromDay: Long): Flow<List<DayCount>> = statsDao.observeDailyCounts(fromDay)
    fun observeTotalReviews(): Flow<Int> = statsDao.observeTotalReviews()
    fun observeSuccessfulReviews(): Flow<Int> = statsDao.observeSuccessfulReviews()

    // ---------- Decks & cards ----------

    suspend fun createDeck(name: String, description: String, languageTag: String): Long =
        deckDao.insertDeck(DeckEntity(name = name, description = description, languageTag = languageTag))

    suspend fun updateDeck(deck: DeckEntity) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(id: Long) = deckDao.deleteDeckById(id)

    suspend fun saveCard(card: CardEntity) {
        db.withTransaction {
            if (card.id == 0L) cardDao.insert(card) else cardDao.update(card)
            deckDao.refreshCardCount(card.deckId)
        }
    }

    suspend fun addCards(deckId: Long, cards: List<CardEntity>) {
        db.withTransaction {
            cards.forEach { cardDao.insert(it) }
            deckDao.refreshCardCount(deckId)
        }
    }

    suspend fun deleteCard(card: CardEntity) {
        db.withTransaction {
            cardDao.delete(card)
            deckDao.refreshCardCount(card.deckId)
        }
    }

    suspend fun getCardsOnce(deckId: Long): List<CardEntity> = cardDao.getCardsInDeck(deckId)

    suspend fun getDeckLanguages(): Map<Long, String> =
        deckDao.getAllDecks().associate { it.id to it.languageTag }

    // ---------- Study ----------

    suspend fun loadSession(deckId: Long?): List<CardEntity> {
        val today = DateUtils.today()
        return if (deckId == null) {
            cardDao.getDueCards(today, AppConstants.SESSION_SIZE)
        } else {
            cardDao.getDueCardsForDeck(deckId, today, AppConstants.SESSION_SIZE)
        }
    }

    suspend fun submitReview(card: CardEntity, quality: Int): ReviewOutcome = db.withTransaction {
        val now = System.currentTimeMillis()
        val today = DateUtils.epochDay(now)

        cardDao.update(SM2Algorithm.applyReview(card, quality, today, now))
        statsDao.insertLog(ReviewLogEntity(cardId = card.id, reviewDate = now, epochDay = today, quality = quality))

        val current = statsDao.getStats() ?: UserStatsEntity()
        val newStreak = when (current.lastReviewDate) {
            today -> maxOf(current.currentStreak, 1)
            today - 1 -> current.currentStreak + 1
            else -> 1
        }
        val reviewedToday = if (current.lastReviewDate == today) current.cardsReviewedToday + 1 else 1

        val base = current.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(current.longestStreak, newStreak),
            lastReviewDate = today,
            cardsReviewedToday = reviewedToday
        )
        val (updated, levels) = applyXp(base, AppConstants.XP_PER_CARD)
        statsDao.upsertStats(updated)
        ReviewOutcome(AppConstants.XP_PER_CARD, levels, updated.level, updated.currentStreak)
    }

    suspend fun awardBonusXp(amount: Int): ReviewOutcome = db.withTransaction {
        val current = statsDao.getStats() ?: UserStatsEntity()
        val (updated, levels) = applyXp(current, amount)
        statsDao.upsertStats(updated)
        ReviewOutcome(amount, levels, updated.level, updated.currentStreak)
    }

    private fun applyXp(s: UserStatsEntity, amount: Int): Pair<UserStatsEntity, Int> {
        var xp = s.xp + amount
        var level = s.level
        var freezes = s.streakFreezes
        var gained = 0
        while (xp >= AppConstants.XP_PER_LEVEL) {
            xp -= AppConstants.XP_PER_LEVEL
            level += 1
            gained += 1
            freezes = minOf(freezes + 1, AppConstants.MAX_STREAK_FREEZES)
        }
        return Pair(s.copy(xp = xp, level = level, streakFreezes = freezes, totalXp = s.totalXp + amount), gained)
    }

    // ---------- Settings ----------

    suspend fun setDailyGoal(goal: Int) {
        val s = statsDao.getStats() ?: UserStatsEntity()
        statsDao.upsertStats(s.copy(dailyGoal = goal))
    }

    suspend fun resetProgress() {
        db.withTransaction {
            cardDao.resetAllProgress()
            statsDao.clearLogs()
            val goal = statsDao.getStats()?.dailyGoal ?: AppConstants.DEFAULT_DAILY_GOAL
            statsDao.upsertStats(UserStatsEntity(dailyGoal = goal))
        }
    }
}
