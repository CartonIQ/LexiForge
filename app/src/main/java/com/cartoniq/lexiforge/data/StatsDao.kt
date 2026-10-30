package com.cartoniq.lexiforge.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DayCount(val day: Long, val total: Int)

@Dao
interface StatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun observeStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: UserStatsEntity)

    @Insert
    suspend fun insertLog(log: ReviewLogEntity)

    @Query("DELETE FROM review_logs")
    suspend fun clearLogs()

    @Query("SELECT epochDay AS day, COUNT(*) AS total FROM review_logs WHERE epochDay >= :fromDay GROUP BY epochDay ORDER BY epochDay ASC")
    fun observeDailyCounts(fromDay: Long): Flow<List<DayCount>>

    @Query("SELECT COUNT(*) FROM review_logs")
    fun observeTotalReviews(): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_logs WHERE quality >= 3")
    fun observeSuccessfulReviews(): Flow<Int>
}
