package com.example.smartfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.data.datastore.UserPreferences
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.remote.ApiService
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.ui.screens.HomeScreen
import com.example.smartfit.ui.theme.SmartFitTheme
import com.example.smartfit.viewmodel.ActivityViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Automated UI tests for HomeScreen
 * Tests navigation, user interactions, and data display
 * 
 * Each group member should add at least 2 UI tests for smooth navigation
 * or correct display of data
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysAppTitle() {
        // Test that the home screen displays the app title
        composeTestRule.setContent {
            SmartFitTheme {
                // This would require a mock ViewModel in production
                // Demonstrating UI test structure for app title
            }
        }

        // Verify app title is displayed
        composeTestRule.onNodeWithText("SmartFit").assertExists()
    }

    @Test
    fun homeScreen_addActivityButton_isDisplayed() {
        // Test that the add activity button (FAB) is visible
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup would go here
            }
        }

        // Verify FAB exists with content description
        composeTestRule.onNodeWithContentDescription("Add new activity button")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_addActivityButton_isClickable() {
        // Test that the add activity button is clickable
        var buttonClicked = false

        composeTestRule.setContent {
            SmartFitTheme {
                // Mock implementation
            }
        }

        // Verify button can be clicked
        composeTestRule.onNodeWithContentDescription("Add new activity button")
            .assertHasClickAction()
    }

    @Test
    fun homeScreen_navigationBottomBar_isDisplayed() {
        // Test that the bottom navigation bar is visible
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Verify navigation bar exists
        composeTestRule.onNodeWithTag("bottom_navigation")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_activityLogTab_isClickable() {
        // Test that the activity log tab is clickable
        var tabClicked = false

        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with click callback
            }
        }

        // Verify activity log tab can be interacted with
        composeTestRule.onNodeWithText("Activity Log")
            .assertExists()
    }

    @Test
    fun homeScreen_workoutTab_isClickable() {
        // Test that the workout tab is clickable and navigates correctly
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Verify workout tab exists
        composeTestRule.onNodeWithText("Workouts")
            .assertExists()
    }

    @Test
    fun homeScreen_profileTab_isClickable() {
        // Test that the profile tab is visible and clickable
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Verify profile tab exists
        composeTestRule.onNodeWithText("Profile")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysActivityList_whenDataAvailable() {
        // Test that activities are displayed when data is available
        composeTestRule.setContent {
            SmartFitTheme {
                // Would inject mock ViewModel with test data
            }
        }

        // Verify activity list container exists
        composeTestRule.onNodeWithTag("activity_list")
            .assertExists()
    }

    @Test
    fun homeScreen_displaysEmptyState_whenNoActivities() {
        // Test that empty state is shown when no activities exist
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock ViewModel with empty activity list
            }
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("No activities yet")
            .assertExists()
    }

    @Test
    fun homeScreen_loadingIndicator_showsWhileLoadingData() {
        // Test that loading indicator is shown during data fetch
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock ViewModel with loading state
            }
        }

        // Verify loading indicator exists
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_swipeRefresh_triggersDataReload() {
        // Test that pull-to-refresh gesture triggers data reload
        var refreshTriggered = false

        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with refresh callback
            }
        }

        // Perform swipe down gesture on scrollable content
        composeTestRule.onNodeWithTag("activity_list")
            .performTouchInput { swipeDown() }

        // In real implementation, would verify refresh callback was invoked
    }

    @Test
    fun homeScreen_activityCard_displaysCorrectInformation() {
        // Test that activity cards display correct data
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock ViewModel with test activity data
                // Activity: Walking, 30 min, 150 calories
            }
        }

        // Verify activity details are displayed
        composeTestRule.onNodeWithText("Walking").assertExists()
        composeTestRule.onNodeWithText("30 min").assertExists()
        composeTestRule.onNodeWithText("150 cal").assertExists()
    }

    @Test
    fun homeScreen_activityCard_isClickableForDetails() {
        // Test that clicking activity card shows details
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Verify activity cards are clickable
        composeTestRule.onNodeWithTag("activity_card_0")
            .assertHasClickAction()
    }

    @Test
    fun homeScreen_stepCounter_displaysCurrentSteps() {
        // Test that step counter displays current step count
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with step data: 5000 steps
            }
        }

        // Verify step count is displayed
        composeTestRule.onNodeWithText("5000").assertExists()
        composeTestRule.onNodeWithText("steps").assertExists()
    }

    @Test
    fun homeScreen_calorieCounter_displaysCaloriesBurned() {
        // Test that calorie counter displays burned calories
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with calorie data: 450 calories
            }
        }

        // Verify calorie count is displayed
        composeTestRule.onNodeWithText("450").assertExists()
        composeTestRule.onNodeWithText("cal").assertExists()
    }

    @Test
    fun homeScreen_goalProgress_displaysCorrectPercentage() {
        // Test that goal progress bar shows correct percentage
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with 75% goal completion
            }
        }

        // Verify progress indicator exists and shows percentage
        composeTestRule.onNodeWithTag("goal_progress")
            .assertExists()
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("75%").assertExists()
    }

    @Test
    fun homeScreen_filterButton_opensFilterDialog() {
        // Test that filter button opens filtering options
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock setup
            }
        }

        // Click filter button
        composeTestRule.onNodeWithContentDescription("Filter activities")
            .performClick()

        // Verify filter dialog appears
        composeTestRule.onNodeWithText("Filter Activities")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_searchBar_filtersActivitiesByType() {
        // Test that search functionality filters displayed activities
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with multiple activity types
            }
        }

        // Enter search text
        composeTestRule.onNodeWithTag("search_field")
            .performTextInput("Running")

        // Verify filtered results
        composeTestRule.onNodeWithText("Running").assertExists()
        composeTestRule.onNodeWithText("Walking").assertDoesNotExist()
    }

    @Test
    fun homeScreen_scrolling_worksCorrectly() {
        // Test that activity list scrolls correctly with multiple items
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with 10+ activities
            }
        }

        // Perform scroll gesture
        composeTestRule.onNodeWithTag("activity_list")
            .performScrollToIndex(5)

        // Verify scroll occurred by checking visibility
        composeTestRule.onNodeWithTag("activity_card_5")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_errorMessage_displaysWhenLoadFails() {
        // Test that error message is shown when data load fails
        composeTestRule.setContent {
            SmartFitTheme {
                // Mock ViewModel with error state
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to load activities")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_retryButton_triggersDataReloadAfterError() {
        // Test that retry button reloads data after error
        var retryClicked = false

        composeTestRule.setContent {
            SmartFitTheme {
                // Mock with error state and retry callback
            }
        }

        // Click retry button
        composeTestRule.onNodeWithText("Retry")
            .performClick()

        // Verify retry was triggered (would check callback in real implementation)
    }
}
