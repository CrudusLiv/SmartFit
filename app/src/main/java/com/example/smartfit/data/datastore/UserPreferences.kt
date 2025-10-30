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

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setActiveProfileId(profileId: String?) {
        context.dataStore.edit { preferences ->
            if (profileId.isNullOrBlank()) {
                preferences.remove(ACTIVE_PROFILE_ID)
            } else {
                preferences[ACTIVE_PROFILE_ID] = profileId
            }
        }
    }

    suspend fun upsertProfile(profile: StoredProfile) {
        context.dataStore.edit { preferences ->
            val current = preferences[SAVED_PROFILES]?.let(::deserializeProfiles)?.toMutableList()
                ?: mutableListOf()
            val existingIndex = current.indexOfFirst { it.id == profile.id }
            if (existingIndex >= 0) {
                current[existingIndex] = profile
            } else {
                current += profile
            }
            preferences[SAVED_PROFILES] = serializeProfiles(current)
        }
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

