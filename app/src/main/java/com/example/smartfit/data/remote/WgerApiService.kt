package com.example.smartfit.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WgerApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("language") language: Int = DEFAULT_LANGUAGE,
        @Query("limit") limit: Int = DEFAULT_LIMIT,
        @Query("offset") offset: Int = 0,
        @Query("status") status: Int = STATUS_APPROVED,
        @Query("ordering") ordering: String = "name"
    ): ExerciseInfoResponse

    companion object {
        const val DEFAULT_LANGUAGE = 2
        const val STATUS_APPROVED = 2
        const val DEFAULT_LIMIT = 20
    }
}
