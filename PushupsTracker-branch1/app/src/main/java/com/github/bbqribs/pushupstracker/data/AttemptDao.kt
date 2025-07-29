// File: app/src/main/java/com/github/bbqribs/pushupstracker/data/AttemptDao.kt
package com.github.bbqribs.pushupstracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttemptDao {
    @Query("SELECT * FROM attempts ORDER BY timestamp ASC")
    fun getAll(): Flow<List<Attempt>>

    // ✅ ADD THIS FUNCTION BACK
    @Query("SELECT * FROM attempts ORDER BY timestamp DESC LIMIT 10")
    fun getRecentAttempts(): Flow<List<Attempt>>

    @Query("SELECT * FROM attempts ORDER BY timestamp DESC LIMIT 1")
    fun getLastAttempt(): Flow<Attempt?>

    @Query("SELECT * FROM attempts WHERE outcome != 'TEST' ORDER BY timestamp DESC LIMIT 1")
    fun getLastNormalAttempt(): Flow<Attempt?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: Attempt)

    @Update
    suspend fun update(attempt: Attempt)

    @Delete
    suspend fun delete(attempt: Attempt)
}