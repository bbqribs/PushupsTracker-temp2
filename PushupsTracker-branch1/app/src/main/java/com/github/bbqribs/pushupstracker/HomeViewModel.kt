// File: app/src/main/java/com/github/bbqribs/pushupstracker/HomeViewModel.kt
package com.github.bbqribs.pushupstracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.model.PushupPlanEntry
import com.github.bbqribs.pushupstracker.repository.AttemptRepository
import com.github.bbqribs.pushupstracker.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val attemptRepository: AttemptRepository,
    val planRepository: PlanRepository
) : ViewModel() {

    val allAttempts: StateFlow<List<Attempt>> = attemptRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastAttempt: StateFlow<Attempt?> = attemptRepository.getLastAttempt()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val upcomingSession: StateFlow<PushupPlanEntry?> = attemptRepository.getLastNormalAttempt()
        .map { lastNormal -> calculateNextSession(lastNormal) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private fun calculateNextSession(lastNormal: Attempt?): PushupPlanEntry? {
        if (lastNormal == null) {
            return planRepository.plan.firstOrNull()
        }

        val (nextWeek, nextDay) = if (lastNormal.day < 3) {
            lastNormal.week to lastNormal.day + 1
        } else {
            lastNormal.week + 1 to 1
        }

        return planRepository.plan.find { it.week == nextWeek && it.day == nextDay && it.column == lastNormal.column }
    }
}