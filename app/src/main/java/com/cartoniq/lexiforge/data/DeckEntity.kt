package com.cartoniq.lexiforge.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val cardCount: Int = 0,
    /** BCP-47 tag used for Text-to-Speech. Empty = device default. */
    val languageTag: String = ""
)
