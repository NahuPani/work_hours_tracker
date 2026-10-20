package com.example.workhourstracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WorkShift::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workShiftDao(): WorkShiftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE work_shifts ADD COLUMN standardDailyHours REAL NOT NULL DEFAULT 8.0")
                db.execSQL("ALTER TABLE work_shifts ADD COLUMN overtimeMultiplier REAL NOT NULL DEFAULT 1.5")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Intentional no-op: v3 has identical schema to v2.
                // Version was bumped for app-level logic changes only (daily overtime, backup format).
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_work_shifts_startTimeMillis ON work_shifts(startTimeMillis)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "work_hours_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
