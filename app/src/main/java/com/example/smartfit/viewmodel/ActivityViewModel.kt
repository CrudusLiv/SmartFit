package com.example.smartfit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.datastore.UserPreferences
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.remote.Post
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class ActivityUiState(
    val activities: List<ActivityEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val tips: List<Post> = emptyList(),
    val tipsLoading: Boolean = false,
    val tipsError: String? = null
)

data class UserPreferencesState(
    val darkTheme: Boolean = false,
    val dailyStepGoal: Int = 10000,
    val dailyCalorieGoal: Int = 2000,
    val userName: String = "",
    val userWeight: Float = 70f,
    val userHeight: Float = 170f,
    val isFirstLaunch: Boolean = true
)

class ActivityViewModel(
    private val repository: ActivityRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "ActivityViewModel"
    }

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    val userPreferencesState: StateFlow<UserPreferencesState> = combine(
        userPreferences.darkTheme,
        userPreferences.dailyStepGoal,
        userPreferences.dailyCalorieGoal,
        userPreferences.userName,
        userPreferences.userWeight,
        userPreferences.userHeight,
        userPreferences.isFirstLaunch
    ) { values: Array<*> ->
        UserPreferencesState(
            darkTheme = values[0] as Boolean,
            dailyStepGoal = values[1] as Int,
            dailyCalorieGoal = values[2] as Int,
            userName = values[3] as String,
            userWeight = values[4] as Float,
            userHeight = values[5] as Float,
            isFirstLaunch = values[6] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferencesState()
    )

    init {
        Log.d(TAG, "ActivityViewModel initialized")
        loadActivities()
        loadTips()
    }

    private fun loadActivities() {
        Log.d(TAG, "Loading activities")
        viewModelScope.launch {
            repository.getAllActivities()
                .catch { e ->
                    Log.e(TAG, "Error loading activities", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { activities ->
                    Log.d(TAG, "Loaded ${activities.size} activities")
                    _uiState.update { it.copy(activities = activities, isLoading = false) }
                }
        }
    }

    fun loadTips() {
        Log.d(TAG, "Loading tips from network")
        viewModelScope.launch {
            repository.getTipsFromNetwork()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            Log.d(TAG, "Tips loading...")
                            _uiState.update { it.copy(tipsLoading = true, tipsError = null) }
                        }
                        is Result.Success -> {
                            Log.d(TAG, "Tips loaded successfully: ${result.data.size} tips")
                            _uiState.update {
                                it.copy(
                                    tips = result.data,
                                    tipsLoading = false,
                                    tipsError = null
                                )
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error loading tips", result.exception)
                            _uiState.update {
                                it.copy(
                                    tipsLoading = false,
                                    tipsError = result.exception.message
                                )
                            }
                        }
                    }
                }
        }
    }

    fun addActivity(activity: ActivityEntity) {
        Log.d(TAG, "Adding activity: $activity")
        viewModelScope.launch {
            try {
                repository.insertActivity(activity)
                Log.d(TAG, "Activity added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding activity", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        Log.d(TAG, "Updating activity: $activity")
        viewModelScope.launch {
            try {
                repository.updateActivity(activity)
                Log.d(TAG, "Activity updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating activity", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        Log.d(TAG, "Deleting activity: $activity")
        viewModelScope.launch {
            try {
                repository.deleteActivity(activity)
                Log.d(TAG, "Activity deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting activity", e)
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun getTodayStats(): Flow<Map<String, Int>> = flow {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        val steps = repository.getTotalByTypeAndDateRange("Steps", startOfDay, endOfDay)
        val calories = repository.getTotalByTypeAndDateRange("Calories", startOfDay, endOfDay)
        val workout = repository.getTotalByTypeAndDateRange("Workout", startOfDay, endOfDay)

        Log.d(TAG, "Today's stats - Steps: $steps, Calories: $calories, Workout: $workout")
        emit(mapOf(
            "Steps" to steps,
            "Calories" to calories,
            "Workout" to workout
        ))
    }

    fun getWeeklyStats(): Flow<Map<String, Int>> = flow {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        val endOfWeek = System.currentTimeMillis()

        val steps = repository.getTotalByTypeAndDateRange("Steps", startOfWeek, endOfWeek)
        val calories = repository.getTotalByTypeAndDateRange("Calories", startOfWeek, endOfWeek)
        val workout = repository.getTotalByTypeAndDateRange("Workout", startOfWeek, endOfWeek)

        Log.d(TAG, "Weekly stats - Steps: $steps, Calories: $calories, Workout: $workout")
        emit(mapOf(
            "Steps" to steps,
            "Calories" to calories,
            "Workout" to workout
        ))
    }

    fun calculateCaloriesBurned(activityType: String, duration: Int, weight: Float): Int {
        return repository.calculateCaloriesBurned(activityType, duration, weight)
    }

    fun calculateStepGoalProgress(currentSteps: Int, goalSteps: Int): Float {
        return repository.calculateStepGoalProgress(currentSteps, goalSteps)
    }

    // User preferences methods
    fun setDarkTheme(enabled: Boolean) {
        Log.d(TAG, "Setting dark theme: $enabled")
        viewModelScope.launch {
            userPreferences.setDarkTheme(enabled)
        }
    }

    fun setDailyStepGoal(goal: Int) {
        Log.d(TAG, "Setting daily step goal: $goal")
        viewModelScope.launch {
            userPreferences.setDailyStepGoal(goal)
        }
    }

    fun setDailyCalorieGoal(goal: Int) {
        Log.d(TAG, "Setting daily calorie goal: $goal")
        viewModelScope.launch {
            userPreferences.setDailyCalorieGoal(goal)
        }
    }

    fun setUserName(name: String) {
        Log.d(TAG, "Setting user name: $name")
        viewModelScope.launch {
            userPreferences.setUserName(name)
        }
    }

    fun setUserWeight(weight: Float) {
        Log.d(TAG, "Setting user weight: $weight")
        viewModelScope.launch {
            userPreferences.setUserWeight(weight)
        }
    }

    fun setUserHeight(height: Float) {
        Log.d(TAG, "Setting user height: $height")
        viewModelScope.launch {
            userPreferences.setUserHeight(height)
        }
    }

    fun setFirstLaunchComplete() {
        Log.d(TAG, "Setting first launch complete")
        viewModelScope.launch {
            userPreferences.setFirstLaunchComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, tipsError = null) }
    }
}
