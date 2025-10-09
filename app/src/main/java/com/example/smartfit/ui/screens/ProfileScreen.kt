package com.example.smartfit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartfit.viewmodel.ActivityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ActivityViewModel,
    onNavigateBack: () -> Unit
) {
    val userPreferences by viewModel.userPreferencesState.collectAsState()

    var userName by remember { mutableStateOf("") }
    var userWeight by remember { mutableStateOf("") }
    var userHeight by remember { mutableStateOf("") }
    var dailyStepGoal by remember { mutableStateOf("") }
    var dailyCalorieGoal by remember { mutableStateOf("") }

    // Initialize values from preferences
    LaunchedEffect(userPreferences) {
        userName = userPreferences.userName
        userWeight = if (userPreferences.userWeight > 0) userPreferences.userWeight.toString() else ""
        userHeight = if (userPreferences.userHeight > 0) userPreferences.userHeight.toString() else ""
        dailyStepGoal = userPreferences.dailyStepGoal.toString()
        dailyCalorieGoal = userPreferences.dailyCalorieGoal.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Profile Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Personal Information",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Name input field" }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = userWeight,
                            onValueChange = { userWeight = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Weight input field" }
                        )

                        OutlinedTextField(
                            value = userHeight,
                            onValueChange = { userHeight = it },
                            label = { Text("Height (cm)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Height input field" }
                        )
                    }

                    Button(
                        onClick = {
                            if (userName.isNotEmpty()) viewModel.setUserName(userName)
                            userWeight.toFloatOrNull()?.let { viewModel.setUserWeight(it) }
                            userHeight.toFloatOrNull()?.let { viewModel.setUserHeight(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile")
                    }
                }
            }

            // Goals Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Daily Goals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = dailyStepGoal,
                        onValueChange = { dailyStepGoal = it },
                        label = { Text("Daily Step Goal") },
                        leadingIcon = { Icon(Icons.Default.DirectionsWalk, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("steps") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Daily step goal input" }
                    )

                    OutlinedTextField(
                        value = dailyCalorieGoal,
                        onValueChange = { dailyCalorieGoal = it },
                        label = { Text("Daily Calorie Goal") },
                        leadingIcon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("kcal") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Daily calorie goal input" }
                    )

                    Button(
                        onClick = {
                            dailyStepGoal.toIntOrNull()?.let { viewModel.setDailyStepGoal(it) }
                            dailyCalorieGoal.toIntOrNull()?.let { viewModel.setDailyCalorieGoal(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Goals")
                    }
                }
            }

            // Theme Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Appearance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "Dark theme toggle, currently ${if (userPreferences.darkTheme) "enabled" else "disabled"}"
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (userPreferences.darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null
                            )
                            Column {
                                Text(
                                    "Dark Theme",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (userPreferences.darkTheme) "Enabled" else "Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = userPreferences.darkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) }
                        )
                    }
                }
            }

            // App Info Section
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "About",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    InfoRow("Version", "1.0.0")
                    InfoRow("Build", "Debug")

                    Divider()

                    Text(
                        "SmartFit helps you track your daily activities, monitor your progress, and achieve your fitness goals.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

