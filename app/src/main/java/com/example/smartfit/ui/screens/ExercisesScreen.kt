package com.example.smartfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.viewmodel.ActivityViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ExercisesScreen(
    viewModel: ActivityViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSuggestion by remember { mutableStateOf<WorkoutSuggestion?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val suggestionLimit = 40

    LaunchedEffect(Unit) {
        if (
            !uiState.suggestionsLoading &&
            uiState.suggestionsError == null &&
            uiState.workoutSuggestions.size < suggestionLimit
        ) {
            viewModel.loadWorkoutSuggestions(limit = suggestionLimit)
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.suggestionsLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Loading personalized ideas…")
                    }
                }

                uiState.suggestionsError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "We couldn't load the exercises",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.suggestionsError ?: "Unknown error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20, forceRefresh = true) }) {
                            Text("Try again")
                        }
                    }
                }

                uiState.workoutSuggestions.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No exercises yet")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20, forceRefresh = true) }) {
                            Text("Load exercises")
                        }
                    }
                }

                else -> {
                    val categories = remember(uiState.workoutSuggestions) {
                        buildList {
                            add("All")
                            uiState.workoutSuggestions
                                .mapNotNull { suggestion ->
                                    suggestion.category.takeIf { it.isNotBlank() }
                                }
                                .distinct()
                                .sortedBy { it.lowercase(Locale.getDefault()) }
                                .forEach { add(it) }
                        }
                    }

                    if (selectedCategory !in categories) {
                        selectedCategory = "All"
                    }

                    val filteredSuggestions = remember(uiState.workoutSuggestions, selectedCategory, searchQuery) {
                        val query = searchQuery.trim()
                        uiState.workoutSuggestions.filter { suggestion ->
                            val matchesCategory = selectedCategory == "All" ||
                                suggestion.category.equals(selectedCategory, ignoreCase = true)
                            val searchableFields = listOf(
                                suggestion.name,
                                suggestion.category,
                                suggestion.intensityLabel
                            ) + suggestion.primaryMuscles + suggestion.equipment
                            val matchesQuery = query.isBlank() || searchableFields.any { field ->
                                field.contains(query, ignoreCase = true)
                            }
                            matchesCategory && matchesQuery
                        }
                    }

                    val summaryMetrics = remember(uiState.workoutSuggestions) {
                        buildExerciseSummary(uiState.workoutSuggestions)
                    }
                    val featuredSuggestions = remember(filteredSuggestions) { filteredSuggestions.take(4) }
                    val remainingSuggestions = remember(filteredSuggestions) { filteredSuggestions.drop(4) }
                    val displayedSuggestions = remember(remainingSuggestions) { remainingSuggestions }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            HeaderRow(
                                hasBackAction = onNavigateBack != null,
                                onBack = onNavigateBack
                            )
                        }

                        item {
                            SearchField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                onClear = { searchQuery = "" }
                            )
                        }

                        if (categories.size > 1) {
                            item {
                                CategorySelector(
                                    categories = categories,
                                    selected = selectedCategory,
                                    onSelectedChange = {
                                        selectedCategory = it
                                    }
                                )
                            }
                        }

                        if (summaryMetrics.isNotEmpty()) {
                            item { SummaryRow(metrics = summaryMetrics) }
                        }

                        if (featuredSuggestions.isNotEmpty()) {
                            item {
                                FeaturedCarousel(
                                    suggestions = featuredSuggestions,
                                    onSelect = { selectedSuggestion = it }
                                )
                            }
                        }

                        if (displayedSuggestions.isEmpty()) {
                            item {
                                ExercisesEmptyState(
                                    selectedCategory = selectedCategory,
                                    searchQuery = searchQuery,
                                    onClearFilters = {
                                        selectedCategory = "All"
                                        searchQuery = ""
                                    },
                                    onRefresh = { viewModel.loadWorkoutSuggestions(limit = suggestionLimit, forceRefresh = true) }
                                )
                            }
                        } else {
                            item {
                                EssentialsGrid(
                                    suggestions = displayedSuggestions,
                                    onSelect = { selectedSuggestion = it }
                                )
                            }

                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = { viewModel.loadWorkoutSuggestions(limit = suggestionLimit, forceRefresh = true) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Refresh catalogue")
                                }
                            }
                        }
                    }
                }
            }
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
private fun HeaderRow(hasBackAction: Boolean, onBack: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Training studio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Plan a workout without endless scrolling.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (hasBackAction && onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search by name, focus, or gear") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (value.isNotBlank()) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* filtering handled by state */ })
    )
}

@Composable
private fun CategorySelector(
    categories: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        categories.forEachIndexed { index, label ->
            val isSelected = label == selected
            SegmentedButton(
                selected = isSelected,
                onClick = { onSelectedChange(label) },
                shape = SegmentedButtonDefaults.itemShape(index, categories.size)
            ) {
                Text(label)
            }
        }
    }
}

private data class SummaryMetric(val title: String, val value: String, val icon: ImageVector)

private fun buildExerciseSummary(suggestions: List<WorkoutSuggestion>): List<SummaryMetric> {
    if (suggestions.isEmpty()) return emptyList()

    val total = suggestions.size
    val durations = suggestions.mapNotNull { it.durationMinutes.takeIf { minutes -> minutes > 0 } }
    val avgDuration = durations.takeIf { it.isNotEmpty() }?.average()?.roundToInt()
    val focusCount = suggestions.flatMap { it.primaryMuscles }
        .mapNotNull { it.takeIf(String::isNotBlank) }
        .distinct()
        .size
    val topIntensity = suggestions.mapNotNull { it.intensityLabel.takeIf(String::isNotBlank) }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key

    return buildList {
        add(SummaryMetric("Workouts", total.toString(), Icons.Default.FitnessCenter))
    avgDuration?.let { add(SummaryMetric("Avg duration", "${it} min", Icons.Default.Speed)) }
        if (focusCount > 0) {
            add(SummaryMetric("Focus groups", focusCount.toString(), Icons.Default.AutoAwesome))
        }
        topIntensity?.let {
            add(SummaryMetric("Common focus", it, Icons.Default.Insights))
        }
    }
}

@Composable
private fun SummaryRow(metrics: List<SummaryMetric>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(metrics) { metric ->
            SummaryPill(metric)
        }
    }
}

@Composable
private fun SummaryPill(metric: SummaryMetric) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(metric.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column {
                Text(metric.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(metric.title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FeaturedCarousel(
    suggestions: List<WorkoutSuggestion>,
    onSelect: (WorkoutSuggestion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Featured picks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(suggestions) { suggestion ->
                FeaturedExerciseCard(suggestion = suggestion, onClick = { onSelect(suggestion) })
            }
        }
    }
}

@Composable
private fun FeaturedExerciseCard(
    suggestion: WorkoutSuggestion,
    onClick: () -> Unit
) {
    val visual = categoryVisual(suggestion.category)
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Brush.linearGradient(visual.gradient), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(visual.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                suggestion.name.takeIf { it.isNotBlank() }
                    ?: suggestion.category.ifBlank { "Workout" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            suggestion.intensityLabel.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            val highlights = buildExerciseHighlights(suggestion)
            highlights.take(2).forEach { line ->
                Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Open details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EssentialsGrid(
    suggestions: List<WorkoutSuggestion>,
    onSelect: (WorkoutSuggestion) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        suggestions.chunked(2).forEach { rowSuggestions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowSuggestions.forEach { suggestion ->
                    ExerciseTile(
                        suggestion = suggestion,
                        onClick = { onSelect(suggestion) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowSuggestions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExerciseTile(
    suggestion: WorkoutSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = categoryVisual(suggestion.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Brush.linearGradient(visual.gradient), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(visual.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suggestion.name.takeIf { it.isNotBlank() }
                            ?: suggestion.category.ifBlank { "Workout" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    suggestion.intensityLabel.takeIf { it.isNotBlank() }
                        ?: suggestion.primaryMuscles.firstOrNull()?.takeIf { it.isNotBlank() }
                        ?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                }
            }

            val highlights = buildExerciseHighlights(suggestion)
            highlights.take(3).forEach { highlight ->
                Text(
                    "• $highlight",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "View details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private data class CategoryVisual(val icon: ImageVector, val gradient: List<Color>)

@Composable
private fun categoryVisual(category: String): CategoryVisual {
    val scheme = MaterialTheme.colorScheme
    val lower = category.lowercase(Locale.getDefault())
    return when {
        "strength" in lower -> CategoryVisual(
            icon = Icons.Default.FitnessCenter,
            gradient = listOf(scheme.primary, scheme.primary.copy(alpha = 0.6f))
        )

        "cardio" in lower || "run" in lower -> CategoryVisual(
            icon = Icons.Default.DirectionsRun,
            gradient = listOf(scheme.secondary, scheme.secondary.copy(alpha = 0.6f))
        )

        "mobility" in lower || "yoga" in lower -> CategoryVisual(
            icon = Icons.Default.SelfImprovement,
            gradient = listOf(scheme.tertiary, scheme.tertiary.copy(alpha = 0.6f))
        )

        else -> CategoryVisual(
            icon = Icons.Default.AutoAwesome,
            gradient = listOf(scheme.primary, scheme.secondary)
        )
    }
}

private fun buildExerciseHighlights(suggestion: WorkoutSuggestion): List<String> {
    val highlights = mutableListOf<String>()
    if (suggestion.durationMinutes > 0) {
        highlights += "${suggestion.durationMinutes} min"
    }
    if (suggestion.calories > 0) {
        highlights += "${suggestion.calories} kcal"
    }
    suggestion.primaryMuscles.firstOrNull()?.takeIf { it.isNotBlank() }?.let { highlights += it }
    suggestion.equipment.firstOrNull()?.takeIf { it.isNotBlank() }?.let { highlights += it }
    suggestion.averageHeartRate?.takeIf { it > 0 }?.let { highlights += "Avg HR ${it} bpm" }
    suggestion.steps.takeIf { it > 0 }?.let { highlights += "${suggestion.steps} steps" }
    return highlights
}

@Composable
private fun ExercisesEmptyState(
    selectedCategory: String,
    searchQuery: String,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.FilterAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            val message = when {
                searchQuery.isNotBlank() -> "No workouts match \"$searchQuery\""
                selectedCategory != "All" -> "No workouts tagged $selectedCategory"
                else -> "No workouts available"
            }
            Text(message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(
                text = "Try clearing filters or refreshing the catalogue for more ideas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
                TextButton(onClick = onClearFilters) {
                    Text("Clear filters")
                }
            }
        }
    }
}
