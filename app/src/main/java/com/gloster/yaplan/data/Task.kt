package com.gloster.yaplan.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Priority(val label: String) {
    HIGH("Yüksek"),
    MEDIUM("Orta"),
    LOW("Düşük"),
}

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("categoryId")],
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val isDone: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val categoryId: Long? = null,
    /** Son tarih, epoch milisaniye. */
    val dueAt: Long? = null,
    val remind: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
