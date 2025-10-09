package com.example.smartfit.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class WorkoutTip(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val imageUrl: String
)

data class NutritionTip(
    val id: Int,
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val description: String
)

// Response wrapper for JSONPlaceholder API
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    // Using JSONPlaceholder as a demo API
    @GET("posts")
    suspend fun getTips(@Query("_limit") limit: Int = 10): List<Post>

    @GET("posts")
    suspend fun getTipById(@Query("id") id: Int): List<Post>
}

