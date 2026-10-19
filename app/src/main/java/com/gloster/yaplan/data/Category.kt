package com.gloster.yaplan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
)

val CategoryColors = listOf(
    0xFF4FC3F7, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8,
    0xFFE57373, 0xFF4DB6AC, 0xFFF06292, 0xFFA1887F,
)

/** The first palette color no category uses yet, so a new category is told apart at a glance. */
fun nextCategoryColor(existing: List<Category>): Long =
    CategoryColors.firstOrNull { c -> existing.none { it.color == c } }
        ?: CategoryColors[existing.size % CategoryColors.size]
