package com.gloster.yaplan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt")
    fun observeAll(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observe(id: Long): Flow<Habit?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun get(id: Long): Habit?

    @Query("SELECT * FROM habits WHERE reminderMinutes IS NOT NULL")
    suspend fun withReminder(): List<Habit>

    @Query("SELECT * FROM habit_checks")
    fun observeAllChecks(): Flow<List<HabitCheck>>

    @Query("SELECT day FROM habit_checks WHERE habitId = :habitId")
    fun observeDays(habitId: Long): Flow<List<Long>>

    @Query("SELECT day FROM habit_checks WHERE habitId = :habitId")
    suspend fun days(habitId: Long): List<Long>

    @Upsert
    suspend fun upsert(habit: Habit): Long

    @Delete
    suspend fun delete(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheck(check: HabitCheck)

    @Delete
    suspend fun deleteCheck(check: HabitCheck)
}
