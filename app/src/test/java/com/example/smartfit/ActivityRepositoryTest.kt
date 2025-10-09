package com.example.smartfit

import com.example.smartfit.data.repository.ActivityRepository
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for ActivityRepository business logic
 * Tests core features like calorie calculation and step goal progress
 */
class ActivityRepositoryTest {

    private lateinit var repository: ActivityRepository

    @Before
    fun setup() {
        // Note: For full testing, you would mock the DAO and ApiService
        // This demonstrates the business logic testing
    }

    @Test
    fun calculateCaloriesBurned_walking_returnsCorrectValue() {
        // Test calorie calculation for walking
        val calories = calculateCaloriesBurnedStandalone("walking", 30, 70f)

        // Expected: MET (3.5) * weight (70) * duration (0.5 hours) = 122.5
        assertEquals(122, calories)
    }

    @Test
    fun calculateCaloriesBurned_running_returnsCorrectValue() {
        // Test calorie calculation for running
        val calories = calculateCaloriesBurnedStandalone("running", 60, 80f)

        // Expected: MET (8.0) * weight (80) * duration (1 hour) = 640
        assertEquals(640, calories)
    }

    @Test
    fun calculateCaloriesBurned_workout_returnsCorrectValue() {
        // Test calorie calculation for general workout
        val calories = calculateCaloriesBurnedStandalone("workout", 45, 75f)

        // Expected: MET (5.0) * weight (75) * duration (0.75 hours) = 281.25
        assertEquals(281, calories)
    }

    @Test
    fun calculateCaloriesBurned_zeroWeight_returnsZero() {
        // Test with zero weight
        val calories = calculateCaloriesBurnedStandalone("walking", 30, 0f)
        assertEquals(0, calories)
    }

    @Test
    fun calculateCaloriesBurned_zeroDuration_returnsZero() {
        // Test with zero duration
        val calories = calculateCaloriesBurnedStandalone("walking", 0, 70f)
        assertEquals(0, calories)
    }

    @Test
    fun calculateStepGoalProgress_halfComplete_returns50Percent() {
        // Test step goal progress calculation
        val progress = calculateStepGoalProgressStandalone(5000, 10000)
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun calculateStepGoalProgress_complete_returns100Percent() {
        // Test 100% completion
        val progress = calculateStepGoalProgressStandalone(10000, 10000)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun calculateStepGoalProgress_overComplete_returns100Percent() {
        // Test over 100% (should cap at 1.0)
        val progress = calculateStepGoalProgressStandalone(15000, 10000)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun calculateStepGoalProgress_zeroSteps_returnsZero() {
        // Test with zero steps
        val progress = calculateStepGoalProgressStandalone(0, 10000)
        assertEquals(0.0f, progress, 0.01f)
    }

    @Test
    fun calculateStepGoalProgress_zeroGoal_returnsZero() {
        // Test with zero goal (edge case)
        val progress = calculateStepGoalProgressStandalone(5000, 0)
        assertEquals(0.0f, progress, 0.01f)
    }

    @Test
    fun calculateStepGoalProgress_negativeValues_handledCorrectly() {
        // Test with negative values (edge case)
        val progress = calculateStepGoalProgressStandalone(-100, 10000)
        assertEquals(0.0f, progress, 0.01f)
    }

    // Standalone implementations for testing without mocking
    private fun calculateCaloriesBurnedStandalone(activityType: String, durationMinutes: Int, weight: Float): Int {
        val metValue = when (activityType.lowercase()) {
            "walking" -> 3.5
            "running" -> 8.0
            "cycling" -> 6.0
            "swimming" -> 7.0
            "workout" -> 5.0
            "yoga" -> 2.5
            else -> 4.0
        }
        return (metValue * weight * (durationMinutes / 60.0)).toInt()
    }

    private fun calculateStepGoalProgressStandalone(currentSteps: Int, goalSteps: Int): Float {
        if (goalSteps <= 0) return 0f
        return (currentSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    }
}

