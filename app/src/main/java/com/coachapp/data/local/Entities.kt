package com.coachapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscles: List<String>,
    val equipment: String,
    val illustrationAsset: String,
    val videoAsset: String?
)

@Entity(tableName = "workout_plans")
data class WorkoutPlanEntity(
    @PrimaryKey val id: String,
    val dayOfWeekIso: Int,
    val title: String,
    val targetMinutes: Int
)

@Entity(
    tableName = "workout_plan_exercises",
    primaryKeys = ["planDayId", "position"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planDayId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class WorkoutPlanExerciseEntity(
    val planDayId: String,
    val position: Int,
    val exerciseId: String,
    val setCount: Int,
    val repsMin: Int,
    val repsMax: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val rirTarget: Int,
    val notes: String
)

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planDayId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("planDayId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val planDayId: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val estimatedCalories: Double?,
    val totalVolumeKg: Double
)

@Entity(
    tableName = "performed_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("sessionId"), Index("exerciseId")]
)
data class PerformedSetEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val exerciseId: String,
    @ColumnInfo(defaultValue = "''")
    val exerciseName: String,
    val setIndex: Int,
    val reps: Int,
    @ColumnInfo(defaultValue = "0")
    val plannedRepsMin: Int,
    @ColumnInfo(defaultValue = "0")
    val plannedRepsMax: Int,
    val performedReps: Int?,
    val weightKg: Double,
    @ColumnInfo(defaultValue = "0")
    val restSeconds: Int,
    val completedAtEpochMillis: Long,
    val rir: Int?,
    @ColumnInfo(defaultValue = "'unknown'")
    val source: String
)

@Entity(tableName = "body_snapshots")
data class BodySnapshotEntity(
    @PrimaryKey val id: String,
    val dateIso: String,
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPercent: Double?,
    val bmi: Double?,
    val bmr: Double?,
    val source: String
)

@Entity(tableName = "health_sync_states")
data class HealthSyncStateEntity(
    @PrimaryKey val source: String,
    val lastSyncedAtEpochMillis: Long?,
    val status: String,
    val lastError: String?
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val heightCm: Double?,
    val weightKg: Double?,
    val ageYears: Int?,
    val trainingDaysIsoCsv: String,
    val updatedAtEpochMillis: Long
)
