package com.gloster.yaplan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class, Category::class, Habit::class, HabitCheck::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun habitDao(): HabitDao

    companion object {
        private val defaultCategories = listOf("İş", "Ev", "Okul", "Kişisel")

        /** Copied from schemas/…/2.json so Room's schema check accepts the migrated tables. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `reminderMinutes` INTEGER, `createdAt` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `habit_checks` (`habitId` INTEGER NOT NULL, `day` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`habitId`, `day`), FOREIGN KEY(`habitId`) REFERENCES `habits`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "yaplan.db")
                .addMigrations(MIGRATION_1_2)
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
