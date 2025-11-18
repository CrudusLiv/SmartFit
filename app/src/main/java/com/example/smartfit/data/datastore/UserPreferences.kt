package com.example.smartfit.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        private const val TAG = "UserPreferences"
        private const val DEMO_PROFILE_ID = "smartfit-demo"
        private const val DEMO_DISPLAY_NAME = "SmartFit Demo"
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val DAILY_STEP_GOAL = intPreferencesKey("daily_step_goal")
        private val DAILY_CALORIE_GOAL = intPreferencesKey("daily_calorie_goal")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_WEIGHT = floatPreferencesKey("user_weight")
        private val USER_HEIGHT = floatPreferencesKey("user_height")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        private val SAVED_PROFILES = stringPreferencesKey("saved_profiles")
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

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val activeProfileId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_PROFILE_ID]
    }

    val savedProfiles: Flow<List<StoredProfile>> = context.dataStore.data.map { preferences ->
        preferences[SAVED_PROFILES]?.let(::deserializeProfiles) ?: emptyList()
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        Log.d(TAG, "Setting dark theme preference: $enabled")
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
        Log.i(TAG, "Dark theme preference saved: $enabled")
    }

    suspend fun setDailyStepGoal(goal: Int) {
        Log.d(TAG, "Setting daily step goal: $goal steps")
        context.dataStore.edit { preferences ->
            preferences[DAILY_STEP_GOAL] = goal
        }
        Log.i(TAG, "Daily step goal saved: $goal steps")
    }

    suspend fun setDailyCalorieGoal(goal: Int) {
        Log.d(TAG, "Setting daily calorie goal: $goal calories")
        context.dataStore.edit { preferences ->
            preferences[DAILY_CALORIE_GOAL] = goal
        }
        Log.i(TAG, "Daily calorie goal saved: $goal calories")
    }

    suspend fun setUserName(name: String) {
        Log.d(TAG, "Setting user name: $name")
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
        Log.i(TAG, "User name saved: $name")
    }

    suspend fun setUserWeight(weight: Float) {
        Log.d(TAG, "Setting user weight: ${weight}kg")
        context.dataStore.edit { preferences ->
            preferences[USER_WEIGHT] = weight
        }
        Log.i(TAG, "User weight saved: ${weight}kg")
    }

    suspend fun setUserHeight(height: Float) {
        Log.d(TAG, "Setting user height: ${height}cm")
        context.dataStore.edit { preferences ->
            preferences[USER_HEIGHT] = height
        }
        Log.i(TAG, "User height saved: ${height}cm")
    }

    suspend fun setFirstLaunchComplete() {
        Log.d(TAG, "Marking first launch as complete")
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
        Log.i(TAG, "First launch flag cleared")
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        Log.d(TAG, "Setting logged in status: $isLoggedIn")
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
        Log.i(TAG, "Logged in status saved: $isLoggedIn")
    }

    suspend fun setActiveProfileId(profileId: String?) {
        Log.d(TAG, "Setting active profile ID: $profileId")
        context.dataStore.edit { preferences ->
            if (profileId.isNullOrBlank()) {
                preferences.remove(ACTIVE_PROFILE_ID)
                Log.i(TAG, "Active profile ID cleared")
            } else {
                preferences[ACTIVE_PROFILE_ID] = profileId
                Log.i(TAG, "Active profile ID saved: $profileId")
            }
        }
    }

    suspend fun upsertProfile(profile: StoredProfile) {
        Log.d(TAG, "Upserting profile: ${profile.id} - ${profile.displayName}")
        context.dataStore.edit { preferences ->
            val current = preferences[SAVED_PROFILES]?.let(::deserializeProfiles)?.toMutableList()
                ?: mutableListOf()
            val existingIndex = current.indexOfFirst { it.id == profile.id }
            if (existingIndex >= 0) {
                current[existingIndex] = profile
                Log.i(TAG, "Profile updated: ${profile.id}")
            } else {
                current += profile
                Log.i(TAG, "Profile created: ${profile.id}")
            }
            preferences[SAVED_PROFILES] = serializeProfiles(current)
        }
        Log.i(TAG, "Profile upserted successfully: ${profile.displayName}")
    }

    suspend fun removeProfile(profileId: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[SAVED_PROFILES]?.let(::deserializeProfiles)?.toMutableList()
                ?: mutableListOf()
            val updated = current.filterNot { it.id == profileId }
            if (updated.size != current.size) {
                preferences[SAVED_PROFILES] = serializeProfiles(updated)
            }
            val activeId = preferences[ACTIVE_PROFILE_ID]
            if (activeId == profileId) {
                preferences.remove(ACTIVE_PROFILE_ID)
                preferences[IS_LOGGED_IN] = false
            }
        }
    }

    suspend fun getProfileById(profileId: String): StoredProfile? {
        return context.dataStore.data.first()[SAVED_PROFILES]
            ?.let(::deserializeProfiles)
            ?.firstOrNull { it.id == profileId }
    }

    suspend fun updateProfileName(profileId: String?, newName: String) {
        if (profileId.isNullOrBlank()) return
        context.dataStore.edit { preferences ->
            val current = preferences[SAVED_PROFILES]?.let(::deserializeProfiles)?.toMutableList()
                ?: return@edit
            val index = current.indexOfFirst { it.id == profileId }
            if (index >= 0) {
                current[index] = current[index].copy(displayName = newName)
                preferences[SAVED_PROFILES] = serializeProfiles(current)
            }
        }
    }

    suspend fun normalizeAndCreateProfile(rawEmail: String, fallbackName: String): StoredProfile {
        val trimmedEmail = rawEmail.trim()
        val normalizedId = trimmedEmail.lowercase(Locale.getDefault()).ifBlank {
            fallbackName.lowercase(Locale.getDefault())
        }
        val displayName = fallbackName.ifBlank {
            trimmedEmail.substringBefore('@').ifBlank { "SmartFit Member" }
        }
        val profile = StoredProfile(
            id = normalizedId,
            displayName = displayName,
            email = trimmedEmail.ifBlank { null }
        )
        upsertProfile(profile)
        setActiveProfileId(profile.id)
        return profile
    }

    suspend fun upsertProfileFromGoogleAccount(
        accountId: String?,
        displayName: String?,
        email: String?
    ): StoredProfile {
        val sanitizedEmail = email?.trim()?.takeIf { it.isNotBlank() }
        val normalizedId = when {
            !accountId.isNullOrBlank() -> accountId
            !sanitizedEmail.isNullOrBlank() -> sanitizedEmail.lowercase(Locale.getDefault())
            !displayName.isNullOrBlank() -> displayName.lowercase(Locale.getDefault())
                .replace("\\s+".toRegex(), "_")
            else -> "smartfit-${UUID.randomUUID()}"
        }

        val resolvedDisplayName = when {
            !displayName.isNullOrBlank() -> displayName
            !sanitizedEmail.isNullOrBlank() -> sanitizedEmail.substringBefore('@')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            else -> "SmartFit Member"
        }

        val profile = StoredProfile(
            id = normalizedId,
            displayName = resolvedDisplayName,
            email = sanitizedEmail
        )

        upsertProfile(profile)
        setActiveProfileId(profile.id)
        return profile
    }

    suspend fun upsertDemoProfile(): StoredProfile {
        val profile = StoredProfile(
            id = DEMO_PROFILE_ID,
            displayName = DEMO_DISPLAY_NAME,
            email = null
        )
        upsertProfile(profile)
        setActiveProfileId(profile.id)
        return profile
    }
}

private fun serializeProfiles(profiles: List<StoredProfile>): String {
    val array = JSONArray()
    profiles.forEach { profile ->
        val obj = JSONObject().apply {
            put("id", profile.id)
            put("displayName", profile.displayName)
            profile.email?.let { put("email", it) }
        }
        array.put(obj)
    }
    return array.toString()
}

private fun deserializeProfiles(raw: String): List<StoredProfile> {
    return try {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val obj = array.optJSONObject(index) ?: continue
                val id = obj.optString("id").takeIf { it.isNotBlank() } ?: continue
                val name = obj.optString("displayName").takeIf { it.isNotBlank() } ?: id
                val email = obj.optString("email").takeIf { it.isNotBlank() }
                add(StoredProfile(id = id, displayName = name, email = email))
            }
        }
    } catch (_: JSONException) {
        emptyList()
    }
}

