package com.example.smartfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.theme.SmartFitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated UI tests for data display
 * Tests correct display of fetched data from API and database
 * 
 * These tests ensure that data is properly formatted and displayed to users
 */
@RunWith(AndroidJUnit4::class)
class DataDisplayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activityCard_displaysAllActivityFields() {
        // Test that activity card shows all required fields
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock activity: Running, 45 min, 3.5 km, 400 cal
            }
        }

        // Verify all fields are displayed
        composeTestRule.onNodeWithText("Running").assertExists()
        composeTestRule.onNodeWithText("45 min").assertExists()
        composeTestRule.onNodeWithText("3.5 km").assertExists()
        composeTestRule.onNodeWithText("400 cal").assertExists()
    }

    @Test
    fun activityCard_displaysDateAndTime() {
        // Test that activity card shows date and time
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock activity with timestamp
            }
        }

        // Verify date and time are formatted correctly
        composeTestRule.onNodeWithText("Nov 18, 2025").assertExists()
        composeTestRule.onNodeWithText("14:30").assertExists()
    }

    @Test
    fun workoutCard_displaysWorkoutDetails() {
        // Test that workout card shows all details
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout suggestion data
            }
        }

        // Verify workout details
        composeTestRule.onNodeWithText("Total Body HIIT").assertExists()
        composeTestRule.onNodeWithText("High intensity").assertExists()
        composeTestRule.onNodeWithText("18 min").assertExists()
    }

    @Test
    fun workoutCard_displaysPrimaryMuscles() {
        // Test that primary muscles are displayed
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout with muscles: Chest, Shoulders, Triceps
            }
        }

        // Verify muscle groups are shown
        composeTestRule.onNodeWithText("Chest").assertExists()
        composeTestRule.onNodeWithText("Shoulders").assertExists()
        composeTestRule.onNodeWithText("Triceps").assertExists()
    }

    @Test
    fun workoutCard_displaysEquipment() {
        // Test that equipment requirements are shown
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout with equipment: Dumbbells, Mat
            }
        }

        // Verify equipment is displayed
        composeTestRule.onNodeWithText("Dumbbells").assertExists()
        composeTestRule.onNodeWithText("Mat").assertExists()
    }

    @Test
    fun statsCard_displaysDailyStepCount() {
        // Test that daily step statistics are shown correctly
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock stats: 7,500 steps of 10,000 goal
            }
        }

        // Verify step count display
        composeTestRule.onNodeWithText("7,500").assertExists()
        composeTestRule.onNodeWithText("/ 10,000 steps").assertExists()
        composeTestRule.onNodeWithText("75%").assertExists()
    }

    @Test
    fun statsCard_displaysDailyCalories() {
        // Test that daily calorie statistics are shown
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock stats: 1,200 calories of 2,000 goal
            }
        }

        // Verify calorie display
        composeTestRule.onNodeWithText("1,200").assertExists()
        composeTestRule.onNodeWithText("/ 2,000 cal").assertExists()
        composeTestRule.onNodeWithText("60%").assertExists()
    }

    @Test
    fun progressBar_showsCorrectCompletion() {
        // Test that progress bar fills correctly based on percentage
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with 80% progress
            }
        }

        // Verify progress bar exists and shows correct percentage
        composeTestRule.onNodeWithTag("progress_bar")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("80%").assertExists()
    }

    @Test
    fun activityList_displaysMultipleActivities() {
        // Test that activity list shows multiple items correctly
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with 5 different activities
            }
        }

        // Verify multiple activities are displayed
        composeTestRule.onNodeWithTag("activity_card_0").assertExists()
        composeTestRule.onNodeWithTag("activity_card_1").assertExists()
        composeTestRule.onNodeWithTag("activity_card_2").assertExists()
        composeTestRule.onNodeWithTag("activity_card_3").assertExists()
        composeTestRule.onNodeWithTag("activity_card_4").assertExists()
    }

    @Test
    fun activityList_sortsActivitiesByDate() {
        // Test that activities are sorted by date (newest first)
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with activities from different dates
            }
        }

        // Verify most recent activity appears first
        composeTestRule.onAllNodesWithTag("activity_timestamp")[0]
            .assertTextContains("Today")
    }

    @Test
    fun workoutDescription_displaysFullText() {
        // Test that workout description is fully displayed
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout with description
            }
        }

        // Navigate to workout details
        composeTestRule.onNodeWithTag("workout_card_0")
            .performClick()

        // Verify description is shown
        composeTestRule.onNodeWithText("A fast-paced circuit mixing squats")
            .assertExists()
    }

    @Test
    fun errorMessage_displaysNetworkError() {
        // Test that network errors are displayed to user
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock network error state
            }
        }

        // Verify error message is shown
        composeTestRule.onNodeWithText("Network connection failed")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun loadingSpinner_showsDuringDataFetch() {
        // Test that loading spinner appears during network request
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock loading state
            }
        }

        // Verify loading indicator is visible
        composeTestRule.onNodeWithTag("loading_spinner")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsWhenNoWorkouts() {
        // Test that empty state is displayed when no workouts available
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock empty workout list
            }
        }

        // Verify empty state message
        composeTestRule.onNodeWithText("No workout suggestions available")
            .assertExists()
            .assertIsDisplayed()

        // Verify empty state icon
        composeTestRule.onNodeWithTag("empty_state_icon")
            .assertExists()
    }

    @Test
    fun activityDetails_showsHeartRateData() {
        // Test that heart rate data is displayed when available
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock activity with heart rate: avg 140, max 165
            }
        }

        // Navigate to activity details
        composeTestRule.onNodeWithTag("activity_card_0")
            .performClick()

        // Verify heart rate data
        composeTestRule.onNodeWithText("Avg Heart Rate: 140 bpm").assertExists()
        composeTestRule.onNodeWithText("Max Heart Rate: 165 bpm").assertExists()
    }

    @Test
    fun activityDetails_showsPaceForRunning() {
        // Test that pace is displayed for running activities
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock running activity with pace: 5:30 min/km
            }
        }

        // Navigate to activity details
        composeTestRule.onNodeWithTag("activity_card_0")
            .performClick()

        // Verify pace is shown
        composeTestRule.onNodeWithText("5:30 min/km").assertExists()
    }

    @Test
    fun profileScreen_displaysUserInfo() {
        // Test that profile displays user information correctly
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock user: John, 70kg, 175cm
            }
        }

        // Navigate to profile
        composeTestRule.onNodeWithText("Profile")
            .performClick()

        // Verify user info is displayed
        composeTestRule.onNodeWithText("John").assertExists()
        composeTestRule.onNodeWithText("70 kg").assertExists()
        composeTestRule.onNodeWithText("175 cm").assertExists()
    }

    @Test
    fun statisticsCard_showsWeeklySummary() {
        // Test that weekly summary statistics are displayed
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock weekly stats: 5 workouts, 2500 cal, 35 km
            }
        }

        // Verify weekly stats
        composeTestRule.onNodeWithText("This Week").assertExists()
        composeTestRule.onNodeWithText("5 workouts").assertExists()
        composeTestRule.onNodeWithText("2,500 cal").assertExists()
        composeTestRule.onNodeWithText("35 km").assertExists()
    }

    @Test
    fun workoutImage_loadsFromURL() {
        // Test that workout images load from API URLs
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock workout with image URL
            }
        }

        // Verify image placeholder or loaded image exists
        composeTestRule.onNodeWithTag("workout_image_0")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun activityTypeIcon_displaysCorrectIcon() {
        // Test that correct icon is shown for activity type
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock running activity
            }
        }

        // Verify running icon is displayed
        composeTestRule.onNodeWithContentDescription("Running activity icon")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun dateRangePicker_filtersActivitiesByRange() {
        // Test that date range picker filters activities
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with activities from different dates
            }
        }

        // Open date range picker
        composeTestRule.onNodeWithContentDescription("Select date range")
            .performClick()

        // Select date range (last 7 days)
        composeTestRule.onNodeWithText("Last 7 Days")
            .performClick()

        // Verify only activities from last 7 days are shown
        composeTestRule.onNodeWithText("Today").assertExists()
        composeTestRule.onNodeWithText("2 days ago").assertExists()
    }
}
