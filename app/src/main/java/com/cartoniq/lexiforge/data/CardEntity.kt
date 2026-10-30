package com.cartoniq.lexiforge.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"]), Index(value = ["nextReviewDate"])]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val example: String = "",
    val interval: Int = 1,
    val repetition: Int = 0,
    val easeFactor: Double = 2.5,
    /** Epoch day (local time zone). 0 = due immediately (new card). */
    val nextReviewDate: Long = 0L,
    /** Epoch millis. 0 = never reviewed. */
    val lastReviewedAt: Long = 0L
)
