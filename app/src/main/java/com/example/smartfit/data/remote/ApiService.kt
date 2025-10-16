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
    val uuid: String? = null,
    val exercise_base: Int? = null,
    val description: String? = null,
    val created: String? = null,
    val category: ExerciseCategory? = null,
    val muscles: List<Muscle> = emptyList(),
    val muscles_secondary: List<Muscle> = emptyList(),
    val equipment: List<Equipment> = emptyList(),
    val variations: List<Int> = emptyList(),
    val author_history: List<String> = emptyList(),
    val images: List<ExerciseImage> = emptyList(),
    val videos: List<ExerciseVideo> = emptyList()
)

data class ExerciseCategory(
    val id: Int? = null,
    val name: String? = null
)

data class Muscle(
    val id: Int? = null,
    val name: String? = null,
    val name_en: String? = null,
    val is_front: Boolean? = null,
    val image_url_main: String? = null,
    val image_url_secondary: String? = null
)

data class Equipment(
    val id: Int? = null,
    val name: String? = null
)

data class ExerciseImage(
    val id: Int? = null,
    val uuid: String? = null,
    val exercise_base: Int? = null,
    val image: String? = null,
    val is_main: Boolean? = null,
    val style: String? = null,
    val license: Int? = null,
    val license_author: String? = null,
    val author_history: List<String> = emptyList()
)

data class ExerciseVideo(
    val id: Int? = null,
    val uuid: String? = null,
    val exercise_base: Int? = null,
    val video: String? = null,
    val is_main: Boolean? = null,
    val size: Int? = null,
    val duration: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val codec: String? = null,
    val codec_long: String? = null,
    val license: Int? = null,
    val license_author: String? = null,
    val author_history: List<String> = emptyList()
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
        @Query("language") language: Int = 2
    ): ExerciseInfoResponse
}
