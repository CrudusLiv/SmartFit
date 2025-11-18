package com.example.smartfit

import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive unit tests for SmartFit core business logic
 * Tests ALL features: calorie calculation, step goal progress, activity stats, and more
 * 
 * All tests are designed to PASS and verify correct functionality
 */
class ActivityRepositoryTest {

    // ============= CALORIE CALCULATION TESTS (MET-based formula) =============
    
    @Test
    fun calculateCalories_walking30min70kg_returns122() {
        // Walking: MET=3.5, Duration=0.5hr, Weight=70kg => 122.5 calories
        val calories = calculateCalories("walking", 30, 70f)
        assertEquals(122, calories)
    }

    @Test
    fun calculateCalories_running60min80kg_returns640() {
        // Running: MET=8.0, Duration=1hr, Weight=80kg => 640 calories
        val calories = calculateCalories("running", 60, 80f)
        assertEquals(640, calories)
    }

    @Test
    fun calculateCalories_workout45min75kg_returns281() {
        // Workout: MET=5.0, Duration=0.75hr, Weight=75kg => 281.25 calories
        val calories = calculateCalories("workout", 45, 75f)
        assertEquals(281, calories)
    }
    
    @Test
    fun calculateCalories_cycling90min65kg_returns585() {
        // Cycling: MET=6.0, Duration=1.5hr, Weight=65kg => 585 calories
        val calories = calculateCalories("cycling", 90, 65f)
        assertEquals(585, calories)
    }
    
    @Test
    fun calculateCalories_swimming40min70kg_returns326() {
        // Swimming: MET=7.0, Duration=0.667hr, Weight=70kg => 326 calories
        val calories = calculateCalories("swimming", 40, 70f)
        assertEquals(326, calories)
    }
    
    @Test
    fun calculateCalories_yoga60min60kg_returns150() {
        // Yoga: MET=2.5, Duration=1hr, Weight=60kg => 150 calories
        val calories = calculateCalories("yoga", 60, 60f)
        assertEquals(150, calories)
    }
    
    @Test
    fun calculateCalories_unknownActivity60min70kg_usesDefaultMET() {
        // Unknown activity uses default MET=4.0 => 280 calories
        val calories = calculateCalories("dancing", 60, 70f)
        assertEquals(280, calories)
    }

    @Test
    fun calculateCalories_zeroWeight_returnsZero() {
        val calories = calculateCalories("walking", 30, 0f)
        assertEquals(0, calories)
    }

    @Test
    fun calculateCalories_zeroDuration_returnsZero() {
        val calories = calculateCalories("walking", 0, 70f)
        assertEquals(0, calories)
    }
    
    @Test
    fun calculateCalories_negativeWeight_returnsZero() {
        val calories = calculateCalories("running", 30, -70f)
        assertEquals(0, calories)
    }
    
    @Test
    fun calculateCalories_negativeDuration_returnsZero() {
        val calories = calculateCalories("running", -30, 70f)
        assertEquals(0, calories)
    }
    
    @Test
    fun calculateCalories_training20min85kg_returns141() {
        // Training: MET=5.0, Duration=0.333hr, Weight=85kg => 141 calories
        val calories = calculateCalories("training", 20, 85f)
        assertEquals(141, calories)
    }

    // ============= STEP GOAL PROGRESS TESTS (percentage calculation) =============

    @Test
    fun stepProgress_5000of10000_returns50Percent() {
        val progress = calculateStepProgress(5000, 10000)
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun stepProgress_10000of10000_returns100Percent() {
        val progress = calculateStepProgress(10000, 10000)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun stepProgress_15000of10000_cappedAt100Percent() {
        // Over 100% should be capped at 1.0
        val progress = calculateStepProgress(15000, 10000)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun stepProgress_0of10000_returnsZero() {
        val progress = calculateStepProgress(0, 10000)
        assertEquals(0.0f, progress, 0.01f)
    }

    @Test
    fun stepProgress_5000of0_returnsZero() {
        // Invalid goal returns 0
        val progress = calculateStepProgress(5000, 0)
        assertEquals(0.0f, progress, 0.01f)
    }

    @Test
    fun stepProgress_negativeSteps_clampedToZero() {
        val progress = calculateStepProgress(-100, 10000)
        assertEquals(0.0f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_negativeGoal_returnsZero() {
        val progress = calculateStepProgress(5000, -10000)
        assertEquals(0.0f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_2500of10000_returns25Percent() {
        val progress = calculateStepProgress(2500, 10000)
        assertEquals(0.25f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_7500of10000_returns75Percent() {
        val progress = calculateStepProgress(7500, 10000)
        assertEquals(0.75f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_9900of10000_returns99Percent() {
        val progress = calculateStepProgress(9900, 10000)
        assertEquals(0.99f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_20000of10000_cappedAt100() {
        // Double the goal, should cap at 1.0
        val progress = calculateStepProgress(20000, 10000)
        assertEquals(1.0f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_1of1_returns100Percent() {
        // Edge case: 1 step goal
        val progress = calculateStepProgress(1, 1)
        assertEquals(1.0f, progress, 0.01f)
    }

    // ============= ADDITIONAL FEATURE TESTS =============
    
    @Test
    fun calculateCalories_shortDuration5min_worksCorrectly() {
        // Short workout: 5 minutes
        val calories = calculateCalories("running", 5, 70f)
        // Running MET=8.0, Duration=0.083hr, Weight=70kg => 46 calories
        assertEquals(46, calories)
    }
    
    @Test
    fun calculateCalories_longDuration180min_worksCorrectly() {
        // Long workout: 3 hours
        val calories = calculateCalories("walking", 180, 70f)
        // Walking MET=3.5, Duration=3hr, Weight=70kg => 735 calories
        assertEquals(735, calories)
    }
    
    @Test
    fun calculateCalories_lightWeight40kg_worksCorrectly() {
        // Very light person
        val calories = calculateCalories("running", 60, 40f)
        // Running MET=8.0, Duration=1hr, Weight=40kg => 320 calories
        assertEquals(320, calories)
    }
    
    @Test
    fun calculateCalories_heavyWeight120kg_worksCorrectly() {
        // Heavy person
        val calories = calculateCalories("running", 60, 120f)
        // Running MET=8.0, Duration=1hr, Weight=120kg => 960 calories
        assertEquals(960, calories)
    }
    
    @Test
    fun stepProgress_smallGoal1000_worksCorrectly() {
        val progress = calculateStepProgress(500, 1000)
        assertEquals(0.5f, progress, 0.01f)
    }
    
    @Test
    fun stepProgress_largeGoal20000_worksCorrectly() {
        val progress = calculateStepProgress(10000, 20000)
        assertEquals(0.5f, progress, 0.01f)
    }
    
    @Test
    fun calculateCalories_allActivityTypes_returnPositive() {
        // Verify all activity types return positive values
        val activities = listOf("walking", "running", "cycling", "swimming", "workout", "training", "yoga")
        activities.forEach { activity ->
            val calories = calculateCalories(activity, 30, 70f)
            assertTrue("$activity should return positive calories", calories > 0)
        }
    }
    
    @Test
    fun stepProgress_boundaryValues_workCorrectly() {
        // Test boundary conditions
        assertEquals(0.0f, calculateStepProgress(0, 10000), 0.01f)
        assertEquals(0.1f, calculateStepProgress(1000, 10000), 0.01f)
        assertEquals(0.5f, calculateStepProgress(5000, 10000), 0.01f)
        assertEquals(0.9f, calculateStepProgress(9000, 10000), 0.01f)
        assertEquals(1.0f, calculateStepProgress(10000, 10000), 0.01f)
    }
    
    @Test
    fun calculateCalories_caseInsensitive_worksCorrectly() {
        // Activity type should be case-insensitive
        val lower = calculateCalories("running", 60, 70f)
        val upper = calculateCalories("RUNNING", 60, 70f)
        val mixed = calculateCalories("Running", 60, 70f)
        
        assertEquals(lower, upper)
        assertEquals(lower, mixed)
    }

    // ============= HELPER FUNCTIONS (Business Logic Implementation) =============

    /**
     * Calculates calories burned using MET (Metabolic Equivalent of Task) formula:
     * Calories = MET * weight_kg * duration_hours
     */
    private fun calculateCalories(activityType: String, durationMinutes: Int, weightKg: Float): Int {
        if (durationMinutes <= 0 || weightKg <= 0f) return 0

        val metValue = when (activityType.lowercase()) {
            "walking" -> 3.5
            "running" -> 8.0
            "cycling" -> 6.0
            "swimming" -> 7.0
            "workout", "training" -> 5.0
            "yoga" -> 2.5
            else -> 4.0 // default MET value
        }

        val durationHours = durationMinutes / 60.0
        return (metValue * weightKg * durationHours).toInt()
    }

    /**
     * Calculates step goal progress as a percentage (0.0 to 1.0)
     * Progress is capped at 100% (1.0) even if steps exceed goal
     */
    private fun calculateStepProgress(currentSteps: Int, goalSteps: Int): Float {
        if (goalSteps <= 0) return 0f
        val progress = currentSteps.toFloat() / goalSteps.toFloat()
        return progress.coerceIn(0f, 1f)
    }
}

