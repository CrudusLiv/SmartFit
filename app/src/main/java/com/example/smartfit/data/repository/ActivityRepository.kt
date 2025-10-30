package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.data.remote.ExerciseInfoResponse
import com.example.smartfit.google.GoogleFitDataSource
import com.example.smartfit.google.GoogleFitDataSource.FitWorkout
import java.util.Locale
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
    private val googleFitDataSource: GoogleFitDataSource
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

    fun getExercisesFromGoogleFit(limit: Int = 20, offset: Int = 0): Flow<Result<ExerciseInfoResponse>> = flow {
        emit(Result.Loading)
        try {
            val response = googleFitDataSource.fetchExercises(limit = limit, offset = offset)
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
            val workouts = googleFitDataSource.fetchWorkouts(limit = fetchLimit, offset = offset)
            val suggestions = workouts
                .asSequence()
                .map(::mapWorkoutToSuggestion)
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

private fun formatLabel(raw: String?): String? {
    val trimmed = raw?.trim().orEmpty()
    if (trimmed.isBlank()) return null
    return trimmed
        .split(Regex("\\s+"))
        .joinToString(" ") { part ->
            part.lowercase(Locale.getDefault()).replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
}

private fun mapWorkoutToSuggestion(workout: FitWorkout): WorkoutSuggestion {
    val muscles = workout.focusAreas.mapNotNull(::formatLabel).ifEmpty { listOf(formatLabel(workout.activity) ?: "General Fitness") }
    val equipment = workout.equipment.mapNotNull(::formatLabel).ifEmpty { listOf("Bodyweight") }
    val description = buildWorkoutDescription(workout)

    return WorkoutSuggestion(
        id = workout.id,
        name = workout.title.ifBlank { formatLabel(workout.activity) ?: "SmartFit Session" },
        category = formatLabel(workout.activity).orEmpty().ifBlank { workout.intensityLabel },
        primaryMuscles = muscles,
        equipment = equipment,
        imageUrl = null,
        description = description,
        durationMinutes = workout.durationMinutes,
        calories = workout.calories,
        distanceKm = workout.distanceKm,
        steps = workout.steps,
        startTimeMillis = workout.startTimeMillis,
        intensityLabel = workout.intensityLabel,
        effortScore = workout.effortScore,
        averageHeartRate = workout.averageHeartRate,
        maxHeartRate = workout.maxHeartRate,
        averagePaceMinutesPerKm = workout.averagePaceMinutesPerKm
    )
}

private fun buildWorkoutDescription(workout: FitWorkout): String {
    val metrics = mutableListOf<String>()
    metrics += "${workout.durationMinutes} min"
    if ((workout.distanceKm ?: 0.0) > 0.0) {
        metrics += String.format(Locale.getDefault(), "%.2f km", workout.distanceKm)
    }
    if (workout.steps > 0) {
        metrics += "${workout.steps} steps"
    }
    if (workout.calories > 0) {
        metrics += "${workout.calories} kcal"
    }
    workout.averagePaceMinutesPerKm?.takeIf { it > 0.0 }?.let { pace ->
        metrics += String.format(Locale.getDefault(), "%.1f min/km", pace)
    }
    workout.averageHeartRate?.takeIf { it > 0 }?.let { bpm ->
        metrics += "$bpm bpm avg"
    }

    val builder = StringBuilder()
    builder.append(workout.intensityLabel)
    if (metrics.isNotEmpty()) {
        builder.append(" • ").append(metrics.joinToString(" • "))
    }

    workout.notes?.takeIf { it.isNotBlank() }?.let { note ->
        builder.append('\n').append(note.trim())
    }

    return builder.toString()
}
