// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/test/TestScreen.kt
package com.github.bbqribs.pushupstracker.ui.test

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestScreen(
    vm: TestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var count by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Shared logic for logging the test and navigating back
    val onLogTest = {
        val pushupCount = count.toIntOrNull() ?: 0
        if (pushupCount > 0) {
            vm.logTest(pushupCount)
            onNavigateBack()
        }
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Test Attempt") },
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
            Text(
                "Enter the maximum number of pushups you can do in a single, unbroken set.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = count,
                onValueChange = { count = it },
                label = { Text("Total Pushups") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLogTest() })
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onLogTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Test")
            }
        }
    }
}
