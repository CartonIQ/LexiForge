package com.cartoniq.lexiforge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class DeckWithDue(
    val id: Long,
    val name: String,
    val description: String,
    val cardCount: Int,
    val dueCount: Int
)

@Dao
interface DeckDao {

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun observeDecks(): Flow<List<DeckEntity>>

    @Query(
        "SELECT d.id AS id, d.name AS name, d.description AS description, d.cardCount AS cardCount, " +
            "(SELECT COUNT(*) FROM cards c WHERE c.deckId = d.id AND c.nextReviewDate <= :today) AS dueCount " +
            "FROM decks d ORDER BY d.createdAt DESC"
    )
    fun observeDecksWithDue(today: Long): Flow<List<DeckWithDue>>

    @Query("SELECT * FROM decks WHERE id = :id")
    fun observeDeck(id: Long): Flow<DeckEntity?>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeck(id: Long): DeckEntity?

    @Query("SELECT * FROM decks")
    suspend fun getAllDecks(): List<DeckEntity>

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun deckCount(): Int

    @Insert
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteDeckById(id: Long)

    @Query("UPDATE decks SET cardCount = (SELECT COUNT(*) FROM cards WHERE deckId = :deckId) WHERE id = :deckId")
    suspend fun refreshCardCount(deckId: Long)
}
