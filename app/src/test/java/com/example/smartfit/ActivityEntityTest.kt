package com.example.smartfit

import com.example.smartfit.data.local.ActivityEntity
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ActivityEntity data model
 * Tests data validation and entity creation
 */
class ActivityEntityTest {

    @Test
    fun activityEntity_creation_isCorrect() {
        // Test creating an ActivityEntity with actual structure
        val activity = ActivityEntity(
            id = 1,
            type = "Steps",
            value = 10000,
            date = System.currentTimeMillis(),
            notes = "Morning walk",
            duration = 30
        )

        assertEquals(1L, activity.id)
        assertEquals("Steps", activity.type)
        assertEquals(10000, activity.value)
        assertEquals(30, activity.duration)
        assertEquals("Morning walk", activity.notes)
    }

    @Test
    fun activityEntity_defaultValues_areCorrect() {
        // Test default values
        val activity = ActivityEntity(
            type = "Workout",
            value = 30,
            date = System.currentTimeMillis()
        )

        assertEquals(0L, activity.id)
        assertEquals("", activity.notes)
        assertEquals(0, activity.duration)
    }

    @Test
    fun activityEntity_copy_createsNewInstance() {
        // Test copying an entity
        val original = ActivityEntity(
            id = 1,
            type = "Steps",
            value = 5000,
            date = System.currentTimeMillis()
        )

        val copied = original.copy(value = 10000)

        assertEquals(original.id, copied.id)
        assertEquals(original.type, copied.type)
        assertEquals(10000, copied.value)
        assertNotEquals(original.value, copied.value)
    }

    @Test
    fun activityEntity_equals_comparesCorrectly() {
        // Test equality
        val time = System.currentTimeMillis()
        val activity1 = ActivityEntity(
            id = 1,
            type = "Steps",
            value = 10000,
            date = time,
            duration = 30
        )

        val activity2 = ActivityEntity(
            id = 1,
            type = "Steps",
            value = 10000,
            date = time,
            duration = 30
        )

        assertEquals(activity1, activity2)
    }

    @Test
    fun activityEntity_duration_isStored() {
        // Test duration field
        val activity = ActivityEntity(
            type = "Workout",
            value = 500,
            date = System.currentTimeMillis(),
            duration = 45
        )

        assertEquals(45, activity.duration)
    }
}

