// File: app/src/main/java/com/github/bbqribs/pushupstracker/data/PushupsDatabase.kt
package com.github.bbqribs.pushupstracker.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Attempt::class],
    version = 3,
    exportSchema = false
)
abstract class PushupsDatabase : RoomDatabase() {
    abstract fun attemptDao(): AttemptDao
}