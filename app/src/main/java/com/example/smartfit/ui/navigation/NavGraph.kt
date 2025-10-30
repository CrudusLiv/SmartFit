package com.example.smartfit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartfit.ui.screens.*
import com.example.smartfit.viewmodel.ActivityViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object ActivityLog : Screen("activity_log")
    object AddEditActivity : Screen("add_edit_activity/{activityId}") {
        fun createRoute(activityId: Long = -1L) = "add_edit_activity/$activityId"
    }
    object Profile : Screen("profile")
    object Exercises : Screen("exercises")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ActivityViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = viewModel,
                onNavigationResolved = { isLoggedIn ->
                    val targetRoute = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToActivityLog = {
                    navController.navigate(Screen.ActivityLog.route)
                },
                onNavigateToAddActivity = {
                    navController.navigate(Screen.AddEditActivity.createRoute())
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToExercises = {
                    navController.navigate(Screen.Exercises.route)
                }
            )
        }

        composable(Screen.ActivityLog.route) {
            ActivityLogScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { activityId ->
                    navController.navigate(Screen.AddEditActivity.createRoute(activityId))
                },
                onNavigateToAdd = {
                    navController.navigate(Screen.AddEditActivity.createRoute())
                }
            )
        }

        composable(
            route = Screen.AddEditActivity.route,
            arguments = listOf(navArgument("activityId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getLong("activityId") ?: -1L
            AddEditActivityScreen(
                viewModel = viewModel,
                activityId = activityId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Exercises.route) {
            ExercisesScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

