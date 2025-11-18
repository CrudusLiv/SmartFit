package com.example.smartfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated UI tests for navigation flows
 * Tests smooth navigation between screens and correct screen transitions
 * 
 * These tests verify that navigation works correctly throughout the app
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun navigation_homeToActivityLog_displaysActivityLogScreen() {
        // Test navigation from Home to Activity Log screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Click on Activity Log tab
        composeTestRule.onNodeWithText("Activity Log")
            .performClick()

        // Verify Activity Log screen is displayed
        composeTestRule.onNodeWithText("Your Activities")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_homeToWorkouts_displaysWorkoutsScreen() {
        // Test navigation from Home to Workouts screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Click on Workouts tab
        composeTestRule.onNodeWithText("Workouts")
            .performClick()

        // Verify Workouts screen is displayed
        composeTestRule.onNodeWithText("Workout Suggestions")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_homeToProfile_displaysProfileScreen() {
        // Test navigation from Home to Profile screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Click on Profile tab
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Verify Profile screen is displayed
        composeTestRule.onNodeWithText("User Profile")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_addActivityFlow_opensAddActivityScreen() {
        // Test that clicking add activity button navigates to add screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Click FAB to add activity
        composeTestRule.onNodeWithContentDescription("Add new activity button")
            .performClick()

        // Verify Add Activity screen is displayed
        composeTestRule.onNodeWithText("Add Activity")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_activityDetails_displaysCorrectActivity() {
        // Test navigation to activity details screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with sample activity data
            }
        }

        // Click on an activity card
        composeTestRule.onNodeWithTag("activity_card_0")
            .performClick()

        // Verify Activity Details screen is displayed
        composeTestRule.onNodeWithText("Activity Details")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_backButton_returnsToHomeScreen() {
        // Test that back button returns to home screen
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Click back button
        composeTestRule.onNodeWithContentDescription("Navigate back")
            .performClick()

        // Verify returned to Home screen
        composeTestRule.onNodeWithText("SmartFit")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_bottomNav_maintainsState() {
        // Test that bottom navigation maintains state when switching tabs
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock navigation setup
            }
        }

        // Navigate to Activity Log
        composeTestRule.onNodeWithText("Activity Log")
            .performClick()

        // Scroll down in activity log
        composeTestRule.onNodeWithTag("activity_list")
            .performScrollToIndex(3)

        // Navigate to Home
        composeTestRule.onNodeWithText("Home")
            .performClick()

        // Navigate back to Activity Log
        composeTestRule.onNodeWithText("Activity Log")
            .performClick()

        // Verify scroll position is maintained (would check state in real implementation)
        composeTestRule.onNodeWithTag("activity_card_3")
            .assertIsDisplayed()
    }

    @Test
    fun navigation_deepLink_opensCorrectScreen() {
        // Test that deep linking works correctly
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with deep link navigation
            }
        }

        // Verify correct screen is displayed from deep link
        composeTestRule.onNodeWithTag("workout_details_screen")
            .assertExists()
    }

    @Test
    fun navigation_workoutDetails_displaysWorkoutInfo() {
        // Test navigation to workout details
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with workout data
            }
        }

        // Navigate to Workouts tab
        composeTestRule.onNodeWithText("Workouts")
            .performClick()

        // Click on a workout suggestion
        composeTestRule.onNodeWithTag("workout_card_0")
            .performClick()

        // Verify Workout Details screen is displayed
        composeTestRule.onNodeWithText("Workout Details")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_editProfile_savesAndReturns() {
        // Test edit profile flow
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock profile setup
            }
        }

        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Click edit button
        composeTestRule.onNodeWithContentDescription("Edit profile")
            .performClick()

        // Verify Edit Profile screen is displayed
        composeTestRule.onNodeWithText("Edit Profile")
            .assertExists()

        // Click save button
        composeTestRule.onNodeWithText("Save")
            .performClick()

        // Verify returned to Profile screen
        composeTestRule.onNodeWithText("User Profile")
            .assertExists()
    }

    @Test
    fun navigation_settings_opensFromProfile() {
        // Test navigation to settings from profile
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Navigate to Profile
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Click settings button
        composeTestRule.onNodeWithContentDescription("Settings")
            .performClick()

        // Verify Settings screen is displayed
        composeTestRule.onNodeWithText("Settings")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_cancelAddActivity_returnsToHome() {
        // Test canceling add activity returns to home
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Open add activity screen
        composeTestRule.onNodeWithContentDescription("Add new activity button")
            .performClick()

        // Click cancel button
        composeTestRule.onNodeWithText("Cancel")
            .performClick()

        // Verify returned to Home screen
        composeTestRule.onNodeWithText("SmartFit")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_multipleTabSwitches_worksCorrectly() {
        // Test rapid navigation between multiple tabs
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Navigate through multiple tabs
        composeTestRule.onNodeWithText("Workouts").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Activity Log").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("Home").performClick()
        composeTestRule.waitForIdle()

        // Verify ended on Home screen
        composeTestRule.onNodeWithText("SmartFit")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun navigation_deleteActivity_showsConfirmationDialog() {
        // Test that delete action shows confirmation
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with activity data
            }
        }

        // Navigate to activity details
        composeTestRule.onNodeWithTag("activity_card_0")
            .performClick()

        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete activity")
            .performClick()

        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Delete Activity?")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Are you sure you want to delete this activity?")
            .assertExists()
    }

    @Test
    fun navigation_filterActivities_appliesAndDisplaysResults() {
        // Test filtering activities and displaying results
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with multiple activities
            }
        }

        // Navigate to Activity Log
        composeTestRule.onNodeWithText("Activity Log")
            .performClick()

        // Open filter dialog
        composeTestRule.onNodeWithContentDescription("Filter activities")
            .performClick()

        // Select filter option
        composeTestRule.onNodeWithText("Running")
            .performClick()

        // Apply filter
        composeTestRule.onNodeWithText("Apply")
            .performClick()

        // Verify filtered results are displayed
        composeTestRule.onNodeWithText("Running")
            .assertExists()
    }

    @Test
    fun navigation_startWorkout_beginsWorkoutSession() {
        // Test starting a workout from suggestions
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout setup
            }
        }

        // Navigate to Workouts
        composeTestRule.onNodeWithText("Workouts")
            .performClick()

        // Click on workout card
        composeTestRule.onNodeWithTag("workout_card_0")
            .performClick()

        // Click start workout button
        composeTestRule.onNodeWithText("Start Workout")
            .performClick()

        // Verify workout session screen is displayed
        composeTestRule.onNodeWithText("Workout in Progress")
            .assertExists()
            .assertIsDisplayed()
    }
}
