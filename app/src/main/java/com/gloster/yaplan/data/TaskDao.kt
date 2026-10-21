package com.gloster.yaplan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun observeAll(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observe(id: Long): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun get(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND (dueAt > :now OR repeatDays != 0)")
    suspend fun withReminders(now: Long): List<Task>

    @Upsert
    suspend fun upsert(task: Task): Long

    @Delete
    suspend fun delete(task: Task)
}
