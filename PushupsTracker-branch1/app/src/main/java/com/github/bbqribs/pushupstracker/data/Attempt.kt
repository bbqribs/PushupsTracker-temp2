// File: app/src/main/java/com/github/bbqribs/pushupstracker/data/Attempt.kt
package com.github.bbqribs.pushupstracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attempts")
data class Attempt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val week: Int,
    val day: Int,
    val column: String,
    val outcome: String, // "SUCCESS", "PARTIAL", "INCOMPLETE", or "TEST"
    val setsCompleted: String // Pipe-separated values, e.g., "10|12|8|12"
)