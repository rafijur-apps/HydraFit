package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WaterLogEntity::class, ExerciseLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HydraFitDatabase : RoomDatabase() {
    abstract fun hydraFitDao(): HydraFitDao

    companion object {
        @Volatile
        private var INSTANCE: HydraFitDatabase? = null

        fun getInstance(context: Context): HydraFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HydraFitDatabase::class.java,
                    "hydrafit_offline.db",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
