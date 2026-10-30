package com.cartoniq.lexiforge.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Insert
    suspend fun insert(card: CardEntity): Long

    @Update
    suspend fun update(card: CardEntity)

    @Delete
    suspend fun delete(card: CardEntity)

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCard(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY id DESC")
    fun observeCardsInDeck(deckId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY id ASC")
    suspend fun getCardsInDeck(deckId: Long): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE nextReviewDate <= :today")
    fun observeDueCount(today: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE nextReviewDate <= :today")
    suspend fun countDue(today: Long): Int

    @Query("SELECT COUNT(*) FROM cards")
    fun observeTotalCards(): Flow<Int>

    @Query("SELECT * FROM cards WHERE nextReviewDate <= :today ORDER BY nextReviewDate ASC, id ASC LIMIT :limit")
    suspend fun getDueCards(today: Long, limit: Int): List<CardEntity>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND nextReviewDate <= :today ORDER BY nextReviewDate ASC, id ASC LIMIT :limit")
    suspend fun getDueCardsForDeck(deckId: Long, today: Long, limit: Int): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE lastReviewedAt = 0")
    fun observeNewCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE lastReviewedAt > 0 AND interval < 21")
    fun observeYoungCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cards WHERE lastReviewedAt > 0 AND interval >= 21")
    fun observeMatureCount(): Flow<Int>

    @Query("UPDATE cards SET interval = 1, repetition = 0, easeFactor = 2.5, nextReviewDate = 0, lastReviewedAt = 0")
    suspend fun resetAllProgress()
}
