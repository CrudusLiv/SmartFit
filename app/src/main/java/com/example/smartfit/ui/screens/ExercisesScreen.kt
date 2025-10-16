package com.example.smartfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                        Text("Loading from Wger…")
                    }
                }
                uiState.suggestionsError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Failed to load exercises", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(uiState.suggestionsError ?: "Unknown error")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20) }) { Text("Retry") }
                    }
                }
                uiState.workoutSuggestions.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No exercises returned")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadWorkoutSuggestions(limit = 20) }) { Text("Load") }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "Total: ${uiState.workoutSuggestions.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(uiState.workoutSuggestions, key = { it.id }) { suggestion ->
                            ExerciseRow(suggestion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRow(suggestion: WorkoutSuggestion) {
    Card {
        Column(Modifier.padding(12.dp)) {
            Text(suggestion.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(suggestion.category, style = MaterialTheme.typography.labelMedium)
            if (!suggestion.imageUrl.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = suggestion.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
            if (suggestion.primaryMuscles.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("Focus: ${suggestion.primaryMuscles.joinToString()}", style = MaterialTheme.typography.bodySmall)
            }
            if (suggestion.equipment.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Equipment: ${suggestion.equipment.joinToString()}", style = MaterialTheme.typography.bodySmall)
            }
            if (suggestion.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(suggestion.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
