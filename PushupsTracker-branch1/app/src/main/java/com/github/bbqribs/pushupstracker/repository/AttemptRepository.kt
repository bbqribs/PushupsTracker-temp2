// File: app/src/main/java/com/github/bbqribs/pushupstracker/repository/AttemptRepository.kt
package com.github.bbqribs.pushupstracker.repository

import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.data.AttemptDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Tells Hilt to create only one instance of this repository
class AttemptRepository @Inject constructor(private val dao: AttemptDao) {
    // The @Inject constructor tells Hilt how to create an AttemptRepository.
    // It says: "To make me, you need an AttemptDao. Go find it."

    fun getAll(): Flow<List<Attempt>> = dao.getAll()
    fun getRecentAttempts(): Flow<List<Attempt>> = dao.getRecentAttempts()
    fun getLastAttempt(): Flow<Attempt?> = dao.getLastAttempt()
    fun getLastNormalAttempt(): Flow<Attempt?> = dao.getLastNormalAttempt()
    suspend fun insert(attempt: Attempt) = dao.insert(attempt)
    suspend fun delete(attempt: Attempt) = dao.delete(attempt)
}