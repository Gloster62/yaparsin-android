package com.gloster.yaplan.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Category>>

    @Upsert
    suspend fun upsert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Delete
    suspend fun delete(category: Category)
}
