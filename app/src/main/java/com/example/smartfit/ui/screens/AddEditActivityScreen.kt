package com.example.smartfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.viewmodel.ActivityViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditActivityScreen(
    viewModel: ActivityViewModel,
    activityId: Long,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditMode = activityId != -1L

    var activityType by remember { mutableStateOf("Steps") }
    var value by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("steps") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Load activity if editing
    LaunchedEffect(activityId) {
        if (isEditMode) {
            scope.launch {
                viewModel.uiState.collect { state ->
                    state.activities.find { it.id == activityId }?.let { activity ->
                        activityType = activity.type
                        value = activity.value.toString()
                        unit = activity.unit
                        notes = activity.notes
                        selectedDate = activity.date
                    }
                }
            }
        }
    }

    // Update unit based on activity type
    LaunchedEffect(activityType) {
        unit = when (activityType) {
            "Steps" -> "steps"
            "Workout" -> "minutes"
            "Calories" -> "kcal"
            else -> "units"
        }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Activity" else "Add Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val valueInt = value.toIntOrNull() ?: 0
                            if (valueInt > 0) {
                                val activity = ActivityEntity(
                                    id = if (isEditMode) activityId else 0,
                                    type = activityType,
                                    value = valueInt,
                                    unit = unit,
                                    date = selectedDate,
                                    notes = notes
                                )

                                if (isEditMode) {
                                    viewModel.updateActivity(activity)
                                } else {
                                    viewModel.addActivity(activity)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = value.toIntOrNull() != null && value.toIntOrNull()!! > 0
                    ) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activity Type Selection
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Activity Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = activityType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .semantics { contentDescription = "Activity type selector" },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("Steps", "Workout", "Calories").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        activityType = type
                                        expanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            getActivityIcon(type),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Value Input
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Value",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Enter $unit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Value input field" },
                        suffix = { Text(unit) },
                        isError = value.isNotEmpty() && value.toIntOrNull() == null
                    )
                }
            }

            // Date and Time
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Date & Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(Date(selectedDate)))
                        }

                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeFormat.format(Date(selectedDate)))
                        }
                    }
                }
            }

            // Notes
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Notes (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Add notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .semantics { contentDescription = "Notes input field" },
                        maxLines = 4
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    val valueInt = value.toIntOrNull() ?: 0
                    if (valueInt > 0) {
                        val activity = ActivityEntity(
                            id = if (isEditMode) activityId else 0,
                            type = activityType,
                            value = valueInt,
                            unit = unit,
                            date = selectedDate,
                            notes = notes
                        )

                        if (isEditMode) {
                            viewModel.updateActivity(activity)
                        } else {
                            viewModel.addActivity(activity)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Save activity button" },
                enabled = value.toIntOrNull() != null && value.toIntOrNull()!! > 0
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isEditMode) "Update Activity" else "Add Activity",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = selectedDate
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(Calendar.MINUTE)

                            calendar.timeInMillis = millis
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            selectedDate = calendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.timeInMillis = selectedDate
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        selectedDate = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

fun getActivityIcon(type: String): ImageVector {
    return when (type) {
        "Steps" -> Icons.Default.DirectionsRun
        "Workout" -> Icons.Default.FitnessCenter
        "Calories" -> Icons.Default.Whatshot
        else -> Icons.Default.SportsScore
    }
}
