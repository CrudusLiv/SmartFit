package com.example.smartfit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE type = :type ORDER BY date DESC")
    fun getActivitiesByType(type: String): Flow<List<ActivityEntity>>

    @Query("SELECT SUM(value) FROM activities WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
}

