package com.example.smartfit.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.smartfit.data.local.ActivityDao
import com.example.smartfit.data.local.ActivityEntity
import com.example.smartfit.data.model.WorkoutSuggestion
import com.example.smartfit.data.remote.ExerciseImage
import com.example.smartfit.data.remote.ExerciseInfo
import com.example.smartfit.data.remote.ExerciseInfoResponse
import com.example.smartfit.data.remote.WgerRemoteDataSource
import java.util.Locale
import kotlin.jvm.Volatile
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val wgerRemoteDataSource: WgerRemoteDataSource
) {
    companion object {
        private const val TAG = "ActivityRepository"
        private const val CATALOGUE_CACHE_TTL_MS = 6 * 60 * 60 * 1000L
        private const val WGER_PAGE_SIZE = 100
        private const val MAX_WGER_PAGES = 60
        private const val MAX_CATALOGUE_SAMPLE = 250
    }

    private val catalogueCacheMutex = Mutex()

    @Volatile
    private var catalogueCache: CatalogueCache? = null

    @Volatile
    private var lastCatalogueRefresh = 0L

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

    fun getExercises(limit: Int = 20, offset: Int = 0): Flow<Result<ExerciseInfoResponse>> = flow {
        emit(Result.Loading)
        try {
            var cache = ensureCatalogueCacheLoaded()
            if (cache.exercises.isEmpty()) {
                cache = ensureCatalogueCacheLoaded(forceRefresh = true)
            }

            val safeOffset = offset.coerceAtLeast(0)
            val safeLimit = limit.coerceAtLeast(1)

            if (cache.exercises.isEmpty()) {
                throw IllegalStateException("Catalogue is unavailable")
            }

            val results = cache.exercises.drop(safeOffset).take(safeLimit)
            val response = ExerciseInfoResponse(
                count = cache.exercises.size,
                next = null,
                previous = null,
                results = results
            )
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
            Log.d(TAG, "Loading workout suggestions: limit=$limit, offset=$offset")
            var cache = ensureCatalogueCacheLoaded()
            Log.d(TAG, "Initial cache loaded: ${cache.suggestions.size} suggestions available")
            
            if (cache.suggestions.isEmpty()) {
                Log.w(TAG, "Cache empty, forcing refresh")
                cache = ensureCatalogueCacheLoaded(forceRefresh = true)
                Log.d(TAG, "After refresh: ${cache.suggestions.size} suggestions available")
            }

            val targetCount = if (limit > 0) limit else 1
            val suggestions = if (cache.suggestions.isNotEmpty()) {
                val pool = if (cache.suggestions.size > MAX_CATALOGUE_SAMPLE) {
                    cache.suggestions.shuffled(Random(System.nanoTime())).take(MAX_CATALOGUE_SAMPLE)
                } else {
                    cache.suggestions
                }
                if (pool.size <= targetCount) pool else pool.shuffled(Random(System.nanoTime())).take(targetCount)
            } else {
                Log.w(TAG, "Cache still empty after refresh, using fallback")
                emptyList()
            }

            val finalSuggestions = if (suggestions.isEmpty()) {
                Log.i(TAG, "Using fallback workout suggestions")
                fallbackWorkoutSuggestions()
                    .shuffled(Random(System.nanoTime()))
                    .take(targetCount)
            } else {
                Log.i(TAG, "Returning ${suggestions.size} Wger suggestions")
                suggestions
            }

            emit(Result.Success(finalSuggestions))
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming workout suggestions", e)
            val targetCount = if (limit > 0) limit else 1
            val fallback = fallbackWorkoutSuggestions()
                .shuffled(Random(System.nanoTime()))
                .take(targetCount)
            emit(Result.Success(fallback))
        }
    }.catch { throwable ->
        Log.e(TAG, "Error streaming workout suggestions", throwable)
        val targetCount = if (limit > 0) limit else 1
        val fallback = fallbackWorkoutSuggestions()
            .shuffled(Random(System.nanoTime()))
            .take(targetCount)
        emit(Result.Success(fallback))
    }.flowOn(Dispatchers.IO)

    suspend fun testWgerApiConnection(): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Testing Wger API connection...")
                val response = wgerRemoteDataSource.fetchExercises(limit = 1, offset = 0)
                val message = "✅ API Connected! Count: ${response.count}, Results: ${response.results.size}"
                Log.i(TAG, message)
                if (response.results.isNotEmpty()) {
                    val first = response.results[0]
                    Log.i(TAG, "First exercise: id=${first.id}, name=${first.name}")
                }
                message
            } catch (e: Exception) {
                val message = "❌ API Error: ${e.message}"
                Log.e(TAG, message, e)
                message
            }
        }
    }

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

    suspend fun refreshCatalogueCache() {
        catalogueCacheMutex.withLock {
            catalogueCache = null
            lastCatalogueRefresh = 0L
        }
    }

    private suspend fun ensureCatalogueCacheLoaded(forceRefresh: Boolean = false): CatalogueCache {
        val now = System.currentTimeMillis()
        val cachedSnapshot = catalogueCache
        val cacheStale = cachedSnapshot == null || now - lastCatalogueRefresh > CATALOGUE_CACHE_TTL_MS
        if (!forceRefresh && cachedSnapshot != null && !cacheStale) {
            return cachedSnapshot
        }

        return catalogueCacheMutex.withLock {
            val innerSnapshot = catalogueCache
            val innerStale = innerSnapshot == null || forceRefresh || System.currentTimeMillis() - lastCatalogueRefresh > CATALOGUE_CACHE_TTL_MS
            if (!innerStale && innerSnapshot != null) {
                return@withLock innerSnapshot
            }

            val loaded = loadFullCatalogue()
            if (loaded.suggestions.isNotEmpty()) {
                catalogueCache = loaded
                lastCatalogueRefresh = System.currentTimeMillis()
            } else if (catalogueCache != null && !forceRefresh) {
                return@withLock catalogueCache!!
            }

            return@withLock loaded
        }
    }

    private suspend fun loadFullCatalogue(): CatalogueCache {
        val exerciseMap = linkedMapOf<Int, ExerciseInfo>()
        val suggestionMap = linkedMapOf<Int, WorkoutSuggestion>()

        var offsetCursor = 0
        var pageCount = 0

        Log.d(TAG, "Starting full catalogue load from Wger API")

        while (pageCount < MAX_WGER_PAGES) {
            val response = try {
                Log.d(TAG, "Fetching page $pageCount at offset $offsetCursor")
                wgerRemoteDataSource.fetchExercises(limit = WGER_PAGE_SIZE, offset = offsetCursor)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch Wger catalogue page $pageCount at offset $offsetCursor", e)
                if (exerciseMap.isEmpty()) throw e else break
            }

            Log.d(TAG, "Page $pageCount returned ${response.results.size} results, total count: ${response.count}")

            if (response.results.isEmpty()) {
                Log.w(TAG, "Empty results at page $pageCount, stopping")
                break
            }

            response.results.forEachIndexed { index, info ->
                val key = uniqueExerciseKey(info, offsetCursor + index)
                if (!exerciseMap.containsKey(key)) {
                    exerciseMap[key] = info
                }
                val suggestion = mapExerciseToSuggestion(info, offsetCursor + index)
                if (suggestion != null) {
                    suggestionMap.putIfAbsent(suggestion.id, suggestion)
                }
            }

            val nextOffset = parseOffsetFromNext(response.next)
            if (nextOffset != null && nextOffset > offsetCursor) {
                offsetCursor = nextOffset
            } else {
                offsetCursor += response.results.size
            }

            if (response.next.isNullOrBlank()) {
                Log.d(TAG, "No next page URL, stopping at page $pageCount")
                break
            }
            pageCount++
        }

        Log.i(TAG, "Catalogue load complete: ${exerciseMap.size} exercises, ${suggestionMap.size} suggestions")

        return CatalogueCache(
            exercises = exerciseMap.values.toList(),
            suggestions = suggestionMap.values.toList()
        )
    }

    private fun parseOffsetFromNext(next: String?): Int? {
        if (next.isNullOrBlank()) return null
        return runCatching {
            Uri.parse(next).getQueryParameter("offset")?.toInt()
        }.getOrNull()
    }

}

private const val TAG = "ActivityRepository"

private fun mapExerciseToSuggestion(info: ExerciseInfo, position: Int): WorkoutSuggestion? {
    val id = uniqueExerciseKey(info, position)
    
    // The exerciseinfo endpoint doesn't have a name field, construct one from available data
    val categoryName = info.category?.name?.takeIf { it.isNotBlank() } ?: "Exercise"
    
    // Try to extract a meaningful name from the description or use category + ID
    val name = when {
        !info.description.isNullOrBlank() -> {
            // Extract first sentence or first 50 chars from description as name
            val desc = HtmlCompat.fromHtml(info.description, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim()
            val firstLine = desc.lines().firstOrNull { it.isNotBlank() }?.take(50) ?: categoryName
            if (firstLine.length < 3) "$categoryName Exercise" else firstLine
        }
        info.id != null -> "$categoryName Exercise #${info.id}"
        else -> "$categoryName Exercise"
    }
    
    Log.d(TAG, "Mapped exercise $id: name='$name', category='$categoryName'")
    
    val muscles = info.muscles.mapNotNull { it.name_en ?: it.name }.filter { it.isNotBlank() }
    val equipment = info.equipment.mapNotNull { it.name }.filter { it.isNotBlank() }
    val imageUrl = resolveImageUrl(info.images)
    val description = sanitizeDescription(info.description)
    val intensity = guessIntensity(categoryName)

    return WorkoutSuggestion(
        id = id,
        name = name.trim(),
        category = categoryName.trim(),
        primaryMuscles = muscles.ifEmpty { listOf(categoryName) },
        equipment = equipment.ifEmpty { listOf("Bodyweight") },
        imageUrl = imageUrl,
        description = description,
        durationMinutes = defaultDuration(categoryName),
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

private fun fallbackWorkoutSuggestions(): List<WorkoutSuggestion> = listOf(
    WorkoutSuggestion(
        id = -101,
        name = "Total Body HIIT",
        category = "HIIT",
        primaryMuscles = listOf("Full Body"),
        equipment = listOf("Bodyweight"),
        imageUrl = "https://images.unsplash.com/photo-1546484955-0bce7fefc357?auto=format&fit=crop&w=1200&q=80",
        description = "A fast-paced circuit mixing squats, burpees, and mountain climbers to spike your heart rate.",
        durationMinutes = 18,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "High intensity",
        effortScore = 70,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    ),
    WorkoutSuggestion(
        id = -102,
        name = "Strength Push Day",
        category = "Strength",
        primaryMuscles = listOf("Chest", "Shoulders", "Triceps"),
        equipment = listOf("Dumbbells"),
        imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?auto=format&fit=crop&w=1200&q=80",
        description = "Pressing-focused session alternating between push-ups, overhead presses, and dips.",
        durationMinutes = 25,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "Strength focus",
        effortScore = 60,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    ),
    WorkoutSuggestion(
        id = -103,
        name = "Mobility Reset",
        category = "Mobility",
        primaryMuscles = listOf("Hips", "Back"),
        equipment = listOf("Mat"),
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80",
        description = "Slow flow opening the thoracic spine, hip flexors, and hamstrings to restore range of motion.",
        durationMinutes = 20,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "Mobility",
        effortScore = 35,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    ),
    WorkoutSuggestion(
        id = -104,
        name = "Endurance Ride",
        category = "Cardio",
        primaryMuscles = listOf("Legs", "Glutes"),
        equipment = listOf("Stationary Bike"),
        imageUrl = "https://images.unsplash.com/photo-1507831228885-004b65a06f4c?auto=format&fit=crop&w=1200&q=80",
        description = "Steady-state cycling blocks layered with cadence surges for aerobic conditioning.",
        durationMinutes = 30,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "Cardio",
        effortScore = 55,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    ),
    WorkoutSuggestion(
        id = -105,
        name = "Trail Run Prep",
        category = "Cardio",
        primaryMuscles = listOf("Legs", "Core"),
        equipment = listOf("Bodyweight"),
        imageUrl = "https://images.unsplash.com/photo-1546484959-f9a94a30713e?auto=format&fit=crop&w=1200&q=80",
        description = "Interval jogs paired with uphill bounding drills to build toughness for trail adventures.",
        durationMinutes = 22,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "Cardio",
        effortScore = 55,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    ),
    WorkoutSuggestion(
        id = -106,
        name = "Core Focus",
        category = "Strength",
        primaryMuscles = listOf("Core"),
        equipment = listOf("Mat"),
        imageUrl = "https://images.unsplash.com/photo-1554284126-aa88f22d8b74?auto=format&fit=crop&w=1200&q=80",
        description = "Rotational planks, hollow holds, and anti-rotation presses to stabilise your midline.",
        durationMinutes = 18,
        calories = 0,
        distanceKm = null,
        steps = 0,
        startTimeMillis = null,
        intensityLabel = "Strength focus",
        effortScore = 50,
        averageHeartRate = null,
        maxHeartRate = null,
        averagePaceMinutesPerKm = null
    )
)

private fun uniqueExerciseKey(info: ExerciseInfo, fallback: Int): Int =
    info.id ?: (info.name?.hashCode() ?: fallback)

private data class CatalogueCache(
    val exercises: List<ExerciseInfo>,
    val suggestions: List<WorkoutSuggestion>
)
