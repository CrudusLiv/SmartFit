package com.example.smartfit.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ActivityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmartFitDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        private const val TAG = "SmartFitDatabase"

        @Volatile
        private var INSTANCE: SmartFitDatabase? = null

        fun getDatabase(context: Context): SmartFitDatabase {
            Log.d(TAG, "Getting database instance")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartFitDatabase::class.java,
                    "smartfit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                Log.d(TAG, "Database instance created")
                INSTANCE = instance
                instance
            }
        }
    }
}

