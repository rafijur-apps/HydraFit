package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HydraFitDao {
    @Query("SELECT * FROM water_logs WHERE dateKey = :dateKey ORDER BY timestampMillis DESC")
    fun observeWaterLogsForDate(dateKey: String): Flow<List<WaterLogEntity>>

    @Query("SELECT * FROM water_logs WHERE dateKey >= :startDateKey ORDER BY timestampMillis DESC")
    fun observeWaterLogsSince(startDateKey: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("SELECT * FROM exercise_logs WHERE dateKey = :dateKey ORDER BY timestampMillis DESC")
    fun observeExerciseLogsForDate(dateKey: String): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs WHERE dateKey >= :startDateKey ORDER BY timestampMillis DESC")
    fun observeExerciseLogsSince(startDateKey: String): Flow<List<ExerciseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLogEntity)

    @Query("DELETE FROM exercise_logs WHERE id = :id")
    suspend fun deleteExerciseLogById(id: Long)
}
