package com.example.smartfit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    onUseEmailLogin: () -> Unit,
    onProfileSelected: (StoredProfile) -> Unit
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
                        text = "Who's getting active today?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pick your SmartFit profile to jump right back into your routine.",
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
                            onClick = { onProfileSelected(profile) }
                        )
                    }
                    item {
                        AddProfileTile(onClick = onUseEmailLogin)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (sortedProfiles.isEmpty()) {
                        Text(
                            text = "Create your first SmartFit profile to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    OutlinedButton(onClick = onUseEmailLogin) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with email")
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
                text = "Add profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Create a new SmartFit identity",
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

private val EMAIL_ADDRESS_REGEX =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")

private fun isLikelyEmail(raw: String): Boolean {
    if (raw.isBlank()) return false
    return EMAIL_ADDRESS_REGEX.matches(raw.trim())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ActivityViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.userPreferencesState.collectAsState()

    var showEmailLogin by rememberSaveable { mutableStateOf(false) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val hasStoredProfiles = preferences.savedProfiles.isNotEmpty()

    LaunchedEffect(showEmailLogin) {
        if (!showEmailLogin) {
            attemptedSubmit = false
        }
    }

    if (!showEmailLogin && hasStoredProfiles) {
        ProfilePickerScreen(
            profiles = preferences.savedProfiles,
            activeProfileId = preferences.activeProfileId,
            isLoggedIn = preferences.isLoggedIn,
            isLoading = uiState.authLoading,
            errorMessage = uiState.authError,
            onProfileActivated = onLoginSuccess,
            onUseEmailLogin = {
                viewModel.clearError()
                attemptedSubmit = false
                showEmailLogin = true
            },
            onProfileSelected = { profile -> viewModel.loginWithProfile(profile.id) }
        )
    } else {
        var email by rememberSaveable { mutableStateOf("") }
        var password by rememberSaveable { mutableStateOf("") }
        var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
        val passwordFocusRequester = remember { FocusRequester() }

        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val emailIsValid = isLikelyEmail(trimmedEmail)
        val passwordIsValid = trimmedPassword.length >= 4
        val emailError = attemptedSubmit && !emailIsValid
        val passwordError = attemptedSubmit && !passwordIsValid

        val submitLogin: () -> Unit = {
            attemptedSubmit = true
            if (emailIsValid && passwordIsValid) {
                viewModel.login(trimmedEmail, trimmedPassword)
            } else {
                viewModel.clearError()
            }
        }

        LaunchedEffect(preferences.isLoggedIn) {
            if (preferences.isLoggedIn) {
                onLoginSuccess()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Welcome to SmartFit") },
                    navigationIcon = {
                        if (hasStoredProfiles) {
                            @androidx.compose.runtime.Composable {
                                IconButton(
                                    onClick = {
                                        viewModel.clearError()
                                        attemptedSubmit = false
                                        showEmailLogin = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back to profiles"
                                    )
                                }
                            }
                        } else null
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Stay on top of your health journey",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (attemptedSubmit) attemptedSubmit = false
                            if (uiState.authError != null) viewModel.clearError()
                        },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        isError = emailError,
                        supportingText = {
                            if (emailError) {
                                Text("Enter a valid email address.")
                            }
                        },
                        enabled = !uiState.authLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Email input" }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (attemptedSubmit) attemptedSubmit = false
                            if (uiState.authError != null) viewModel.clearError()
                        },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val toggleIcon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            val toggleDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible }
                            ) {
                                Icon(toggleIcon, contentDescription = toggleDescription)
                            }
                        },
                        isError = passwordError,
                        supportingText = {
                            if (passwordError) {
                                Text("Password must be at least 4 characters.")
                            }
                        },
                        enabled = !uiState.authLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { submitLogin() }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester)
                            .semantics { contentDescription = "Password input" }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(
                        visible = uiState.authError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Authentication error" },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Text(
                                text = uiState.authError.orEmpty(),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = submitLogin,
                        enabled = !uiState.authLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Login button" }
                    ) {
                        if (uiState.authLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Log In")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            email = "demo@smartfit.app"
                            password = "demo1234"
                            attemptedSubmit = false
                            viewModel.clearError()
                        },
                        enabled = !uiState.authLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Fill demo credentials" }
                    ) {
                        Text("Fill demo credentials")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tip: any non-empty credentials will sign you in for demo purposes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                if (uiState.authLoading) {
                    LoadingScrim()
                }
            }
        }
    }
}