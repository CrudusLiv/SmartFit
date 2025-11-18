package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "Steps", "Workout", "Calories", etc.
    val value: Int, // Number value (steps count, calories, duration in minutes)
    val date: Long, // Timestamp
    val notes: String = "",
    val duration: Int = 0 // Duration in minutes for workouts
)

