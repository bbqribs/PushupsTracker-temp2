// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/SessionProgressViewModel.kt
package com.github.bbqribs.pushupstracker.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.model.PushupPlanEntry
import com.github.bbqribs.pushupstracker.repository.AttemptRepository
import com.github.bbqribs.pushupstracker.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// The SessionUiState must be at the top-level or in its own file
// to be accessible by both the ViewModel and the UI.
data class SessionUiState(
    val currentIndex: Int = 0,
    val targetReps: Int = 0,
    val restSecondsLeft: Int? = null,
    val results: List<Pair<Int, Int>> = emptyList(),
    val isSessionComplete: Boolean = false
)

@HiltViewModel
class SessionProgressViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository,
    private val planRepository: PlanRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val week: Int = savedStateHandle.get<Int>("week")!!
    private val day: Int = savedStateHandle.get<Int>("day")!!
    private val column: String = savedStateHandle.get<String>("column")!!

    // ✅ THE CRITICAL FIX: Use .firstOrNull() to prevent crashing if a plan isn't found.
    private val plan: PushupPlanEntry? = planRepository.plan.firstOrNull {
        it.week == week && it.day == day && it.column == column
    }

    val setStrings: List<String> = plan?.sets ?: listOf("Error: Plan not found")
    private val restString: String = plan?.rest ?: "60s"

    private val _uiState = MutableStateFlow(
        SessionUiState(targetReps = parseSetMinimum(setStrings.firstOrNull() ?: ""))
    )
    val uiState: StateFlow<SessionUiState> = _uiState

    private var restJob: Job? = null

    init {
        if (plan == null) {
            _uiState.value = _uiState.value.copy(isSessionComplete = true)
        }
    }

    fun completeSet() {
        val currentState = _uiState.value
        val recommended = parseSetMinimum(setStrings[currentState.currentIndex])
        val newResults = currentState.results + (recommended to recommended)
        _uiState.value = currentState.copy(results = newResults)

        if (currentState.currentIndex < setStrings.lastIndex) {
            startRestTimer()
        } else {
            _uiState.value = _uiState.value.copy(isSessionComplete = true)
            logAttempt(newResults)
        }
    }

    fun submitFinalSet(actualReps: Int) {
        val currentState = _uiState.value
        val recommended = parseSetMinimum(setStrings[currentState.currentIndex])
        val finalResults = currentState.results + (actualReps to recommended)
        _uiState.value = _uiState.value.copy(
            results = finalResults,
            isSessionComplete = true
        )
        logAttempt(finalResults)
    }

    fun skipRest() {
        restJob?.cancel()
        advanceToNextSet()
    }

    private fun logAttempt(results: List<Pair<Int, Int>>) {
        if (plan == null) return
        viewModelScope.launch {
            val outcome = determineOutcome(results)
            val setsCompletedString = results.joinToString("|") { it.first.toString() }
            val newAttempt = Attempt(
                timestamp = System.currentTimeMillis(),
                week = this@SessionProgressViewModel.week,
                day = this@SessionProgressViewModel.day,
                column = this@SessionProgressViewModel.column,
                outcome = outcome,
                setsCompleted = setsCompletedString
            )
            attemptRepository.insert(newAttempt)
        }
    }

    private fun startRestTimer() {
        restJob?.cancel()
        val restSeconds = parseRestSeconds(restString)
        restJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(restSecondsLeft = restSeconds)
            while ((_uiState.value.restSecondsLeft ?: 0) > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    restSecondsLeft = (_uiState.value.restSecondsLeft ?: 1) - 1
                )
            }
            advanceToNextSet()
        }
    }

    private fun advanceToNextSet() {
        restJob = null
        val newIndex = _uiState.value.currentIndex + 1
        _uiState.value = _uiState.value.copy(
            currentIndex = newIndex,
            targetReps = parseSetMinimum(setStrings.getOrNull(newIndex) ?: ""),
            restSecondsLeft = null
        )
    }

    private fun determineOutcome(results: List<Pair<Int, Int>>): String {
        if (results.all { (actual, rec) -> actual >= rec }) return "SUCCESS"
        if (results.any { (actual, rec) -> actual > 0 && actual < rec }) return "PARTIAL"
        return "INCOMPLETE"
    }

    private fun parseSetMinimum(s: String): Int {
        return s.uppercase().substringAfter("≥", s).toIntOrNull() ?: 0
    }

    private fun parseRestSeconds(r: String): Int {
        Regex("""(\d+)-(\d+)s""").find(r)?.let {
            val (low, high) = it.destructured
            return Random.nextInt(low.toInt(), high.toInt() + 1)
        }
        Regex("""(\d+)s""").find(r)?.let {
            return it.groupValues[1].toIntOrNull() ?: 60
        }
        return 60
    }
}
