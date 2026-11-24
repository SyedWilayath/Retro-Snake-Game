package com.nokiasnake.game.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScore(score: ScoreEntity)

    @Query("SELECT * FROM scores ORDER BY score DESC, timestamp ASC LIMIT 10")
    fun getTopScores(): Flow<List<ScoreEntity>>

    @Query("SELECT * FROM scores ORDER BY score DESC, timestamp ASC LIMIT 10")
    suspend fun getTopScoresOnce(): List<ScoreEntity>

    @Query("DELETE FROM scores")
    suspend fun clearAllScores()
}