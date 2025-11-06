// kotlin
package com.example.smartfit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import java.util.Locale
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.ui.theme.*
import com.example.smartfit.viewmodel.ActivityViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ActivityViewModel,
    onNavigateToActivityLog: () -> Unit,
    onNavigateToAddActivity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userPreferences by viewModel.userPreferencesState.collectAsState()
    val todayStats by viewModel.getTodayStats().collectAsState(initial = emptyMap())
    var selectedSuggestion by remember { mutableStateOf<WorkoutSuggestion?>(null) }

    val steps = (todayStats["Steps"] as? Number)?.toInt() ?: 0
    val calories = (todayStats["Calories"] as? Number)?.toInt() ?: 0
    val stepsGoal = userPreferences.dailyStepGoal.coerceAtLeast(0)
    val caloriesGoal = userPreferences.dailyCalorieGoal.coerceAtLeast(0)

    val progress = remember(steps, calories, stepsGoal, caloriesGoal) {
        when {
            stepsGoal > 0 -> steps.toFloat() / stepsGoal
            caloriesGoal > 0 -> calories.toFloat() / caloriesGoal
            else -> 0f
        }.coerceIn(0f, 1f)
    }

    LaunchedEffect(userPreferences.isLoggedIn) {
        if (userPreferences.isLoggedIn && uiState.workoutSuggestions.isEmpty()) {
            viewModel.loadWorkoutSuggestions(limit = 8)
        }
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

            item {
                ProgressHeroCard(
                    userName = userPreferences.userName,
                    steps = steps,
                    stepsGoal = stepsGoal,
                    calories = calories,
                    caloriesGoal = caloriesGoal,
                    progress = progress,
                    onQuickLogClick = onNavigateToActivityLog
                )
            }

            // Today's stats
            item {
                Text(
                    "Today's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // ✅ Steps + Calories side by side
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                    )

                    StatCard(
                        title = "Calories",
                        value = todayStats["Calories"] ?: 0,
                        goal = userPreferences.dailyCalorieGoal,
                        icon = Icons.Default.LocalFireDepartment,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                    )
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
                                WorkoutSuggestionCard(
                                    suggestion = suggestion,
                                    onClick = { selectedSuggestion = suggestion }
                                )
                            }
                        }
                    }
                }
                else -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .semantics { contentDescription = "Empty workout suggestions" },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                "No Google Fit sessions yet",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Start a workout with Google Fit to unlock tailored ideas here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
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

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        selectedSuggestion?.let { suggestion ->
            WorkoutSuggestionDetailDialog(
                suggestion = suggestion,
                onDismiss = { selectedSuggestion = null }
            )
        }
    }
}

@Composable
private fun ProgressHeroCard(
    userName: String,
    steps: Int,
    stepsGoal: Int,
    calories: Int,
    caloriesGoal: Int,
    progress: Float,
    onQuickLogClick: () -> Unit
) {
    val displayName = userName.ifBlank { "there" }
    val safeProgress = progress.coerceIn(0f, 1f)
    val progressPercent = (safeProgress * 100).roundToInt()
    val stepsLabel = if (stepsGoal > 0) "$steps/$stepsGoal" else "$steps steps"
    val caloriesLabel = if (caloriesGoal > 0) "$calories/$caloriesGoal kcal" else "$calories kcal"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Daily progress summary" },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Let's move, $displayName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "${progressPercent}% of today's goal is complete.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                DonutProgress(
                    progress = safeProgress,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStat(
                    title = "Steps",
                    value = stepsLabel,
                    modifier = Modifier.weight(1f)
                )
                DetailStat(
                    title = "Calories",
                    value = caloriesLabel,
                    modifier = Modifier.weight(1f)
                )
            }

            FilledTonalButton(
                onClick = onQuickLogClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .semantics { contentDescription = "Log an activity" }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log activity")
            }
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
    val progress = if (goal > 0) value.toFloat() / goal else 0f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Title & Icon Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Circular progress tracker
            DonutProgress(progress = progress, color = color)

            Spacer(modifier = Modifier.height(8.dp))

            // Goal text
            Text(
                text = "Goal: $goal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DonutProgress(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = 1f,
            color = color.copy(alpha = 0.2f),
            strokeWidth = 8.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Foreground (actual progress)
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            color = color,
            strokeWidth = 8.dp,
            modifier = Modifier.fillMaxSize()
        )

        // Percentage text
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
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
        "Steps" -> MaterialTheme.colorScheme.primary
        "Workout" -> MaterialTheme.colorScheme.tertiary
        "Calories" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutSuggestionCard(
    suggestion: WorkoutSuggestion,
    onClick: () -> Unit
) {
    val displayName = suggestion.name.takeIf { it.isNotBlank() }
        ?: suggestion.category.takeIf { it.isNotBlank() }
        ?: "Workout Idea"
    val focus = suggestion.primaryMuscles.firstOrNull()?.takeIf { it.isNotBlank() }
    val metrics = buildWorkoutMetrics(suggestion).take(3)

    Card(
        modifier = Modifier
            .width(230.dp)
            .semantics { contentDescription = "$displayName workout suggestion" }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (!suggestion.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = suggestion.imageUrl,
                        contentDescription = displayName,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                        error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                                    startY = 20f,
                                    endY = 260f
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(suggestion.category.ifBlank { "Training" }) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    focus?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                suggestion.intensityLabel.takeIf { it.isNotBlank() }?.let { intensity ->
                    AssistChip(
                        onClick = {},
                        label = { Text(intensity) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (metrics.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        metrics.forEach { metric ->
                            MetricChip(metric.icon, metric.label)
                        }
                    }
                }

                Text(
                    text = "Tap for full breakdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSuggestionDetailDialog(
    suggestion: WorkoutSuggestion,
    onDismiss: () -> Unit
) {
    val displayName = suggestion.name.takeIf { it.isNotBlank() }
        ?: suggestion.category.takeIf { it.isNotBlank() }
        ?: "Workout Idea"
    val metrics = buildWorkoutMetrics(suggestion)
    val cardioDetails = remember(suggestion) {
        buildList {
            if (suggestion.steps > 0) add("${suggestion.steps} steps")
            suggestion.distanceKm?.takeIf { it > 0.0 }?.let { km ->
                add(String.format(Locale.getDefault(), "%.2f km", km))
            }
            suggestion.averagePaceMinutesPerKm?.takeIf { it > 0 }?.let { pace ->
                add(String.format(Locale.getDefault(), "%.1f min/km", pace))
            }
            suggestion.averageHeartRate?.takeIf { it > 0 }?.let { bpm ->
                add("Avg HR $bpm bpm")
            }
            suggestion.maxHeartRate?.takeIf { it > 0 }?.let { bpm ->
                add("Max HR $bpm bpm")
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    if (!suggestion.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = suggestion.imageUrl,
                            contentDescription = displayName,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                            error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                        )
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(44.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                                    startY = 80f,
                                    endY = 360f
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(suggestion.category.ifBlank { "Training" }) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        suggestion.intensityLabel.takeIf { it.isNotBlank() }?.let { intensity ->
                            AssistChip(
                                onClick = {},
                                label = { Text(intensity) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close details")
                    }
                }

                Divider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        suggestion.description.takeIf { it.isNotBlank() }?.let { body ->
                            Text(
                                body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (metrics.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            metrics.forEach { metric ->
                                MetricChip(metric.icon, metric.label)
                            }
                        }
                    }

                    if (cardioDetails.isNotEmpty()) {
                        DetailSection(title = "Performance", values = cardioDetails)
                    }

                    if (suggestion.primaryMuscles.isNotEmpty()) {
                        DetailSection(title = "Focus areas", values = suggestion.primaryMuscles)
                    }

                    if (suggestion.equipment.isNotEmpty()) {
                        DetailSection(title = "Equipment", values = suggestion.equipment)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailStat(
                            title = "Duration",
                            value = suggestion.durationMinutes.takeIf { it > 0 }?.let { "$it min" } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        DetailStat(
                            title = "Calories",
                            value = suggestion.calories.takeIf { it > 0 }?.let { "$it kcal" } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                        DetailStat(
                            title = "Effort",
                            value = suggestion.effortScore.takeIf { it > 0 }?.let { "$it" } ?: "—",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, values: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        values.filter { it.isNotBlank() }.forEach { value ->
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailStat(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class MetricDisplay(val icon: ImageVector, val label: String)

private fun buildWorkoutMetrics(suggestion: WorkoutSuggestion): List<MetricDisplay> {
    val chips = mutableListOf<MetricDisplay>()
    if (suggestion.durationMinutes > 0) {
        chips += MetricDisplay(Icons.Default.Schedule, "${suggestion.durationMinutes} min")
    }
    if (suggestion.calories > 0) {
        chips += MetricDisplay(Icons.Default.LocalFireDepartment, "${suggestion.calories} kcal")
    }
    suggestion.distanceKm?.takeIf { it > 0.0 }?.let { distance ->
        chips += MetricDisplay(Icons.Default.Map, String.format(Locale.getDefault(), "%.1f km", distance))
    }
    if (suggestion.steps > 0) {
        chips += MetricDisplay(Icons.Default.DirectionsWalk, "${suggestion.steps} steps")
    }
    suggestion.averageHeartRate?.takeIf { it > 0 }?.let { bpm ->
        chips += MetricDisplay(Icons.Default.Favorite, "$bpm bpm")
    }
    suggestion.averagePaceMinutesPerKm?.takeIf { it > 0.0 }?.let { pace ->
        chips += MetricDisplay(Icons.Default.Speed, String.format(Locale.getDefault(), "%.1f min/km", pace))
    }
    if (suggestion.intensityLabel.isNotBlank()) {
        chips += MetricDisplay(Icons.Default.Bolt, suggestion.intensityLabel)
    }
    return chips.take(6)
}

@Composable
private fun MetricChip(icon: ImageVector, label: String) {
    AssistChip(
        onClick = {},
        leadingIcon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        label = {
            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            labelColor = MaterialTheme.colorScheme.primary
        )
    )
}


