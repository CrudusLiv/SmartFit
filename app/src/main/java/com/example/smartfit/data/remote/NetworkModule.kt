package com.example.smartfit.data.remote

import android.util.Log
import com.example.smartfit.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val TAG = "NetworkModule"
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val WGER_BASE_URL = "https://wger.de/api/v2/"

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val logging = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val auth: Interceptor = Interceptor { chain ->
        val req = chain.request()
        val builder = req.newBuilder()
        val token = BuildConfig.WGER_TOKEN
        if (token.isNotBlank()) {
            builder.header("Authorization", "Token $token")
        }
        chain.proceed(builder.build())
    }

    private val defaultClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val wgerClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    private val wgerRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(WGER_BASE_URL)
        .client(wgerClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
    val wgerApiService: WgerApiService by lazy { wgerRetrofit.create(WgerApiService::class.java) }
}

