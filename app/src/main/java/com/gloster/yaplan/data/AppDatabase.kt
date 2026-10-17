package com.gloster.yaplan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class, Category::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private val defaultCategories = listOf("İş", "Ev", "Okul", "Kişisel")

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "yaplan.db")
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        defaultCategories.forEachIndexed { i, name ->
                            db.execSQL(
                                "INSERT INTO categories (name, color) VALUES (?, ?)",
                                arrayOf<Any>(name, CategoryColors[i]),
                            )
                        }
                    }
                })
                .build()
    }
}
