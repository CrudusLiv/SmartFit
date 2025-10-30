package com.example.smartfit.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                text = { Text("Add Activity") }
            )
        },
        containerColor = Color(0xFF101820) // Dark modern background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dashboard",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                    }
                }
            }

            // WELCOME CARD
            item {
                WelcomeCardModern(userName = userPreferences.userName.ifEmpty { "User" })
            }

            // STATS
            item {
                Text(
                    "Today's Progress",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernStatCard(
                        title = "Steps",
                        value = todayStats["Steps"] ?: 0,
                        goal = userPreferences.dailyStepGoal,
                        icon = Icons.Default.DirectionsWalk,
                        color = FitnessGreen
                    )
                    ModernStatCard(
                        title = "Calories",
                        value = todayStats["Calories"] ?: 0,
                        goal = userPreferences.dailyCalorieGoal,
                        icon = Icons.Default.LocalFireDepartment,
                        color = FitnessOrange
                    )
                }
            }

            // QUICK ACTIONS
            item {
                Text(
                    "Quick Actions",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        QuickActionModern("Activity Log", Icons.Default.List, FitnessBlue, onNavigateToActivityLog)
                    }
                    item {
                        QuickActionModern("Exercises", Icons.Default.FitnessCenter, FitnessPurple, onNavigateToExercises)
                    }
                    item {
                        QuickActionModern("Profile", Icons.Default.Person, FitnessGreen, onNavigateToProfile)
                    }
                }
            }

            // WORKOUT SUGGESTIONS
            item {
                Text(
                    "Workout Suggestions",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.suggestionsLoading && uiState.workoutSuggestions.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = FitnessBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Fetching workouts…", color = Color.White)
                    }
                }
            } else if (uiState.workoutSuggestions.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.workoutSuggestions, key = { it.id }) { suggestion ->
                            WorkoutCardModern(suggestion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCardModern(userName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Welcome back, $userName!",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text("Let's achieve your fitness goals today!", color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ModernStatCard(title: String, value: Int, goal: Int, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    val progress = if (goal > 0) (value.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    Row{
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38)),
            shape = RoundedCornerShape(12.dp)
        )
        {
        }
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(value.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.headlineSmall.fontSize)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = color)
            Text("Goal: $goal", color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
        }
    }
}

@Composable
fun QuickActionModern(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun WorkoutCardModern(suggestion: WorkoutSuggestion) {
    Card(
        modifier = Modifier.width(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A38)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!suggestion.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = suggestion.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            Text(suggestion.name, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(suggestion.category, color = Color(0xFF1E88E5), fontWeight = FontWeight.Medium)
            if (suggestion.primaryMuscles.isNotEmpty()) {
                Text("Focus: ${suggestion.primaryMuscles.joinToString()}", color = Color.Gray, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }
    }
}
