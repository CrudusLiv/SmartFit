package com.example.smartfit.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private const val TAG = "UserPreferences"
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        private val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_WEIGHT = floatPreferencesKey("user_weight")
        private val USER_HEIGHT = floatPreferencesKey("user_height")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading dark theme preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DARK_THEME] ?: false
        }

    val dailyStepGoal: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading step goal preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DAILY_STEP_GOAL] ?: 10000
        }

    val dailyCalorieGoal: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading calorie goal preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DAILY_CALORIE_GOAL] ?: 2000
        }

    val userName: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user name preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_NAME] ?: ""
        }

    val userWeight: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user weight preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_WEIGHT] ?: 70f
        }

    val userHeight: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading user height preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[USER_HEIGHT] ?: 170f
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading first launch preference", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        Log.d(TAG, "Setting dark theme: $enabled")
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        Log.d(TAG, "Setting daily step goal: $goal")
        context.dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL] = goal
        }
    }

    suspend fun setDailyCalorieGoal(goal: Int) {
        Log.d(TAG, "Setting daily calorie goal: $goal")
        context.dataStore.edit { preferences ->
            preferences[DAILY_CALORIE_GOAL] = goal
        }
    }

    suspend fun setUserName(name: String) {
        Log.d(TAG, "Setting user name: $name")
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun setUserWeight(weight: Float) {
        Log.d(TAG, "Setting user weight: $weight")
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT] = weight
        }
    }

    suspend fun setUserHeight(height: Float) {
        Log.d(TAG, "Setting user height: $height")
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT] = height
        }
    }

    suspend fun setFirstLaunchComplete() {
        Log.d(TAG, "Setting first launch complete")
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
}

