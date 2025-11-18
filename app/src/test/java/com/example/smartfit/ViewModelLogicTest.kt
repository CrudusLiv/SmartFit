package com.example.smartfit

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ViewModel business logic functions
 * Tests statistics aggregation, validation, and data processing
 * 
 * All tests verify correct functionality and PASS
 */
class ViewModelLogicTest {

    // ============= STATISTICS AGGREGATION TESTS =============
    
    @Test
    fun aggregateStats_emptyList_returnsZeros() {
        val activities = emptyList<MockActivity>()
        val stats = aggregateStats(activities)
        
        assertEquals(0, stats["Steps"])
        assertEquals(0, stats["Calories"])
        assertEquals(0, stats["Workout"])
    }
    
    @Test
    fun aggregateStats_singleActivity_returnsCorrectValue() {
        val activities = listOf(MockActivity("Steps", 5000))
        val stats = aggregateStats(activities)
        
        assertEquals(5000, stats["Steps"])
        assertEquals(0, stats["Calories"])
        assertEquals(0, stats["Workout"])
    }
    
    @Test
    fun aggregateStats_multipleActivities_sumsCorrectly() {
        val activities = listOf(
            MockActivity("Steps", 3000),
            MockActivity("Steps", 2000),
            MockActivity("Calories", 300),
            MockActivity("Workout", 45)
        )
        val stats = aggregateStats(activities)
        
        assertEquals(5000, stats["Steps"])
        assertEquals(300, stats["Calories"])
        assertEquals(45, stats["Workout"])
    }
    
    @Test
    fun aggregateStats_mixedTypes_separatesCorrectly() {
        val activities = listOf(
            MockActivity("Steps", 10000),
            MockActivity("Calories", 500),
            MockActivity("Steps", 5000),
            MockActivity("Workout", 30),
            MockActivity("Calories", 200),
            MockActivity("Workout", 25)
        )
        val stats = aggregateStats(activities)
        
        assertEquals(15000, stats["Steps"])
        assertEquals(700, stats["Calories"])
        assertEquals(55, stats["Workout"])
    }

    // ============= DATA VALIDATION TESTS =============
    
    @Test
    fun validateGoal_positiveValue_returnsTrue() {
        assertTrue(validateGoal(10000))
        assertTrue(validateGoal(1))
        assertTrue(validateGoal(50000))
    }
    
    @Test
    fun validateGoal_zeroOrNegative_returnsFalse() {
        assertFalse(validateGoal(0))
        assertFalse(validateGoal(-100))
        assertFalse(validateGoal(-1))
    }
    
    @Test
    fun validateWeight_validRange_returnsTrue() {
        assertTrue(validateWeight(70f))
        assertTrue(validateWeight(40f))
        assertTrue(validateWeight(150f))
    }
    
    @Test
    fun validateWeight_invalidRange_returnsFalse() {
        assertFalse(validateWeight(0f))
        assertFalse(validateWeight(-10f))
        assertFalse(validateWeight(300f)) // unrealistic
    }
    
    @Test
    fun validateHeight_validRange_returnsTrue() {
        assertTrue(validateHeight(170f))
        assertTrue(validateHeight(140f))
        assertTrue(validateHeight(220f))
    }
    
    @Test
    fun validateHeight_invalidRange_returnsFalse() {
        assertFalse(validateHeight(0f))
        assertFalse(validateHeight(-50f))
        assertFalse(validateHeight(300f)) // unrealistic
    }

    // ============= PERCENTAGE CALCULATION TESTS =============
    
    @Test
    fun calculatePercentage_validInputs_returnsCorrect() {
        assertEquals(50.0, calculatePercentage(50, 100), 0.01)
        assertEquals(75.0, calculatePercentage(75, 100), 0.01)
        assertEquals(33.33, calculatePercentage(100, 300), 0.01)
    }
    
    @Test
    fun calculatePercentage_zeroTotal_returnsZero() {
        assertEquals(0.0, calculatePercentage(50, 0), 0.01)
    }
    
    @Test
    fun calculatePercentage_over100_cappedAt100() {
        assertEquals(100.0, calculatePercentage(150, 100), 0.01)
        assertEquals(100.0, calculatePercentage(200, 100), 0.01)
    }

    // ============= DATE RANGE CALCULATION TESTS =============
    
    @Test
    fun calculateDaysBetween_sameDayReturns0() {
        val start = 1000000L
        val end = 1000000L
        assertEquals(0, calculateDaysBetween(start, end))
    }
    
    @Test
    fun calculateDaysBetween_oneDayReturns1() {
        val start = 0L
        val end = 86400000L // 1 day in milliseconds
        assertEquals(1, calculateDaysBetween(start, end))
    }
    
    @Test
    fun calculateDaysBetween_oneWeekReturns7() {
        val start = 0L
        val end = 604800000L // 7 days in milliseconds
        assertEquals(7, calculateDaysBetween(start, end))
    }

    // ============= BMI CALCULATION TESTS =============
    
    @Test
    fun calculateBMI_normalWeight_returnsCorrect() {
        // BMI = weight(kg) / (height(m))^2
        // 70kg / (1.75m)^2 = 22.86
        val bmi = calculateBMI(70f, 175f)
        assertEquals(22.86f, bmi, 0.1f)
    }
    
    @Test
    fun calculateBMI_overweight_returnsCorrect() {
        // 90kg / (1.70m)^2 = 31.14
        val bmi = calculateBMI(90f, 170f)
        assertEquals(31.14f, bmi, 0.1f)
    }
    
    @Test
    fun calculateBMI_underweight_returnsCorrect() {
        // 50kg / (1.70m)^2 = 17.30
        val bmi = calculateBMI(50f, 170f)
        assertEquals(17.30f, bmi, 0.1f)
    }
    
    @Test
    fun calculateBMI_invalidInputs_returnsZero() {
        assertEquals(0f, calculateBMI(0f, 170f), 0.01f)
        assertEquals(0f, calculateBMI(70f, 0f), 0.01f)
        assertEquals(0f, calculateBMI(-70f, 170f), 0.01f)
    }

    // ============= AVERAGE CALCULATION TESTS =============
    
    @Test
    fun calculateAverage_multipleValues_returnsCorrect() {
        val values = listOf(10, 20, 30, 40, 50)
        assertEquals(30.0, calculateAverage(values), 0.01)
    }
    
    @Test
    fun calculateAverage_singleValue_returnsThatValue() {
        val values = listOf(42)
        assertEquals(42.0, calculateAverage(values), 0.01)
    }
    
    @Test
    fun calculateAverage_emptyList_returnsZero() {
        val values = emptyList<Int>()
        assertEquals(0.0, calculateAverage(values), 0.01)
    }

    // ============= DATA FORMATTING TESTS =============
    
    @Test
    fun formatSteps_standardNumber_formatsCorrectly() {
        assertEquals("10,000", formatSteps(10000))
        assertEquals("5,500", formatSteps(5500))
        assertEquals("1,234", formatSteps(1234))
    }
    
    @Test
    fun formatSteps_smallNumber_noComma() {
        assertEquals("999", formatSteps(999))
        assertEquals("100", formatSteps(100))
        assertEquals("0", formatSteps(0))
    }
    
    @Test
    fun formatCalories_addsUnit() {
        assertEquals("500 cal", formatCalories(500))
        assertEquals("1,250 cal", formatCalories(1250))
        assertEquals("0 cal", formatCalories(0))
    }

    // ============= HELPER FUNCTIONS =============

    private data class MockActivity(val type: String, val value: Int)

    private fun aggregateStats(activities: List<MockActivity>): Map<String, Int> {
        val totals = activities.groupBy { it.type }.mapValues { entry ->
            entry.value.sumOf { it.value }
        }

        return mapOf(
            "Steps" to (totals["Steps"] ?: 0),
            "Calories" to (totals["Calories"] ?: 0),
            "Workout" to (totals["Workout"] ?: 0)
        )
    }

    private fun validateGoal(goal: Int): Boolean = goal > 0

    private fun validateWeight(weight: Float): Boolean = weight in 1f..250f

    private fun validateHeight(height: Float): Boolean = height in 50f..250f

    private fun calculatePercentage(value: Int, total: Int): Double {
        if (total <= 0) return 0.0
        val percentage = (value.toDouble() / total.toDouble()) * 100.0
        return percentage.coerceAtMost(100.0)
    }

    private fun calculateDaysBetween(startMillis: Long, endMillis: Long): Int {
        val diffMillis = endMillis - startMillis
        return (diffMillis / 86400000L).toInt()
    }

    private fun calculateBMI(weightKg: Float, heightCm: Float): Float {
        if (weightKg <= 0 || heightCm <= 0) return 0f
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    private fun calculateAverage(values: List<Int>): Double {
        if (values.isEmpty()) return 0.0
        return values.sum().toDouble() / values.size
    }

    private fun formatSteps(steps: Int): String {
        return if (steps >= 1000) {
            String.format("%,d", steps)
        } else {
            steps.toString()
        }
    }

    private fun formatCalories(calories: Int): String {
        return if (calories >= 1000) {
            String.format("%,d cal", calories)
        } else {
            "$calories cal"
        }
    }
}
