package com.example.smartfit.google

import android.content.Context
import android.text.format.DateUtils
import com.example.smartfit.data.remote.ExerciseCategory
import com.example.smartfit.data.remote.ExerciseInfo
import com.example.smartfit.data.remote.ExerciseInfoResponse
import com.example.smartfit.data.remote.Equipment
import com.example.smartfit.data.remote.Muscle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataPoint
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.data.Session
import com.google.android.gms.fitness.result.SessionReadResponse
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GoogleFitDataSource(private val context: Context) {

    suspend fun fetchExercises(limit: Int = 20, offset: Int = 0): ExerciseInfoResponse = withContext(Dispatchers.IO) {
        val workouts = loadWorkouts()
        if (workouts.isEmpty()) {
            return@withContext fallbackCatalog(limit, offset)
        }

        val sliced = workouts.drop(offset).take(limit)
        ExerciseInfoResponse(
            count = workouts.size,
            next = null,
            previous = null,
            results = sliced.map { it.toExerciseInfo() }
        )
    }

    suspend fun fetchWorkouts(limit: Int = 20, offset: Int = 0): List<FitWorkout> = withContext(Dispatchers.IO) {
        val workouts = loadWorkouts()
        workouts.drop(offset).take(limit)
    }

    private suspend fun loadWorkouts(): List<FitWorkout> {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return fallbackWorkouts()
        val response = readRecentSessions(account)
        if (response.sessions.isEmpty()) return fallbackWorkouts()

        val workouts = response.sessions.map { session ->
            val activityName = humanReadableActivity(session.activity)
            val metrics = aggregateMetrics(session, response)
            val focusAreas = guessMuscles(activityName)
                .mapNotNull { it.name ?: it.name_en }
                .filter { it.isNotBlank() }
                .ifEmpty { listOf(activityName) }
            val equipment = guessEquipment(activityName)
                .mapNotNull { it.name }
                .ifEmpty { listOf("Bodyweight") }

            FitWorkout(
                id = session.identifier?.hashCode() ?: session.hashCode(),
                title = session.name?.takeIf { it.isNotBlank() } ?: activityName,
                activity = activityName,
                startTimeMillis = session.getStartTime(TimeUnit.MILLISECONDS),
                endTimeMillis = session.getEndTime(TimeUnit.MILLISECONDS),
                durationMinutes = (metrics.durationMillis / TimeUnit.MINUTES.toMillis(1)).toInt().coerceAtLeast(1),
                calories = metrics.calories.roundToInt().coerceAtLeast(0),
                steps = metrics.steps,
                distanceKm = metrics.distanceMeters.takeIf { it > 0.0 }?.div(1000.0),
                notes = session.description?.takeIf { it.isNotBlank() },
                intensityLabel = classifyIntensity(metrics),
                effortScore = computeEffortScore(metrics),
                averagePaceMinutesPerKm = computeAveragePaceMinutesPerKm(metrics),
                averageHeartRate = metrics.avgHeartRate?.roundToInt(),
                maxHeartRate = metrics.maxHeartRate?.roundToInt(),
                focusAreas = focusAreas,
                equipment = equipment
            )
        }
            .sortedByDescending { it.startTimeMillis }

        return if (workouts.isEmpty()) fallbackWorkouts() else workouts
    }

    private suspend fun readRecentSessions(account: GoogleSignInAccount): SessionReadResponse {
        val endMillis = System.currentTimeMillis()
        val startMillis = endMillis - TimeUnit.DAYS.toMillis(30)

        val request = com.google.android.gms.fitness.request.SessionReadRequest.Builder()
            .setTimeInterval(startMillis, endMillis, TimeUnit.MILLISECONDS)
            .readSessionsFromAllApps()
            .enableServerQueries()
            .read(DataType.TYPE_ACTIVITY_SEGMENT)
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_DISTANCE_DELTA)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .read(DataType.TYPE_HEART_RATE_BPM)
            .read(DataType.TYPE_MOVE_MINUTES)
            .read(DataType.TYPE_SPEED)
            .build()

        return Fitness.getSessionsClient(context, account).readSession(request).await()
    }

    private fun aggregateMetrics(session: Session, response: SessionReadResponse): SessionMetrics {
        val durationMillis = session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS)
        val dataSets = response.getDataSet(session)

        var calories = 0.0
        var steps = 0
        var distance = 0.0
        var heartRateSamples = 0
        var heartRateTotal = 0.0
        var maxHeartRate = 0.0
        var speedSamples = 0
        var speedTotal = 0.0
        var moveMinutes = 0

        dataSets.forEach { dataSet ->
            dataSet.dataPoints.forEach { dataPoint ->
                when (dataSet.dataType) {
                    DataType.TYPE_CALORIES_EXPENDED -> dataPoint.floatValue(Field.FIELD_CALORIES)?.let { calories += it }
                    DataType.TYPE_STEP_COUNT_DELTA -> dataPoint.intValue(Field.FIELD_STEPS)?.let { steps += it }
                    DataType.TYPE_DISTANCE_DELTA -> dataPoint.floatValue(Field.FIELD_DISTANCE)?.let { distance += it }
                    DataType.TYPE_HEART_RATE_BPM -> dataPoint.floatValue(Field.FIELD_BPM)?.let { bpm ->
                        heartRateTotal += bpm
                        heartRateSamples++
                        if (bpm > maxHeartRate) maxHeartRate = bpm
                    }
                    DataType.TYPE_SPEED -> dataPoint.floatValue(Field.FIELD_SPEED)?.takeIf { it > 0.0 }?.let { speed ->
                        speedTotal += speed
                        speedSamples++
                    }
                    DataType.TYPE_MOVE_MINUTES -> dataPoint.intValue(Field.FIELD_DURATION)?.let { moveMinutes += it }
                }
            }
        }

        val avgHeart = if (heartRateSamples > 0) heartRateTotal / heartRateSamples else null
        val avgSpeed = if (speedSamples > 0) speedTotal / speedSamples else null

        return SessionMetrics(
            durationMillis = durationMillis,
            calories = calories,
            steps = steps,
            distanceMeters = distance,
            avgHeartRate = avgHeart,
            maxHeartRate = if (maxHeartRate > 0) maxHeartRate else null,
            avgSpeedMps = avgSpeed,
            moveMinutes = moveMinutes
        )
    }

    private fun classifyIntensity(metrics: SessionMetrics): String {
        val minutes = metrics.durationMillis / 60000.0
        val caloriesPerMinute = if (minutes > 0) metrics.calories / minutes else 0.0
        return when {
            metrics.avgHeartRate != null && metrics.avgHeartRate > 150 -> "High intensity"
            caloriesPerMinute >= 8.0 -> "High intensity"
            caloriesPerMinute >= 5.0 -> "Moderate effort"
            metrics.steps >= 4000 -> "Moderate effort"
            else -> "Light session"
        }
    }

    private fun computeEffortScore(metrics: SessionMetrics): Int {
        val minutes = (metrics.durationMillis / TimeUnit.MINUTES.toMillis(1)).coerceAtLeast(1L).toDouble()
        val calorieComponent = metrics.calories / minutes
        val stepComponent = metrics.steps / (minutes * 12.0)
        val distanceComponent = metrics.distanceMeters / (minutes * 40.0)
        val heartComponent = metrics.avgHeartRate?.div(2.0) ?: 0.0
        val rawScore = (calorieComponent * 10) + stepComponent + distanceComponent + heartComponent
        return rawScore.roundToInt().coerceIn(10, 100)
    }

    private fun computeAveragePaceMinutesPerKm(metrics: SessionMetrics): Double? {
        if (metrics.distanceMeters <= 0.0) return null
        val km = metrics.distanceMeters / 1000.0
        val minutes = metrics.durationMillis / 60000.0
        if (km <= 0.0 || minutes <= 0.0) return null
        return minutes / km
    }

    private fun FitWorkout.toExerciseInfo(): ExerciseInfo = ExerciseInfo(
        id = id,
        name = title,
        description = buildExerciseDescription(this),
        category = ExerciseCategory(id = activity.hashCode(), name = activity),
        muscles = focusAreas.mapIndexed { index, label ->
            Muscle(id = (label + id + index).hashCode(), name = label, name_en = label)
        },
        muscles_secondary = emptyList(),
        equipment = equipment.mapIndexed { index, label ->
            Equipment(id = (label + id + index).hashCode(), name = label)
        },
        images = emptyList()
    )

    private fun buildExerciseDescription(workout: FitWorkout): String {
        val metrics = mutableListOf<String>()
        metrics += "${workout.durationMinutes} min"
        workout.distanceKm?.let { metrics += String.format(Locale.getDefault(), "%.2f km", it) }
        if (workout.steps > 0) metrics += "${workout.steps} steps"
        if (workout.calories > 0) metrics += "${workout.calories} kcal"
        workout.averageHeartRate?.let { metrics += "$it bpm avg" }
        workout.averagePaceMinutesPerKm?.let { metrics += String.format(Locale.getDefault(), "%.1f min/km", it) }

        val builder = StringBuilder()
        builder.append(workout.intensityLabel)
        if (metrics.isNotEmpty()) {
            builder.append(" • ").append(metrics.joinToString(" • "))
        }

        val relative = DateUtils.getRelativeTimeSpanString(
            workout.startTimeMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        builder.append('\n').append("Logged ").append(relative)

        workout.notes?.takeIf { it.isNotBlank() }?.let { note ->
            builder.append('\n').append(note.trim())
        }

        return builder.toString()
    }

    private fun humanReadableActivity(activity: String?): String {
        if (activity.isNullOrBlank()) return "Workout"
        val words = activity.replace('_', ' ').lowercase(Locale.getDefault()).split(' ')
        return words.joinToString(" ") { part ->
            part.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }

    private fun guessMuscles(activityName: String): List<Muscle> {
        val normalized = activityName.lowercase(Locale.getDefault())
        val guesses = when {
            normalized.contains("run") -> listOf("Legs", "Core")
            normalized.contains("walk") -> listOf("Legs")
            normalized.contains("cycle") || normalized.contains("bike") -> listOf("Quadriceps", "Glutes")
            normalized.contains("swim") -> listOf("Back", "Shoulders")
            normalized.contains("yoga") -> listOf("Core", "Balance")
            normalized.contains("strength") || normalized.contains("gym") -> listOf("Full Body")
            else -> listOf("General Fitness")
        }
        return guesses.mapIndexed { index, label ->
            Muscle(id = (label + activityName + index).hashCode(), name = label, name_en = label)
        }
    }

    private fun guessEquipment(activityName: String): List<Equipment> {
        val normalized = activityName.lowercase(Locale.getDefault())
        val guesses = when {
            normalized.contains("cycle") || normalized.contains("bike") -> listOf("Bike")
            normalized.contains("swim") -> listOf("Pool")
            normalized.contains("yoga") -> listOf("Mat")
            normalized.contains("strength") || normalized.contains("gym") -> listOf("Gym Equipment")
            else -> listOf("Bodyweight")
        }
        return guesses.mapIndexed { index, label ->
            Equipment(id = (label + activityName + index).hashCode(), name = label)
        }
    }

    private fun fallbackCatalog(limit: Int, offset: Int): ExerciseInfoResponse {
        val workouts = fallbackWorkouts()
        val sliced = workouts.drop(offset).take(limit)
        return ExerciseInfoResponse(
            count = workouts.size,
            next = null,
            previous = null,
            results = sliced.map { it.toExerciseInfo() }
        )
    }

    private fun fallbackWorkouts(): List<FitWorkout> {
        val now = System.currentTimeMillis()
        return FALLBACK.mapIndexed { index, data ->
            val durationMinutes = 18 + index * 6
            val start = now - TimeUnit.DAYS.toMillis((index + 1).toLong())
            val end = start + TimeUnit.MINUTES.toMillis(durationMinutes.toLong())
            FitWorkout(
                id = (data.name + index).hashCode(),
                title = data.name,
                activity = data.category,
                startTimeMillis = start,
                endTimeMillis = end,
                durationMinutes = durationMinutes,
                calories = 160 + index * 45,
                steps = 2200 + index * 700,
                distanceKm = if (data.category.lowercase(Locale.getDefault()) == "cardio") 3.5 + index else null,
                notes = data.description,
                intensityLabel = if (index % 2 == 0) "Moderate effort" else "High intensity",
                effortScore = 45 + index * 10,
                averagePaceMinutesPerKm = if (data.category.lowercase(Locale.getDefault()) == "cardio") 6.0 - index * 0.3 else null,
                averageHeartRate = 125 + index * 5,
                maxHeartRate = 150 + index * 6,
                focusAreas = data.muscles,
                equipment = data.equipment
            )
        }
    }

    private fun DataPoint.floatValue(field: Field): Double? = runCatching { getValue(field).asFloat().toDouble() }.getOrNull()

    private fun DataPoint.intValue(field: Field): Int? = runCatching { getValue(field).asInt() }.getOrNull()

    data class FitWorkout(
        val id: Int,
        val title: String,
        val activity: String,
        val startTimeMillis: Long,
        val endTimeMillis: Long,
        val durationMinutes: Int,
        val calories: Int,
        val steps: Int,
        val distanceKm: Double?,
        val notes: String?,
        val intensityLabel: String,
        val effortScore: Int,
        val averagePaceMinutesPerKm: Double?,
        val averageHeartRate: Int?,
        val maxHeartRate: Int?,
        val focusAreas: List<String>,
        val equipment: List<String>
    )

    private data class SessionMetrics(
        val durationMillis: Long,
        val calories: Double,
        val steps: Int,
        val distanceMeters: Double,
        val avgHeartRate: Double?,
        val maxHeartRate: Double?,
        val avgSpeedMps: Double?,
        val moveMinutes: Int
    )

    private data class FallbackExercise(
        val name: String,
        val description: String,
        val category: String,
        val muscles: List<String>,
        val equipment: List<String>
    )

    companion object {
        private val FALLBACK = listOf(
            FallbackExercise(
                name = "Push-up",
                description = "Classic bodyweight movement to build upper-body strength.",
                category = "Strength",
                muscles = listOf("Chest", "Triceps"),
                equipment = listOf("Bodyweight")
            ),
            FallbackExercise(
                name = "Squat",
                description = "Lower-body compound exercise targeting quads and glutes.",
                category = "Strength",
                muscles = listOf("Quadriceps", "Glutes"),
                equipment = listOf("Bodyweight")
            ),
            FallbackExercise(
                name = "Plank",
                description = "Static core hold building stability and posture.",
                category = "Mobility",
                muscles = listOf("Core"),
                equipment = listOf("Bodyweight")
            ),
            FallbackExercise(
                name = "Running",
                description = "Cardio session inspired by Google Fit pacing.",
                category = "Cardio",
                muscles = listOf("Legs", "Core"),
                equipment = listOf("Bodyweight")
            )
        )
    }
}
