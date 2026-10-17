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
    suspend fun get(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE remind = 1 AND isDone = 0 AND dueAt > :now")
    suspend fun pendingReminders(now: Long): List<Task>

    @Upsert
    suspend fun upsert(task: Task): Long

    @Delete
    suspend fun delete(task: Task)
}
