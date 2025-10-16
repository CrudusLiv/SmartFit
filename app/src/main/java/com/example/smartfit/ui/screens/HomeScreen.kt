package com.example.smartfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.ui.theme.*
import com.example.smartfit.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ActivityViewModel,
    onNavigateToActivityLog: () -> Unit,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToExercises: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferencesState.collectAsState()
    val todayStats by viewModel.getTodayStats().collectAsState(initial = emptyMap())

    LaunchedEffect(Unit) {
        if (uiState.workoutSuggestions.isEmpty()) {
            viewModel.loadWorkoutSuggestions(limit = 8)
        }
    }

    // Animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddActivity,
                icon = { Icon(Icons.Default.Add, "Add activity") },
                text = { Text("Add Activity") },
                modifier = Modifier.semantics { contentDescription = "Add new activity button" }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Page header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                }
            }

            // Welcome message
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically()
                ) {
                    WelcomeCard(userName = userPreferences.userName.ifEmpty { "User" })
                }
            }

            // Today's stats
            item {
                Text(
                    "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Steps",
                        value = todayStats["Steps"] ?: 0,
                        goal = userPreferences.dailyStepGoal,
                        icon = Icons.Default.DirectionsWalk,
                        color = FitnessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Calories",
                        value = todayStats["Calories"] ?: 0,
                        goal = userPreferences.dailyCalorieGoal,
                        icon = Icons.Default.LocalFireDepartment,
                        color = FitnessOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionCard(
                            title = "Activity Log",
                            icon = Icons.Default.List,
                            color = FitnessBlue,
                            onClick = onNavigateToActivityLog
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Exercises",
                            icon = Icons.Default.FitnessCenter,
                            color = FitnessPurple,
                            onClick = onNavigateToExercises
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Profile",
                            icon = Icons.Default.Person,
                            color = FitnessGreen,
                            onClick = onNavigateToProfile
                        )
                    }
                }
            }

            // Workout suggestions from network
            item {
                Text(
                    "Workout Suggestions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                uiState.suggestionsLoading && uiState.workoutSuggestions.isEmpty() -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(12.dp))
                            Text("Fetching workouts…")
                        }
                    }
                }

                uiState.suggestionsError != null && uiState.workoutSuggestions.isEmpty() -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Couldn't load suggestions",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(uiState.suggestionsError ?: "Unknown error")
                            Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 8) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.workoutSuggestions.isNotEmpty() -> {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.workoutSuggestions, key = { it.id }) { suggestion ->
                                WorkoutSuggestionCard(suggestion)
                            }
                        }
                    }
                }
            }

            // Recent activities
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Activities",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToActivityLog) {
                        Text("See All")
                    }
                }
            }

            if (uiState.activities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "No activities yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Start tracking your fitness journey!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.activities.take(3)) { activity ->
                    RecentActivityItem(activity)
                }
            }

            // Tips section
            if (uiState.tips.isNotEmpty()) {
                item {
                    Text(
                        "Fitness Tips",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.tips.take(2)) { tip ->
                    TipCard(tip.title, tip.body)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Welcome card" },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Welcome back, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Let's achieve your fitness goals today!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: Int,
    goal: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier.semantics { contentDescription = "$title stat card" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = color,
            )

            Text(
                "Goal: $goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(140.dp)
            .semantics { contentDescription = "$title quick action" }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        .size(32.dp)
                )
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RecentActivityItem(activity: com.example.smartfit.data.local.ActivityEntity) {
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.type,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${activity.value}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
fun TipCard(title: String, body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Fitness tip" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = FitnessOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun WorkoutSuggestionCard(suggestion: WorkoutSuggestion) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .semantics { contentDescription = "${suggestion.name} workout suggestion" }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!suggestion.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = suggestion.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            Text(
                suggestion.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                suggestion.category,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (suggestion.primaryMuscles.isNotEmpty()) {
                Text(
                    "Focus: ${suggestion.primaryMuscles.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (suggestion.description.isNotBlank()) {
                Text(
                    suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (suggestion.equipment.isNotEmpty()) {
                Text(
                    "Equipment: ${suggestion.equipment.joinToString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

