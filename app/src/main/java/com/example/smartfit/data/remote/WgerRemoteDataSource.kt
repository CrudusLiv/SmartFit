package com.example.smartfit.data.remote

import android.util.Log
import com.example.smartfit.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WgerRemoteDataSource private constructor(
    private val apiService: WgerApiService
) {
    companion object {
        private const val TAG = "WgerRemoteDataSource"
        private const val BASE_URL = "https://wger.de/api/v2/"
        private const val MAX_PAGE_SIZE = 100

        fun create(): WgerRemoteDataSource {
            val authInterceptor = Interceptor { chain ->
                val token = BuildConfig.WGER_TOKEN.orEmpty()
                val requestBuilder = chain.request()
                    .newBuilder()
                    .header("Accept", "application/json")

                if (token.isNotBlank()) {
                    requestBuilder.header("Authorization", "Token $token")
                    Log.d(TAG, "Using Wger API token")
                } else {
                    Log.w(TAG, "No Wger API token configured")
                }

                chain.proceed(requestBuilder.build())
            }

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)

            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                clientBuilder.addInterceptor(logging)
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return WgerRemoteDataSource(retrofit.create(WgerApiService::class.java))
        }
    }

    suspend fun fetchExercises(limit: Int, offset: Int): ExerciseInfoResponse {
        val safeLimit = limit.coerceIn(1, MAX_PAGE_SIZE)
        val safeOffset = offset.coerceAtLeast(0)
        Log.d(TAG, "Fetching exercises: limit=$safeLimit, offset=$safeOffset")
        val response = apiService.getExercises(limit = safeLimit, offset = safeOffset)
        Log.d(TAG, "Response received: count=${response.count}, results=${response.results.size}")
        return response
    }
}
