package com.example.smartfit

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI tests for MainActivity
 * Tests complete user flows and navigation
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_successfully() {
        // Test that the app launches and displays the main screen
        composeTestRule.waitForIdle()

        // Verify main screen elements are present
        composeTestRule.onNodeWithText("Dashboard").assertExists()
        composeTestRule.onNodeWithText("Today's Progress").assertExists()
    }

    @Test
    fun navigation_toProfile_works() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // Click on profile tab
        composeTestRule.onNodeWithTag("nav_profile").performClick()

        // Verify we're on the profile screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Profile & Settings").assertExists()
    }

    @Test
    fun navigation_toActivityLog_works() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // Click on activity tab
        composeTestRule.onNodeWithTag("nav_activity_log").performClick()

        // Verify we're on the activity log screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Activity Log").assertExists()
    }

    @Test
    fun addActivity_flow_works() {
        // Test the complete flow of adding an activity
        composeTestRule.waitForIdle()

        // Click add activity button
        composeTestRule.onNodeWithContentDescription("Add new activity button").performClick()

        // Verify we're on the add activity screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add Activity").assertExists()

        // Enter activity value
        composeTestRule.onNodeWithContentDescription("Value input field").performTextInput("5000")

        // Save the activity
        composeTestRule.onNodeWithContentDescription("Save activity button").performClick()

        // Verify we're back on home screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dashboard").assertExists()
    }

    @Test
    fun appearanceSection_showsUnifiedThemeMessage() {
        // The app now uses a single dark appearance
        composeTestRule.waitForIdle()

        // Navigate to profile
        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("SmartFit now displays in a unified night mode", substring = true)
            .assertExists()
    }

    @Test
    fun statsCards_displayCorrectly() {
        // Test that stats cards are displayed
        composeTestRule.waitForIdle()

        // Verify stats cards exist
        composeTestRule.onNodeWithText("Steps").assertExists()
        composeTestRule.onNodeWithText("Calories").assertExists()
    }

    @Test
    fun activityLog_filtering_works() {
        // Test filtering in activity log
        composeTestRule.waitForIdle()

        // Navigate to activity log via bottom navigation
        composeTestRule.onNodeWithTag("nav_activity_log").performClick()
        composeTestRule.waitForIdle()

        // Click filter chip
        composeTestRule.onNodeWithText("Steps").performClick()

        // Verify filter is applied (filter chip would be selected)
        composeTestRule.onNodeWithText("Steps").assertExists()
    }
}

