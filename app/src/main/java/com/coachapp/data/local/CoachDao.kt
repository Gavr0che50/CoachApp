package com.coachapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CoachDao {
    @Upsert
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    @Upsert
    suspend fun upsertWorkoutPlans(plans: List<WorkoutPlanEntity>)

    @Upsert
    suspend fun upsertWorkoutPlanExercises(planExercises: List<WorkoutPlanExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformedSets(sets: List<PerformedSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodySnapshot(snapshot: BodySnapshotEntity)

    @Query("SELECT * FROM body_snapshots ORDER BY dateIso DESC LIMIT 1")
    suspend fun latestBodySnapshot(): BodySnapshotEntity?

    @Upsert
    suspend fun upsertHealthSyncState(state: HealthSyncStateEntity)

    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE id = 'profile' LIMIT 1")
    suspend fun userProfile(): UserProfileEntity?

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun exercises(): List<ExerciseEntity>

    @Query("SELECT * FROM workout_plans ORDER BY dayOfWeekIso ASC")
    suspend fun workoutPlans(): List<WorkoutPlanEntity>

    @Query(
        """
        SELECT * FROM workout_plan_exercises
        WHERE planDayId = :planDayId
        ORDER BY position ASC
        """
    )
    suspend fun workoutPlanExercises(planDayId: String): List<WorkoutPlanExerciseEntity>

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE planDayId = :planDayId
        ORDER BY startedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun latestSessionForPlan(planDayId: String): WorkoutSessionEntity?

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE planDayId = :planDayId
        AND startedAtEpochMillis < :startedAtEpochMillis
        ORDER BY startedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun latestComparableSessionBefore(
        planDayId: String,
        startedAtEpochMillis: Long
    ): WorkoutSessionEntity?

    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE planDayId = :planDayId
        AND startedAtEpochMillis < :startedAtEpochMillis
        ORDER BY startedAtEpochMillis DESC
        LIMIT :limit
        """
    )
    suspend fun comparableSessionsBefore(
        planDayId: String,
        startedAtEpochMillis: Long,
        limit: Int
    ): List<WorkoutSessionEntity>

    @Query(
        """
        SELECT * FROM workout_sessions
        ORDER BY endedAtEpochMillis DESC, startedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun latestSession(): WorkoutSessionEntity?

    @Query(
        """
        SELECT * FROM performed_sets
        WHERE sessionId = :sessionId
        ORDER BY setIndex ASC
        """
    )
    suspend fun performedSetsForSession(sessionId: String): List<PerformedSetEntity>

    @Query("SELECT COUNT(*) FROM performed_sets WHERE sessionId = :sessionId")
    suspend fun performedSetCountForSession(sessionId: String): Int

    @Query(
        """
        SELECT
            date(endedAtEpochMillis / 1000, 'unixepoch', 'localtime') AS dateIso,
            COUNT(*) AS sessionCount,
            COALESCE(SUM((SELECT COUNT(*) FROM performed_sets WHERE sessionId = workout_sessions.id)), 0) AS completedSets,
            COALESCE(SUM(estimatedCalories), 0.0) AS workoutCalories,
            COALESCE(SUM((endedAtEpochMillis - startedAtEpochMillis) / 60000.0), 0.0) AS durationMinutes,
            COALESCE(SUM(totalVolumeKg), 0.0) AS totalVolumeKg
        FROM workout_sessions
        WHERE endedAtEpochMillis IS NOT NULL
        GROUP BY dateIso
        ORDER BY dateIso DESC
        LIMIT :limit
        """
    )
    suspend fun dailyCoachActivity(limit: Int): List<DailyCoachActivityRow>
}
