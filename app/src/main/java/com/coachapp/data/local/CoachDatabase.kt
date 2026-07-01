package com.coachapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        WorkoutPlanExerciseEntity::class,
        WorkoutSessionEntity::class,
        PerformedSetEntity::class,
        BodySnapshotEntity::class,
        HealthSyncStateEntity::class,
        UserProfileEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(CoachTypeConverters::class)
abstract class CoachDatabase : RoomDatabase() {
    abstract fun coachDao(): CoachDao

    companion object {
        const val DATABASE_NAME = "coachapp.db"

        fun create(context: Context): CoachDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CoachDatabase::class.java,
                DATABASE_NAME
        )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN exerciseName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN plannedRepsMin INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN plannedRepsMax INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN performedReps INTEGER")
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN restSeconds INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE performed_sets ADD COLUMN source TEXT NOT NULL DEFAULT 'unknown'")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_profile (
                    id TEXT NOT NULL,
                    heightCm REAL,
                    weightKg REAL,
                    ageYears INTEGER,
                    trainingDaysIsoCsv TEXT NOT NULL,
                    updatedAtEpochMillis INTEGER NOT NULL,
                    PRIMARY KEY(id)
                )
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_profile ADD COLUMN targetSessionMinutes INTEGER NOT NULL DEFAULT 30")
        }
    }
}
}
