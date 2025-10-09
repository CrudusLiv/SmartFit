package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "Steps", "Workout", "Calories"
    val value: Int,
    val unit: String, // "steps", "minutes", "kcal"
    val date: Long, // Timestamp
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

