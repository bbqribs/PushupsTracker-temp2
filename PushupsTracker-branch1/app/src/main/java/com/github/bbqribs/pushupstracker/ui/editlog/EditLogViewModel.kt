// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/editlog/EditLogViewModel.kt
package com.github.bbqribs.pushupstracker.ui.editlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.repository.AttemptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditLogViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository
) : ViewModel() {

    // Exposes the 10 most recent attempts as a flow for the UI to observe
    val recentAttempts: StateFlow<List<Attempt>> =
        attemptRepository.getRecentAttempts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Deletes a specific attempt from the database.
     */
    fun deleteAttempt(attempt: Attempt) {
        viewModelScope.launch {
            attemptRepository.delete(attempt)
        }
    }
}
