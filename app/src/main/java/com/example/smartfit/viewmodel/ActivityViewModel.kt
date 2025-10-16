package com.example.smartfit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.datastore.UserPreferences
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.data.remote.ExerciseInfo
import com.example.smartfit.data.remote.Post
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class ActivityUiState(
    val activities: List<ActivityEntity> = emptyList(),
    val activitiesLoading: Boolean = false,
    val activitiesError: String? = null,
    val workoutSuggestions: List<WorkoutSuggestion> = emptyList(),
    val suggestionsLoading: Boolean = false,
    val suggestionsError: String? = null,
    val tips: List<Post> = emptyList(),
    val tipsLoading: Boolean = false,
    val tipsError: String? = null,
    val exercises: List<ExerciseInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalCount: Int = 0
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
    ) { values ->
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
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferencesState()
    )

    init {
        observeActivities()
        loadTips()
        loadWorkoutSuggestions()
    }

    private fun observeActivities() {
        viewModelScope.launch {
            _uiState.update { it.copy(activitiesLoading = true, activitiesError = null) }
            repository.getAllActivities()
                .catch { throwable ->
                    Log.e(TAG, "Error loading activities", throwable)
                    _uiState.update {
                        it.copy(
                            activities = emptyList(),
                            activitiesLoading = false,
                            activitiesError = throwable.message
                        )
                    }
                }
                .collect { activities ->
                    _uiState.update {
                        it.copy(
                            activities = activities,
                            activitiesLoading = false,
                            activitiesError = null
                        )
                    }
                }
        }
    }

    fun loadTips(limit: Int = 10) {
        viewModelScope.launch {
            repository.getTipsFromNetwork(limit).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(tipsLoading = true, tipsError = null) }
                    is Result.Success -> _uiState.update {
                        it.copy(
                            tips = result.data,
                            tipsLoading = false,
                            tipsError = null
                        )
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(
                            tipsLoading = false,
                            tipsError = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun loadExercises(limit: Int = 20, offset: Int = 0) {
        viewModelScope.launch {
            repository.getExercisesFromWger(limit, offset).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _uiState.update {
                        it.copy(
                            exercises = result.data.results,
                            totalCount = result.data.count ?: 0,
                            isLoading = false,
                            error = null
                        )
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun loadWorkoutSuggestions(limit: Int = 8, offset: Int = 0) {
        viewModelScope.launch {
            repository.getWorkoutSuggestions(limit, offset).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update {
                        it.copy(suggestionsLoading = true, suggestionsError = null)
                    }

                    is Result.Success -> _uiState.update {
                        it.copy(
                            workoutSuggestions = result.data,
                            suggestionsLoading = false,
                            suggestionsError = null
                        )
                    }

                    is Result.Error -> _uiState.update {
                        it.copy(
                            suggestionsLoading = false,
                            suggestionsError = result.exception.message
                        )
                    }
                }
            }
        }
    }

    fun addActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                repository.insertActivity(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding activity", e)
                _uiState.update { it.copy(activitiesError = e.message) }
            }
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                repository.updateActivity(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating activity", e)
                _uiState.update { it.copy(activitiesError = e.message) }
            }
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            try {
                repository.deleteActivity(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting activity", e)
                _uiState.update { it.copy(activitiesError = e.message) }
            }
        }
    }

    fun getTodayStats(): Flow<Map<String, Int>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + TimeUnit.DAYS.toMillis(1) - 1

        return repository.getActivitiesByDateRange(startOfDay, endOfDay)
            .map { aggregateStats(it) }
            .flowOn(Dispatchers.IO)
    }

    fun getWeeklyStats(): Flow<Map<String, Int>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endOfRange = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfRange = calendar.timeInMillis

        return repository.getActivitiesByDateRange(startOfRange, endOfRange)
            .map { aggregateStats(it) }
            .flowOn(Dispatchers.IO)
    }

    fun calculateCaloriesBurned(activityType: String, duration: Int, weight: Float): Int =
        repository.calculateCaloriesBurned(activityType, duration, weight)

    fun calculateStepGoalProgress(currentSteps: Int, goalSteps: Int): Float =
        repository.calculateStepGoalProgress(currentSteps, goalSteps)

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setDarkTheme(enabled) }
    }

    fun setDailyStepGoal(goal: Int) {
        viewModelScope.launch { userPreferences.setDailyStepGoal(goal) }
    }

    fun setDailyCalorieGoal(goal: Int) {
        viewModelScope.launch { userPreferences.setDailyCalorieGoal(goal) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { userPreferences.setUserName(name) }
    }

    fun setUserWeight(weight: Float) {
        viewModelScope.launch { userPreferences.setUserWeight(weight) }
    }

    fun setUserHeight(height: Float) {
        viewModelScope.launch { userPreferences.setUserHeight(height) }
    }

    fun setFirstLaunchComplete() {
        viewModelScope.launch { userPreferences.setFirstLaunchComplete() }
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                error = null,
                tipsError = null,
                activitiesError = null,
                suggestionsError = null
            )
        }
    }

    private fun aggregateStats(activities: List<ActivityEntity>): Map<String, Int> {
        val totals = activities.groupBy { it.type }.mapValues { entry ->
            entry.value.sumOf { activity -> activity.value }
        }

        return mapOf(
            "Steps" to (totals["Steps"] ?: 0),
            "Calories" to (totals["Calories"] ?: 0),
            "Workout" to (totals["Workout"] ?: 0)
        )
    }
}
