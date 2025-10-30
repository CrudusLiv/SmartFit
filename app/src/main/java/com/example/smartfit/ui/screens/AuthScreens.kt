package com.example.smartfit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smartfit.data.datastore.StoredProfile
import com.example.smartfit.viewmodel.ActivityViewModel
import kotlin.math.absoluteValue

private const val PROFILE_COLUMNS_MIN_WIDTH = 140

@Composable
fun ProfilePickerScreen(
    profiles: List<StoredProfile>,
    activeProfileId: String?,
    isLoggedIn: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onProfileActivated: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onDemoLogin: () -> Unit
) {
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onProfileActivated()
        }
    }

    val sortedProfiles = remember(profiles, activeProfileId) {
        if (activeProfileId.isNullOrBlank()) profiles
        else profiles.sortedByDescending { it.id == activeProfileId }
    }
    val hasProfiles = sortedProfiles.isNotEmpty()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (hasProfiles) "Who's getting active today?" else "Connect SmartFit to Google Fit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (hasProfiles) {
                            "Pick your SmartFit profile to jump right back into your routine."
                        } else {
                            "Sign in with Google Fit to unlock personalized activity insights."
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(PROFILE_COLUMNS_MIN_WIDTH.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sortedProfiles, key = { it.id }) { profile ->
                        ProfileTile(
                            profile = profile,
                            isActive = profile.id == activeProfileId,
                            onClick = onGoogleSignIn
                        )
                    }
                    item {
                        AddProfileTile(onClick = onGoogleSignIn)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Continue with Google Fit to sync your fitness data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onGoogleSignIn,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google Fit")
                    }

                    OutlinedButton(
                        onClick = onDemoLogin,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Try SmartFit demo (no Google Fit)")
                    }

                    if (sortedProfiles.isEmpty()) {
                        Text(
                            text = "Sign in to create your SmartFit profile and start tracking.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Demo mode uses sample data and will not sync with your Google Fit account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (isLoading) {
                LoadingScrim()
            }
        }
    }
}

@Composable
private fun ProfileTile(
    profile: StoredProfile,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val initials = remember(profile.displayName) { profileInitials(profile.displayName) }
    val gradient = remember(profile.id) { profileGradient(profile.id) }
    Column(
        modifier = Modifier
            .semantics { contentDescription = "${profile.displayName} profile" }
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradient))
                .border(
                    BorderStroke(
                        width = if (isActive) 3.dp else 0.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        profile.email?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isActive) {
            Text(
                text = "Current",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun profileInitials(name: String): String {
    val parts = name.split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(2)
    if (parts.isEmpty()) return name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    return parts.joinToString(separator = "") { part ->
        part.firstOrNull()?.uppercaseChar()?.toString() ?: ""
    }
}

private fun profileGradient(seed: String): List<Color> {
    val baseHue = (seed.hashCode().absoluteValue % 360).toFloat()
    val secondaryHue = (baseHue + 40f) % 360f
    val start = Color.hsl(baseHue, 0.65f, 0.52f)
    val end = Color.hsl(secondaryHue, 0.75f, 0.42f)
    return listOf(start, end)
}

@Composable
private fun AddProfileTile(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Add account",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Use a different Google account",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingScrim() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SplashScreen(
    viewModel: ActivityViewModel,
    onNavigationResolved: (Boolean) -> Unit
) {
    val preferences by viewModel.userPreferencesState.collectAsState()

    LaunchedEffect(preferences.isLoggedIn) {
        onNavigationResolved(preferences.isLoggedIn)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Preparing your SmartFit dashboard…",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LoginScreen(
    viewModel: ActivityViewModel,
    onLoginSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.userPreferencesState.collectAsState()

    LaunchedEffect(preferences.isLoggedIn) {
        if (preferences.isLoggedIn) {
            onLoginSuccess()
        }
    }

    val triggerGoogleSignIn: () -> Unit = {
        viewModel.clearError()
        viewModel.beginGoogleSignIn()
        onGoogleSignIn()
    }

    ProfilePickerScreen(
        profiles = preferences.savedProfiles,
        activeProfileId = preferences.activeProfileId,
        isLoggedIn = preferences.isLoggedIn,
        isLoading = uiState.authLoading,
        errorMessage = uiState.authError,
        onProfileActivated = onLoginSuccess,
        onGoogleSignIn = triggerGoogleSignIn,
        onDemoLogin = {
            viewModel.clearError()
            viewModel.loginAsDemo()
        }
    )
}