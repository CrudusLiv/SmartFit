// kotlin
package com.example.smartfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

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

    LaunchedEffect(userPreferences.isLoggedIn) {
        if (userPreferences.isLoggedIn && uiState.workoutSuggestions.isEmpty()) {
            viewModel.loadWorkoutSuggestions(limit = 8)
        }
    }

    // Animation state (moved to composable scope)
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    // Calculate progress safely from todayStats (moved to composable scope)
    val progress = remember(todayStats) {
        // prefer explicit "progress" key if available
        (todayStats["progress"] as? Number)?.toFloat()
            ?: run {
                val current = (todayStats["steps"] as? Number)?.toFloat()
                    ?: (todayStats["calories"] as? Number)?.toFloat() ?: 0f
                val goal = (todayStats["stepsGoal"] as? Number)?.toFloat()
                    ?: (todayStats["caloriesGoal"] as? Number)?.toFloat() ?: 1f
                if (goal > 0f) (current / goal).coerceIn(0f, 1f) else 0f
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
                    // Steps Card
                    StatCard(
                        title = "Steps",
                        value = todayStats["Steps"] ?: 0,
                        goal = userPreferences.dailyStepGoal,
                        icon = Icons.Default.DirectionsWalk,
                        color = FitnessGreen,
                        modifier = Modifier
                            .weight(1f)
                            .height(250.dp) // Increased height
                    )

                    // Calories Card
                    StatCard(
                        title = "Calories",
                        value = todayStats["Calories"] ?: 0,
                        goal = userPreferences.dailyCalorieGoal,
                        icon = Icons.Default.LocalFireDepartment,
                        color = FitnessOrange,
                        modifier = Modifier
                            .weight(1f)
                            .height(250.dp) // Increased height
                    )
                }
            }


            @Composable
            fun DonutProgress(
                progress: Float,
                color: Color,
                diameter: Dp = 120.dp,
                strokeWidth: Dp = 8.dp
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress.coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 600)
                )

                Box(modifier = Modifier.size(diameter), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // draw scope: `size` is the canvas size in pixels
                        val strokePx = strokeWidth.toPx()
                        val diameterPx = size.minDimension
                        val arcDiameter = diameterPx - strokePx
                        val topLeft = Offset(
                            x = (size.width - arcDiameter) / 2f,
                            y = (size.height - arcDiameter) / 2f
                        )
                        val arcSize = Size(arcDiameter, arcDiameter)

                        // Background track
                        drawArc(
                            color = color.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )

                        // Foreground progress arc
                        drawArc(
                            color = color,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // ------------------------
// 🟣 Updated StatCard
// ------------------------
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
                val pctText = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%"

                Card(
                    modifier = modifier

                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Title + Icon
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
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 🌀 Donut Progress Circle
                        Box(contentAlignment = Alignment.Center) {
                            DonutProgress(progress = progress, color = color, diameter = 120.dp, strokeWidth = 10.dp)
                            Text(
                                text = pctText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Goal text
                        Text(
                            text = "Goal: $goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
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
                progress = progress,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkoutSuggestionCard(suggestion: WorkoutSuggestion) {
    val displayName = suggestion.name.takeIf { it.isNotBlank() }
        ?: suggestion.category.takeIf { it.isNotBlank() }
        ?: "Workout Idea"
    val focus = suggestion.primaryMuscles.firstOrNull()?.takeIf { it.isNotBlank() }
    val metrics = buildWorkoutMetrics(suggestion)
    val tags = buildList {
        addAll(suggestion.primaryMuscles)
        addAll(suggestion.equipment)
    }
        .filter { it.isNotBlank() }
        .distinct()
        .take(4)

    Card(
        modifier = Modifier
            .width(230.dp)
            .semantics { contentDescription = "$displayName workout suggestion" },
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

                if (suggestion.description.isNotBlank()) {
                    Text(
                        suggestion.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
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

                if (tags.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { label ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            )
                        }
                    }
                }
            }
        }
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
