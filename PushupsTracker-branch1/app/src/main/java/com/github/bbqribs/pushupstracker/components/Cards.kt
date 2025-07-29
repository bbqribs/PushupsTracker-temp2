// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/components/Cards.kt
package com.github.bbqribs.pushupstracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.bbqribs.pushupstracker.data.Attempt
import com.github.bbqribs.pushupstracker.model.PushupPlanEntry
import com.github.bbqribs.pushupstracker.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LastAttemptCard(
    attempt: Attempt,
    onClick: () -> Unit
) {
    val date = remember(attempt.timestamp) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(attempt.timestamp))
    }
    val isClickable = attempt.outcome != "TEST"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.cardBackground
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Last Attempt ($date)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("W${attempt.week} D${attempt.day} Col ${attempt.column} — ${attempt.outcome}")
            Text("Sets: ${attempt.setsCompleted.replace('|', '/')}")
            Text(
                "Total: ${
                    attempt.setsCompleted
                        .split('|')
                        .sumOf { it.toIntOrNull() ?: 0 }
                }"
            )
        }
    }
}

@Composable
fun UpcomingSessionCard(
    session: PushupPlanEntry,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.cardBackground
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Upcoming Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("W${session.week} D${session.day} Col ${session.column}")
            Text("Sets: ${session.sets.joinToString(" / ")}")
            Text("Rest: ${session.rest}")
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.cardBackground
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}