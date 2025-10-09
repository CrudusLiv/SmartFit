package com.example.smartfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.ui.theme.*
import com.example.smartfit.viewmodel.ActivityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(
    viewModel: ActivityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val weeklyStats by viewModel.getWeeklyStats().collectAsState(initial = emptyMap())
    var showDeleteDialog by remember { mutableStateOf<ActivityEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredActivities = when (selectedFilter) {
        "All" -> uiState.activities
        else -> uiState.activities.filter { it.type == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, "Add activity")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Weekly Summary
            item {
                WeeklySummaryCard(weeklyStats)
            }

            // Filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Steps",
                        onClick = { selectedFilter = "Steps" },
                        label = { Text("Steps") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Workout",
                        onClick = { selectedFilter = "Workout" },
                        label = { Text("Workout") }
                    )
                    FilterChip(
                        selected = selectedFilter == "Calories",
                        onClick = { selectedFilter = "Calories" },
                        label = { Text("Calories") }
                    )
                }
            }

            item {
                Text(
                    "${filteredActivities.size} Activities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (filteredActivities.isEmpty()) {
                item {
                    EmptyStateCard(onAddClick = onNavigateToAdd)
                }
            }

            items(
                items = filteredActivities,
                key = { it.id }
            ) { activity ->
                ActivityItemCard(
                    activity = activity,
                    onEdit = { onNavigateToEdit(activity.id) },
                    onDelete = { showDeleteDialog = activity }
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { activity ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Activity") },
            text = { Text("Are you sure you want to delete this activity?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteActivity(activity)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun WeeklySummaryCard(stats: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Weekly summary card" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "This Week's Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeeklyStat(
                    icon = Icons.Default.DirectionsWalk,
                    value = stats["Steps"] ?: 0,
                    unit = "steps",
                    color = FitnessGreen
                )
                WeeklyStat(
                    icon = Icons.Default.LocalFireDepartment,
                    value = stats["Calories"] ?: 0,
                    unit = "kcal",
                    color = FitnessOrange
                )
                WeeklyStat(
                    icon = Icons.Default.FitnessCenter,
                    value = stats["Workout"] ?: 0,
                    unit = "min",
                    color = FitnessBlue
                )
            }
        }
    }
}

@Composable
fun WeeklyStat(
    icon: ImageVector,
    value: Int,
    unit: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityItemCard(
    activity: ActivityEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val icon = when (activity.type) {
        "Steps" -> Icons.Default.DirectionsRun
        "Workout" -> Icons.Default.FitnessCenter
        "Calories" -> Icons.Default.Whatshot
        else -> Icons.Default.SportsScore
    }
    val color = when (activity.type) {
        "Steps" -> FitnessGreen
        "Workout" -> FitnessBlue
        "Calories" -> FitnessOrange
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${activity.type} activity: ${activity.value} ${activity.unit}"
            },
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = color.copy(alpha = 0.2f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                    )
                }

                Column {
                    Text(
                        activity.type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${activity.value} ${activity.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = color
                    )
                    Text(
                        dateFormat.format(Date(activity.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (activity.notes.isNotEmpty()) {
                        Text(
                            activity.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete activity",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.EventNote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "No activities yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Start tracking your fitness journey!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Activity")
            }
        }
    }
}
