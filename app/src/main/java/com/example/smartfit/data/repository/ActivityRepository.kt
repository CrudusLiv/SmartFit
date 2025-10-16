package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.data.remote.ApiService
import com.example.smartfit.data.remote.ExerciseInfoResponse
import com.example.smartfit.data.remote.Post
import com.example.smartfit.data.remote.WgerApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val apiService: ApiService,
    private val wgerApiService: WgerApiService
) {
    companion object { private const val TAG = "ActivityRepository" }

    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>> =
        activityDao.getActivitiesByDateRange(startDate, endDate)

    suspend fun insertActivity(activity: ActivityEntity) {
        withContext(Dispatchers.IO) {
            activityDao.insertActivity(activity)
        }
    }

    suspend fun updateActivity(activity: ActivityEntity) {
        withContext(Dispatchers.IO) {
            activityDao.updateActivity(activity)
        }
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        withContext(Dispatchers.IO) {
            activityDao.deleteActivity(activity)
        }
    }

    suspend fun getTotalByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Int =
        withContext(Dispatchers.IO) {
            activityDao.getTotalByTypeAndDateRange(type, startDate, endDate) ?: 0
        }

    fun getTipsFromNetwork(limit: Int = 10): Flow<Result<List<Post>>> = flow {
        emit(Result.Loading)
        try {
            val tips = apiService.getTips(limit)
            emit(Result.Success(tips))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tips", e)
            emit(Result.Error(e))
        }
    }.catch { throwable ->
        emit(Result.Error(Exception(throwable)))
    }.flowOn(Dispatchers.IO)

    fun getExercisesFromWger(limit: Int = 20, offset: Int = 0): Flow<Result<ExerciseInfoResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = wgerApiService.getExercises(limit = limit, offset = offset, language = 2, status = 2)
            emit(Result.Success(response))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exercises", e)
            emit(Result.Error(e))
        }
    }.catch { throwable ->
        emit(Result.Error(Exception(throwable)))
    }.flowOn(Dispatchers.IO)

    fun getWorkoutSuggestions(limit: Int = 12, offset: Int = 0): Flow<Result<List<WorkoutSuggestion>>> = flow {
        emit(Result.Loading)
        try {
            val fetchLimit = (limit * 3).coerceAtLeast(limit)
            val response = wgerApiService.getExercises(
                limit = fetchLimit,
                offset = offset,
                language = 2,
                status = 2
            )
            val suggestions = response.results
                .asSequence()
                .map { exercise ->
                    val rawImage = exercise.images.firstOrNull { it.is_main == true }?.image
                        ?: exercise.images.firstOrNull { !it.image.isNullOrBlank() }?.image
                    val imageUrl = rawImage?.let { image ->
                        if (image.startsWith("http")) image else "https://wger.de$image"
                    }
                    val description = exercise.description?.let(::stripHtml).orEmpty()
                    WorkoutSuggestion(
                        id = exercise.id ?: exercise.hashCode(),
                        name = exercise.name.orEmpty().ifBlank { "Unnamed exercise" },
                        category = exercise.category?.name.orEmpty().ifBlank { "General" },
                        primaryMuscles = exercise.muscles.mapNotNull { it.name ?: it.name_en }.ifEmpty { listOf("Full body") },
                        equipment = exercise.equipment.mapNotNull { it.name }.ifEmpty { listOf("Bodyweight") },
                        imageUrl = imageUrl,
                        description = description.ifBlank { "Stay active with a quick session." }
                    )
                }
                .filter { it.description.isNotBlank() }
                .distinctBy { it.id }
                .take(limit)
                .toList()

            emit(Result.Success(suggestions))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workout suggestions", e)
            emit(Result.Error(e))
        }
    }.catch { throwable ->
        emit(Result.Error(Exception(throwable)))
    }.flowOn(Dispatchers.IO)

    fun calculateCaloriesBurned(activityType: String, durationMinutes: Int, weightKg: Float): Int {
        if (durationMinutes <= 0 || weightKg <= 0f) return 0

        val metValue = when (activityType.lowercase()) {
            "walking" -> 3.5
            "running" -> 8.0
            "cycling" -> 6.0
            "swimming" -> 7.0
            "workout", "training" -> 5.0
            "yoga" -> 2.5
            else -> 4.0
        }

        val durationHours = durationMinutes / 60.0
        return (metValue * weightKg * durationHours).toInt()
    }

    fun calculateStepGoalProgress(currentSteps: Int, goalSteps: Int): Float {
        if (goalSteps <= 0) return 0f
        val progress = currentSteps.toFloat() / goalSteps.toFloat()
        return progress.coerceIn(0f, 1f)
    }

    suspend fun getActivityById(id: Long): ActivityEntity? = withContext(Dispatchers.IO) {
        activityDao.getActivityById(id)
    }
}

private fun stripHtml(raw: String): String = raw
    .replace(Regex("<[^>]*>"), "")
    .replace("\n", " ")
    .replace(Regex("\\s+"), " ")
    .trim()
