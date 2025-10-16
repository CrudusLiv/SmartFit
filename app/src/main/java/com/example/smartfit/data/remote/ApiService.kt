package com.example.smartfit.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// Wger API Models (null-safe)
data class ExerciseInfoResponse(
    val count: Int? = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<ExerciseInfo> = emptyList()
)

data class ExerciseInfo(
    val id: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val category: ExerciseCategory? = null,
    val muscles: List<Muscle> = emptyList(),
    val muscles_secondary: List<Muscle> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val images: List<ExerciseImage> = emptyList()
)

data class ExerciseCategory(
    val id: Int? = null,
    val name: String? = null
)

data class Muscle(
    val id: Int? = null,
    val name: String? = null,
    val name_en: String? = null
)

data class Equipment(
    val id: Int? = null,
    val name: String? = null
)

data class ExerciseImage(
    val id: Int? = null,
    val image: String? = null,
    val is_main: Boolean? = null
)

// JSONPlaceholder API Models
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

interface ApiService {
    @GET("posts")
    suspend fun getTips(@Query("_limit") limit: Int = 10): List<Post>
}

interface WgerApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("language") language: Int = 2,
        @Query("status") status: Int = 2
    ): ExerciseInfoResponse
}
