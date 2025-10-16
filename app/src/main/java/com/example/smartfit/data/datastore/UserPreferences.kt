package com.example.smartfit.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        private val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_WEIGHT = floatPreferencesKey("user_weight")
        private val USER_HEIGHT = floatPreferencesKey("user_height")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME] ?: false
    }

    val dailyStepGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_STEP_GOAL] ?: 10000
    }

    val dailyCalorieGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_CALORIE_GOAL] ?: 2000
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: ""
    }

    val userWeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[USER_WEIGHT] ?: 70f
    }

    val userHeight: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[USER_HEIGHT] ?: 170f
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL] = goal
        }
    }

    suspend fun setDailyCalorieGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_CALORIE_GOAL] = goal
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    suspend fun setUserWeight(weight: Float) {
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT] = weight
        }
    }

    suspend fun setUserHeight(height: Float) {
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT] = height
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
}

