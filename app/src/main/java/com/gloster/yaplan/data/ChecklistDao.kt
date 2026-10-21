package com.gloster.yaplan.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE taskId = :taskId ORDER BY position, id")
    fun observe(taskId: Long): Flow<List<ChecklistItem>>

    @Query("SELECT * FROM checklist_items WHERE taskId = :taskId ORDER BY position, id")
    suspend fun forTask(taskId: Long): List<ChecklistItem>

    @Query("SELECT taskId, SUM(isDone) AS done, COUNT(*) AS total FROM checklist_items GROUP BY taskId")
    fun observeProgress(): Flow<List<ChecklistProgress>>

    @Upsert
    suspend fun upsert(items: List<ChecklistItem>)

    @Query("DELETE FROM checklist_items WHERE taskId = :taskId AND id NOT IN (:keep)")
    suspend fun deleteAllExcept(taskId: Long, keep: List<Long>)

    @Query("UPDATE checklist_items SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: Long, done: Boolean)
}
