package com.example.smartfit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartfit.ui.navigation.Screen
import com.example.smartfit.data.datastore.UserPreferences
import com.example.smartfit.data.local.SmartFitDatabase
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.google.createGoogleSignInClient
import com.example.smartfit.google.getFitnessOptions
import com.example.smartfit.ui.navigation.NavGraph
import com.example.smartfit.ui.theme.SmartFitTheme
import com.example.smartfit.viewmodel.ActivityViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var pendingPermissionAction: (() -> Unit)? = null
    private var pendingFitnessAccount: GoogleSignInAccount? = null
 
    private val runtimePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results: Map<String, Boolean> ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                pendingPermissionAction?.invoke()
            } else {
                Log.w(TAG, "Required activity recognition permission not granted")
                if (pendingPermissionAction != null) {
                    viewModel.handleGoogleSignInFailure(
                        message = "Activity recognition permission is required to sync with Google Fit."
                    )
                }
            }
            pendingPermissionAction = null
        }

    private val viewModel: ActivityViewModel by lazy {
        val database = SmartFitDatabase.getDatabase(applicationContext)
        val activityDao = database.activityDao()
        val wgerRemoteDataSource = com.example.smartfit.data.remote.WgerRemoteDataSource.create()
        val repository = ActivityRepository(activityDao, wgerRemoteDataSource)
        val userPreferences = UserPreferences(applicationContext)

        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                        return ActivityViewModel(repository, userPreferences) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        )[ActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        googleSignInClient = createGoogleSignInClient(this)
        ensureRuntimePermissions()

        setContent {
            val userPreferences by viewModel.userPreferencesState.collectAsState()
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val bottomNavItems = remember {
                listOf(
                    BottomNavItem(Screen.Home, Icons.Filled.Home, "Home"),
                    BottomNavItem(Screen.ActivityLog, Icons.Filled.List, "Activity"),
                    BottomNavItem(Screen.Exercises, Icons.Filled.FitnessCenter, "Exercises"),
                    BottomNavItem(Screen.Profile, Icons.Filled.Person, "Profile")
                )
            }

            val bottomDestinations = remember(bottomNavItems) { bottomNavItems.map { it.screen.route } }
            val showBottomBar = currentRoute in bottomDestinations

            SmartFitTheme {
                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            SmartFitBottomBar(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onNavigate = { destination ->
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LaunchedEffect(userPreferences.isLoggedIn) {
                            if (userPreferences.isLoggedIn) {
                                ensureRuntimePermissions { ensureGoogleFitAccess() }
                            }
                        }
                        NavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            onRequestGoogleSignIn = ::requestGoogleSignIn,
                            onRequestGoogleSignOut = ::signOutOfGoogle,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        ensureFitnessPermissions(account)
                    } else {
                        viewModel.handleGoogleSignInFailure("Google account was not returned.")
                    }
                } catch (apiException: ApiException) {
                    val statusCode = apiException.statusCode
                    val isCancelled = statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED
                    if (!isCancelled) {
                        Log.e(TAG, "Google sign-in failed", apiException)
                    }
                    viewModel.handleGoogleSignInFailure(
                        message = when (statusCode) {
                            GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> null
                            GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> "Google sign-in is already in progress."
                            else -> apiException.message
                        },
                        isCancelled = isCancelled
                    )
                }
            }

            RC_FIT_PERMISSIONS -> {
                if (resultCode == Activity.RESULT_OK) {
                    val account = pendingFitnessAccount ?: GoogleSignIn.getLastSignedInAccount(this)
                    if (account != null) {
                        onGoogleAccountReady(account)
                    } else {
                        viewModel.handleGoogleSignInFailure("Couldn't find Google account after granting permissions.")
                    }
                } else {
                    Log.w(TAG, "Google Fit permissions denied by user")
                    viewModel.handleGoogleSignInFailure("Google Fit permissions are required to sync activity data.")
                }
                pendingFitnessAccount = null
            }
        }
    }

    private fun ensureRuntimePermissions(onGranted: (() -> Unit)? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            onGranted?.invoke()
            return
        }

        val missing = REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            onGranted?.invoke()
        } else {
            pendingPermissionAction = onGranted
            runtimePermissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun requestGoogleSignIn() {
        viewModel.beginGoogleSignIn()
        ensureRuntimePermissions { ensureGoogleFitAccess() }
    }

    private fun ensureGoogleFitAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null) {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
            return
        }

        ensureFitnessPermissions(account)
    }

    private fun ensureFitnessPermissions(account: GoogleSignInAccount) {
        val fitnessOptions = getFitnessOptions()
        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            onGoogleAccountReady(account)
        } else {
            pendingFitnessAccount = account
            GoogleSignIn.requestPermissions(
                this,
                RC_FIT_PERMISSIONS,
                account,
                fitnessOptions
            )
        }
    }

    private fun onGoogleAccountReady(account: GoogleSignInAccount) {
        pendingFitnessAccount = null
        subscribeToFitnessData(account)
        viewModel.completeGoogleSignIn(
            accountId = account.id,
            displayName = account.displayName,
            email = account.email
        )
    }

    private fun subscribeToFitnessData(account: GoogleSignInAccount) {
        Fitness.getRecordingClient(this, account)
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { Log.d(TAG, "Subscribed to Google Fit step data") }
            .addOnFailureListener { error ->
                Log.e(TAG, "Failed to subscribe to Google Fit step data", error)
            }
    }

    private fun signOutOfGoogle() {
        googleSignInClient.signOut()
        pendingFitnessAccount = null
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 1001
        private const val RC_FIT_PERMISSIONS = 1002
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    }
}

private data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@Composable
private fun SmartFitBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                modifier = Modifier.testTag("nav_${item.screen.route}"),
                selected = selected,
                onClick = { if (!selected) onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
