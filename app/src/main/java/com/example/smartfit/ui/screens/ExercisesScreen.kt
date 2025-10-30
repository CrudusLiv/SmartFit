package com.example.smartfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisesScreen(
    viewModel: ActivityViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (!uiState.suggestionsLoading && uiState.workoutSuggestions.isEmpty() && uiState.suggestionsError == null) {
            viewModel.loadWorkoutSuggestions(limit = 20)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
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
                        Text("Loading fresh ideas…")
                    }
                }
                uiState.suggestionsError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("We couldn't load the exercises", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.suggestionsError ?: "Unknown error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20) }) { Text("Try again") }
                    }
                }
                uiState.workoutSuggestions.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No exercises yet")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20) }) { Text("Load exercises") }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    "Workout Library",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${uiState.workoutSuggestions.size} curated moves to inspire your next session.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(uiState.workoutSuggestions, key = { it.id }) { suggestion ->
                            ExerciseCard(suggestion)
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(onClick = { viewModel.loadWorkoutSuggestions(limit = 20) }) {
                                    Text("Refresh list")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseCard(suggestion: WorkoutSuggestion) {
    val displayName = suggestion.name.takeIf { it.isNotBlank() }
        ?: suggestion.category.takeIf { it.isNotBlank() }
        ?: "Exercise #${suggestion.id}"

    val secondaryLabel = suggestion.category.takeIf { it.isNotBlank() }
        ?: suggestion.primaryMuscles.firstOrNull()?.takeIf { it.isNotBlank() }
    val equipmentLabel = suggestion.equipment.firstOrNull()?.takeUnless { it.equals("Bodyweight", ignoreCase = true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            ExerciseMediaHeader(
                title = displayName,
                category = suggestion.category,
                imageUrl = suggestion.imageUrl,
                primaryMuscle = suggestion.primaryMuscles.firstOrNull()
            )

            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                secondaryLabel?.let {
                    Spacer(Modifier.height(6.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text(it) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                equipmentLabel?.let {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (suggestion.description.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        suggestion.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val muscleTags = suggestion.primaryMuscles
                    .filter { it.isNotBlank() }
                    .distinct()
                val equipmentTags = suggestion.equipment
                    .filter { it.isNotBlank() }
                    .distinct()

                if (muscleTags.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Primary focus",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        muscleTags.take(6).forEach { label ->
                            TagChip(label)
                        }
                    }
                }

                if (equipmentTags.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Equipment",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        equipmentTags.take(6).forEach { label ->
                            TagChip(label)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseMediaHeader(
    title: String,
    category: String,
    imageUrl: String?,
    primaryMuscle: String?
) {
    val overlayGradient = Brush.verticalGradient(
        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        startY = 0f,
        endY = 340f
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
            )
            Box(modifier = Modifier.matchParentSize().background(overlayGradient))
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text(category.ifBlank { "Training" }) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
            primaryMuscle?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    AssistChip(
        onClick = {},
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}
