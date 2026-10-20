package com.example.workhourstracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkShiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: WorkShift): Long

    @Update
    suspend fun updateShift(shift: WorkShift)

    @Delete
    suspend fun deleteShift(shift: WorkShift)

    @Query("SELECT * FROM work_shifts ORDER BY startTimeMillis DESC")
    fun getAllShifts(): Flow<List<WorkShift>>

    @Query("SELECT * FROM work_shifts ORDER BY startTimeMillis DESC")
    suspend fun getAllShiftsList(): List<WorkShift>

    @Query("SELECT * FROM work_shifts WHERE startTimeMillis >= :startMillis AND startTimeMillis < :endMillis ORDER BY startTimeMillis DESC")
    fun getShiftsBetween(startMillis: Long, endMillis: Long): Flow<List<WorkShift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shifts: List<WorkShift>)

    @Query("DELETE FROM work_shifts")
    suspend fun deleteAllShifts()
}
