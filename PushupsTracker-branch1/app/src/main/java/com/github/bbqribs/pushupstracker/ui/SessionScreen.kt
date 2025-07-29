// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/SessionScreen.kt
package com.github.bbqribs.pushupstracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    // The ViewModel is now provided by Hilt
    vm: SessionProgressViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Session") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                // State 1: Session is complete, show summary
                uiState.isSessionComplete -> {
                    SessionSummary(results = uiState.results, onDone = onNavigateBack)
                }
                // State 2: Rest timer is active
                uiState.restSecondsLeft != null -> {
                    RestUI(
                        secondsLeft = uiState.restSecondsLeft!!,
                        nextSetTarget = vm.setStrings.getOrNull(uiState.currentIndex + 1) ?: "Final Set",
                        onSkip = { vm.skipRest() }
                    )
                }
                // State 3: Active set (final or in-progress)
                else -> {
                    ActiveSetUI(
                        isFinalSet = uiState.currentIndex == vm.setStrings.lastIndex,
                        setNumber = uiState.currentIndex + 1,
                        totalSets = vm.setStrings.size,
                        targetReps = uiState.targetReps,
                        onCompleteSet = { vm.completeSet() },
                        onSubmitFinalSet = { reps ->
                            vm.submitFinalSet(reps)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RestUI(secondsLeft: Int, nextSetTarget: String, onSkip: () -> Unit) {
    Text("REST", style = MaterialTheme.typography.labelLarge)
    Text("$secondsLeft", style = MaterialTheme.typography.displayLarge)
    Spacer(Modifier.height(16.dp))
    Text("Next up: $nextSetTarget pushups", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    OutlinedButton(onClick = onSkip) {
        Text("Skip Rest")
    }
}

@Composable
private fun ActiveSetUI(
    isFinalSet: Boolean,
    setNumber: Int,
    totalSets: Int,
    targetReps: Int,
    onCompleteSet: () -> Unit,
    onSubmitFinalSet: (Int) -> Unit
) {
    var finalInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Text("Set $setNumber of $totalSets", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
        text = if (isFinalSet) "At least $targetReps reps" else "$targetReps reps",
        style = MaterialTheme.typography.displayMedium
    )
    Spacer(Modifier.height(24.dp))

    if (isFinalSet) {
        OutlinedTextField(
            value = finalInput,
            onValueChange = { finalInput = it },
            label = { Text("Actual reps completed") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(onDone = {
                finalInput.toIntOrNull()?.let { onSubmitFinalSet(it) }
                focusManager.clearFocus()
            })
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                finalInput.toIntOrNull()?.let { onSubmitFinalSet(it) }
                focusManager.clearFocus()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Final Set")
        }
    } else {
        Button(onClick = onCompleteSet, modifier = Modifier.fillMaxWidth()) {
            Text("Complete Set")
        }
    }
}

@Composable
private fun SessionSummary(results: List<Pair<Int, Int>>, onDone: () -> Unit) {
    val total = results.sumOf { it.first }
    val success = results.all { (act, rec) -> act >= rec }
    val outcome = if (success) "SUCCESS" else "PARTIAL / INCOMPLETE"

    Text("Session Complete!", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Total Pushups: $total\nOutcome: $outcome",
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text("Done")
    }
}
