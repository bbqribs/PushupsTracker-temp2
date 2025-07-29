// File: app/src/main/java/com/github/bbqribs/pushupstracker/di/AppModule.kt
package com.github.bbqribs.pushupstracker.di

import android.content.Context
import androidx.room.Room
import com.github.bbqribs.pushupstracker.data.AttemptDao
import com.github.bbqribs.pushupstracker.data.PushupsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePushupsDatabase(@ApplicationContext context: Context): PushupsDatabase {
        // ✅ The database is now built and managed directly by Hilt.
        return Room.databaseBuilder(
            context,
            PushupsDatabase::class.java,
            "pushups_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAttemptDao(database: PushupsDatabase): AttemptDao {
        return database.attemptDao()
    }
}