package com.example.smartfit.data.model

/**
 * Simplified representation of a workout exercise fetched from the network.
 */
data class WorkoutSuggestion(
    val id: Int,
    val name: String,
    val category: String,
    val primaryMuscles: List<String>,
    val equipment: List<String>,
    val imageUrl: String?,
    val description: String
)
