// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/HomeScreen.kt
package com.github.bbqribs.pushupstracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.bbqribs.pushupstracker.HomeViewModel
import com.github.bbqribs.pushupstracker.ui.components.ActionCard
import com.github.bbqribs.pushupstracker.ui.components.LastAttemptCard
import com.github.bbqribs.pushupstracker.ui.components.ProgressChart
import com.github.bbqribs.pushupstracker.ui.components.SessionPicker
import com.github.bbqribs.pushupstracker.ui.components.UpcomingSessionCard
import com.github.bbqribs.pushupstracker.ui.theme.AppTheme

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel(),
    onStartSession: (week: Int, day: Int, column: String) -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToTest: () -> Unit
) {
    val allAttempts by vm.allAttempts.collectAsState()
    val lastAttempt by vm.lastAttempt.collectAsState()
    val upcomingSession by vm.upcomingSession.collectAsState()

    val chartHeightFraction = when {
        lastAttempt != null && upcomingSession != null -> 0.25f
        lastAttempt != null || upcomingSession != null -> 0.35f
        else -> 0.5f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(chartHeightFraction),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.cardBackground
            )
        ) {
            if (allAttempts.any { it.outcome != "TEST" }) {
                ProgressChart(
                    attempts = allAttempts,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log your first session to see progress!")
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        lastAttempt?.let {
            LastAttemptCard(attempt = it) {
                onStartSession(it.week, it.day, it.column)
            }
        }

        upcomingSession?.let {
            UpcomingSessionCard(session = it) {
                onStartSession(it.week, it.day, it.column)
            }
        }

        SessionPicker(vm = vm, onGo = onStartSession)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ActionCard("Edit Log", Modifier.weight(1f), onNavigateToEdit)
            ActionCard("Test", Modifier.weight(1f), onNavigateToTest)
        }
    }
}