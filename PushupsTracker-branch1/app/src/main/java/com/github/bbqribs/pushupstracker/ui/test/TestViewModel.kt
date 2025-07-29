// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/test/TestViewModel.kt
package com.github.bbqribs.pushupstracker.ui.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.repository.AttemptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository
) : ViewModel() {

    /**
     * Creates and saves a special "TEST" attempt to the database.
     */
    fun logTest(pushupCount: Int) {
        if (pushupCount <= 0) return // Ignore invalid input

        viewModelScope.launch {
            val testAttempt = Attempt(
                timestamp = System.currentTimeMillis(),
                week = -1, // Special value for tests
                day = -1,  // Special value for tests
                column = "TEST",
                outcome = "TEST",
                setsCompleted = pushupCount.toString()
            )
            attemptRepository.insert(testAttempt)
        }
    }
}
