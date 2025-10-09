package com.example.smartfit.data.repository

import android.util.Log
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.remote.ApiService
import com.example.smartfit.data.remote.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "ActivityRepository"
    }

    // Local database operations
    fun getAllActivities(): Flow<List<ActivityEntity>> {
        Log.d(TAG, "Getting all activities from database")
        return activityDao.getAllActivities()
    }

    suspend fun getActivityById(id: Long): ActivityEntity? {
        Log.d(TAG, "Getting activity by id: $id")
        return activityDao.getActivityById(id)
    }

    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>> {
        Log.d(TAG, "Getting activities by date range: $startDate to $endDate")
        return activityDao.getActivitiesByDateRange(startDate, endDate)
    }

    fun getActivitiesByType(type: String): Flow<List<ActivityEntity>> {
        Log.d(TAG, "Getting activities by type: $type")
        return activityDao.getActivitiesByType(type)
    }

    suspend fun insertActivity(activity: ActivityEntity): Long {
        Log.d(TAG, "Inserting activity: $activity")
        val id = activityDao.insertActivity(activity)
        Log.d(TAG, "Activity inserted with id: $id")
        return id
    }

    suspend fun updateActivity(activity: ActivityEntity) {
        Log.d(TAG, "Updating activity: $activity")
        activityDao.updateActivity(activity)
        Log.d(TAG, "Activity updated successfully")
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        Log.d(TAG, "Deleting activity: $activity")
        activityDao.deleteActivity(activity)
        Log.d(TAG, "Activity deleted successfully")
    }

    suspend fun getTotalByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Int {
        Log.d(TAG, "Getting total for type: $type, date range: $startDate to $endDate")
        return activityDao.getTotalByTypeAndDateRange(type, startDate, endDate) ?: 0
    }

    // Network operations
    fun getTipsFromNetwork(): Flow<Result<List<Post>>> = flow {
        Log.d(TAG, "Fetching tips from network")
        emit(Result.Loading)
        try {
            val tips = apiService.getTips(limit = 10)
            Log.d(TAG, "Successfully fetched ${tips.size} tips from network")
            emit(Result.Success(tips))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching tips from network", e)
            emit(Result.Error(e))
        }
    }.catch { e ->
        Log.e(TAG, "Flow exception while fetching tips", e)
        emit(Result.Error(Exception(e)))
    }

    // Business logic methods
    fun calculateCaloriesBurned(activityType: String, durationMinutes: Int, weight: Float): Int {
        Log.d(TAG, "Calculating calories burned: type=$activityType, duration=$durationMinutes, weight=$weight")
        // MET (Metabolic Equivalent of Task) values for different activities
        val metValue = when (activityType.lowercase()) {
            "walking" -> 3.5
            "running" -> 8.0
            "cycling" -> 6.0
            "swimming" -> 7.0
            "workout" -> 5.0
            "yoga" -> 2.5
            else -> 4.0
        }

        // Calories = MET × weight (kg) × duration (hours)
        val caloriesBurned = (metValue * weight * (durationMinutes / 60.0)).toInt()
        Log.d(TAG, "Calculated calories burned: $caloriesBurned")
        return caloriesBurned
    }

    fun calculateStepGoalProgress(currentSteps: Int, goalSteps: Int): Float {
        if (goalSteps <= 0) return 0f
        val progress = (currentSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
        Log.d(TAG, "Step goal progress: $progress (${currentSteps}/${goalSteps})")
        return progress
    }
}

