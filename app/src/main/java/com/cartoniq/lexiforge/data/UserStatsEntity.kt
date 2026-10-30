package com.cartoniq.lexiforge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    /** Epoch day of the last review, -1 = never. */
    val lastReviewDate: Long = -1L,
    /** XP inside the current level (0..99). */
    val xp: Int = 0,
    val level: Int = 1,
    val dailyGoal: Int = 20,
    val cardsReviewedToday: Int = 0,
    val streakFreezes: Int = 0,
    val totalXp: Int = 0
)
