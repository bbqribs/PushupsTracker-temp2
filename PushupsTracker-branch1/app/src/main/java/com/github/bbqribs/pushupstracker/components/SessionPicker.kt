// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/components/SessionPicker.kt
package com.github.bbqribs.pushupstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.bbqribs.pushupstracker.HomeViewModel
import com.github.bbqribs.pushupstracker.repository.PlanRepository
import com.github.bbqribs.pushupstracker.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionPicker(
    vm: HomeViewModel,
    onGo: (Int, Int, String) -> Unit
) {
    val weekOptions = (1..6).map { it.toString() }
    val dayOptions = (1..3).map { it.toString() }
    val columnOptions = (1..3).map { it.toString() }

    var selectedWeek by remember { mutableStateOf(weekOptions[0]) }
    var selectedDay by remember { mutableStateOf(dayOptions[0]) }
    var selectedColumn by remember { mutableStateOf(columnOptions[0]) }

    val planRepository: PlanRepository = vm.planRepository
    val selectedPlan = remember(selectedWeek, selectedDay, selectedColumn) {
        planRepository.findPlanEntry(selectedWeek.toInt(), selectedDay.toInt(), selectedColumn)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.cardBackground
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Session Picker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PickerDropdown(
                    label = "Week",
                    options = weekOptions,
                    selected = selectedWeek,
                    onSelected = { selectedWeek = it },
                    modifier = Modifier.weight(1f)
                )
                PickerDropdown(
                    label = "Day",
                    options = dayOptions,
                    selected = selectedDay,
                    onSelected = { selectedDay = it },
                    modifier = Modifier.weight(1f)
                )
                PickerDropdown(
                    label = "Col",
                    options = columnOptions,
                    selected = selectedColumn,
                    onSelected = { selectedColumn = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedPlan?.sets?.joinToString(" / ") ?: "No plan found",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Button(
                    onClick = {
                        onGo(
                            selectedWeek.toInt(),
                            selectedDay.toInt(),
                            selectedColumn
                        )
                    }
                ) {
                    Text("Start")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}