package com.gloster.yaplan.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Günün dakikası (20:30 → 1230); null ise hatırlatma yok. */
    val reminderMinutes: Int? = null,
    /** Hatırlatma bildirim yerine tam ekran alarm olarak çalar. */
    @ColumnInfo(defaultValue = "0") val reminderAlarm: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

/** One "yaptım" mark: [day] is a LocalDate epoch day. */
@Entity(
    tableName = "habit_checks",
    primaryKeys = ["habitId", "day"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HabitCheck(val habitId: Long, val day: Long)
