// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/editlog/EditLogScreen.kt
package com.github.bbqribs.pushupstracker.ui.editlog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Upload // ✅ Import for the new icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.bbqribs.pushupstracker.data.Attempt
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLogScreen(
    vm: EditLogViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit // ✅ Add the new navigation callback
) {
    val recentAttempts by vm.recentAttempts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recent Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        // ✅ Add a Floating Action Button for the import action
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToImport) {
                Icon(Icons.Filled.Upload, contentDescription = "Import Log")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recentAttempts, key = { it.id }) { attempt ->
                AttemptLogItem(
                    attempt = attempt,
                    onDelete = { vm.deleteAttempt(attempt) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun AttemptLogItem(
    attempt: Attempt,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val date = remember(attempt.timestamp) {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(attempt.timestamp))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        // ✅ Apply themed background color
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(date, style = MaterialTheme.typography.bodySmall)
                val title = if (attempt.outcome == "TEST") {
                    "TEST: ${attempt.setsCompleted} reps"
                } else {
                    "W${attempt.week} D${attempt.day} C${attempt.column} - ${attempt.outcome}"
                }
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete attempt")
            }
        }
    }
}