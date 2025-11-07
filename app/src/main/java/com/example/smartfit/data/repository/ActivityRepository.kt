package com.example.smartfit.data.repository

import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.data.remote.ExerciseImage
import com.example.smartfit.data.remote.ExerciseInfo
import com.example.smartfit.data.remote.ExerciseInfoResponse
import com.example.smartfit.data.remote.WgerRemoteDataSource
import com.example.smartfit.google.GoogleFitDataSource
import com.example.smartfit.google.GoogleFitDataSource.FitWorkout
import java.util.Locale
import kotlin.math.min
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
    private val googleFitDataSource: GoogleFitDataSource,
    private val wgerRemoteDataSource: WgerRemoteDataSource
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
            val fitResponse = googleFitDataSource.fetchExercises(limit = limit, offset = offset)
            val wgerResponse = try {
                wgerRemoteDataSource.fetchExercises(limit = limit, offset = offset)
            } catch (catalogueError: Exception) {
                Log.w(TAG, "Unable to load catalogue exercises", catalogueError)
                fitResponse
            }
            val merged = mergeExerciseResponses(fitResponse, wgerResponse, limit)
            emit(Result.Success(merged))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching exercises", e)
            try {
                val fallback = wgerRemoteDataSource.fetchExercises(limit = limit, offset = offset)
                emit(Result.Success(fallback))
            } catch (_: Exception) {
                emit(Result.Error(e))
            }
        }
    }.catch { throwable ->
        emit(Result.Error(Exception(throwable)))
    }.flowOn(Dispatchers.IO)

    fun getWorkoutSuggestions(limit: Int = 12, offset: Int = 0): Flow<Result<List<WorkoutSuggestion>>> = flow {
        emit(Result.Loading)
        try {
            val fetchLimit = (limit * 3).coerceAtLeast(limit)
            val workouts = googleFitDataSource.fetchWorkouts(limit = fetchLimit, offset = offset)
            val googleSuggestions = workouts
                .asSequence()
                .map(::mapWorkoutToSuggestion)
                .distinctBy { it.id }
                .toList()
            val remoteLimit = min(limit * 2, 60)
            val cataloguePrimary = try {
                fetchCatalogueSuggestions(remoteLimit, offset)
            } catch (catalogueError: Exception) {
                Log.w(TAG, "Unable to load catalogue suggestions", catalogueError)
                emptyList()
            }

            var merged = blendSuggestions(limit, cataloguePrimary, googleSuggestions)

            if (merged.size < limit) {
                val additional = try {
                    fetchCatalogueSuggestions((limit - merged.size) * 2, offset + remoteLimit)
                } catch (catalogueError: Exception) {
                    Log.w(TAG, "Additional catalogue fetch failed", catalogueError)
                    emptyList()
                }
                val expandedCatalogue = cataloguePrimary + additional
                merged = blendSuggestions(limit, expandedCatalogue, googleSuggestions)
            }

            emit(Result.Success(merged))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workout suggestions", e)
            try {
                val fallback = fetchCatalogueSuggestions(limit * 2, offset)
                if (fallback.isNotEmpty()) {
                    emit(Result.Success(fallback.take(limit)))
                } else {
                    emit(Result.Error(e))
                }
            } catch (catalogueError: Exception) {
                emit(Result.Error(e))
            }
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

    private suspend fun fetchCatalogueSuggestions(limit: Int, offset: Int): List<WorkoutSuggestion> {
        val safeLimit = limit.coerceIn(1, 100)
        val response = wgerRemoteDataSource.fetchExercises(limit = safeLimit, offset = offset)
        return response.results
            .asSequence()
            .mapIndexed { index, info -> mapExerciseToSuggestion(info, offset + index) }
            .filterNotNull()
            .distinctBy { it.id }
            .toList()
    }
}

private fun blendSuggestions(
    limit: Int,
    vararg sources: List<WorkoutSuggestion>
): List<WorkoutSuggestion> {
    if (sources.isEmpty()) return emptyList()
    val result = mutableListOf<WorkoutSuggestion>()
    val seen = mutableSetOf<Int>()
    var index = 0
    while (result.size < limit) {
        var added = false
        for (source in sources) {
            if (index < source.size) {
                val candidate = source[index]
                if (seen.add(candidate.id)) {
                    result += candidate
                    added = true
                    if (result.size == limit) break
                }
            }
        }
        if (!added) break
        index++
    }
    return result
}

private fun mergeExerciseResponses(
    fitResponse: ExerciseInfoResponse,
    wgerResponse: ExerciseInfoResponse,
    limit: Int
): ExerciseInfoResponse {
    val merged = mutableListOf<ExerciseInfo>()
    val seen = mutableSetOf<Int>()

    fun addUnique(info: ExerciseInfo) {
        if (merged.size >= limit) return
        val key = uniqueExerciseId(info)
        if (seen.add(key)) {
            merged += info
        }
    }

    wgerResponse.results.forEach(::addUnique)
    if (merged.size < limit) {
        fitResponse.results.forEach(::addUnique)
    }

    val candidateCounts = listOfNotNull(
        wgerResponse.count?.takeIf { it > 0 },
        fitResponse.count?.takeIf { it > 0 },
        merged.size
    )

    val totalCount = candidateCounts.maxOrNull() ?: merged.size

    return ExerciseInfoResponse(
        count = totalCount,
        next = wgerResponse.next ?: fitResponse.next,
        previous = wgerResponse.previous ?: fitResponse.previous,
        results = merged
    )
}

private fun uniqueExerciseId(info: ExerciseInfo): Int =
    info.id ?: info.name?.hashCode() ?: info.hashCode()

private fun mapExerciseToSuggestion(info: ExerciseInfo, position: Int): WorkoutSuggestion? {
    val id = info.id ?: (info.name?.hashCode() ?: position)
    val name = info.name?.takeIf { it.isNotBlank() } ?: return null
    val category = info.category?.name?.takeIf { it.isNotBlank() } ?: "General"
    val muscles = info.muscles.mapNotNull { it.name_en ?: it.name }.filter { it.isNotBlank() }
    val equipment = info.equipment.mapNotNull { it.name }.filter { it.isNotBlank() }
    val imageUrl = resolveImageUrl(info.images)
    val description = sanitizeDescription(info.description)
    val intensity = guessIntensity(category)

    return WorkoutSuggestion(
        id = id,
        name = name.trim(),
        category = category.trim(),
        primaryMuscles = muscles.ifEmpty { listOf(category) },
        equipment = equipment.ifEmpty { listOf("Bodyweight") },
        imageUrl = imageUrl,
        description = description,
        durationMinutes = defaultDuration(category),
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = intensity,
        effortScore = defaultEffort(intensity),
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    )
}

private fun resolveImageUrl(images: List<ExerciseImage>): String? {
    val candidate = images.firstOrNull { it.is_main == true } ?: images.firstOrNull()
    val raw = candidate?.image?.trim().orEmpty()
    if (raw.isBlank()) return null
    return if (raw.startsWith("http")) raw else "https://wger.de$raw"
}

private fun sanitizeDescription(raw: String?): String {
    if (raw.isNullOrBlank()) return "No description available."
    val text = HtmlCompat.fromHtml(raw, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
    if (text.isNotBlank()) return text
    return raw.replace(Regex("<[^>]*>"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .ifBlank { "No description available." }
}

private fun guessIntensity(category: String): String {
    val lower = category.lowercase(Locale.getDefault())
    return when {
        listOf("hiit", "plyometric", "crossfit").any(lower::contains) -> "High intensity"
        listOf("strength", "power", "legs", "arms").any(lower::contains) -> "Strength focus"
        listOf("yoga", "stretch", "mobility", "pilates").any(lower::contains) -> "Mobility"
        listOf("cardio", "run", "walk", "cycle", "row").any(lower::contains) -> "Cardio"
        else -> "General fitness"
    }
}

private fun defaultDuration(category: String): Int {
    val lower = category.lowercase(Locale.getDefault())
    return when {
        listOf("hiit", "plyometric").any(lower::contains) -> 15
        listOf("strength", "power").any(lower::contains) -> 25
        listOf("mobility", "yoga", "pilates").any(lower::contains) -> 20
        else -> 18
    }
}

private fun defaultEffort(intensity: String): Int = when (intensity) {
    "High intensity" -> 70
    "Strength focus" -> 60
    "Cardio" -> 55
    "Mobility" -> 35
    else -> 45
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
        imageUrl = workoutImageUrl(workout.activity),
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

private fun workoutImageUrl(activityName: String): String? {
    val normalized = activityName.lowercase(Locale.getDefault())
    return when {
        normalized.contains("run") -> "https://images.unsplash.com/photo-1546484959-f9a94a30713e?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("walk") -> "https://images.unsplash.com/photo-1526403228-eda4a702e8c8?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("cycle") || normalized.contains("bike") -> "https://images.unsplash.com/photo-1507831228885-004b65a06f4c?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("swim") -> "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("yoga") -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("strength") || normalized.contains("gym") -> "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80"
        normalized.contains("hiit") || normalized.contains("interval") -> "https://images.unsplash.com/photo-1546484955-0bce7fefc357?auto=format&fit=crop&w=1200&q=80"
        else -> "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=1200&q=80"
    }
}
