package com.coachapp.data

import androidx.room.withTransaction
import com.coachapp.core.DefaultProgram
import com.coachapp.core.Science
import com.coachapp.core.WorkoutDay
import com.coachapp.data.local.BodySnapshotEntity
import com.coachapp.data.local.CoachDatabase
import com.coachapp.data.local.DefaultProgramSeed
import com.coachapp.data.local.HealthSyncStateEntity
import com.coachapp.data.local.PerformedSetEntity
import com.coachapp.data.local.UserProfileEntity
import com.coachapp.data.local.WorkoutSessionEntity
import com.coachapp.health.BodyMetrics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val ComparisonSessionLimit = 5

class CoachRepository(
    private val database: CoachDatabase
) {
    private val dao = database.coachDao()

    suspend fun seedDefaultProgram() {
        database.withTransaction {
            if (dao.workoutPlans().isNotEmpty()) {
                return@withTransaction
            }

            dao.upsertExercises(DefaultProgramSeed.exercises())
            dao.upsertWorkoutPlans(DefaultProgramSeed.workoutPlans())
            dao.upsertWorkoutPlanExercises(DefaultProgramSeed.workoutPlanExercises())
        }
    }

    suspend fun latestSessionSummary(): SessionSummarySnapshot? {
        val session = dao.latestSession() ?: return null
        val completedSets = dao.performedSetCountForSession(session.id)
        val previousComparableSessions = dao.comparableSessionsBefore(
            planDayId = session.planDayId,
            startedAtEpochMillis = session.startedAtEpochMillis,
            limit = ComparisonSessionLimit
        )

        return session.toSummarySnapshot(
            completedSets = completedSets,
            previousComparables = previousComparableSessions
        )
    }

    suspend fun saveBodyMetricsSnapshot(metrics: BodyMetrics): BodyMetricsSnapshot {
        val dateIso = metrics.recordedAtEpochMillis
            ?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
            ?: LocalDate.now().toString()
        val bmi = metrics.weightKg?.let { weight ->
            metrics.heightCm?.let { height -> Science.bmi(weight, height) }
        }
        val bmr = metrics.weightKg?.let { weight ->
            metrics.bodyFatPercent?.takeIf { it in 3.0..70.0 }?.let { bodyFat ->
                Science.leanMassBmr(weight, bodyFat)
            }
        }
        val snapshot = BodySnapshotEntity(
            id = "${metrics.source}-$dateIso",
            dateIso = dateIso,
            weightKg = metrics.weightKg,
            heightCm = metrics.heightCm,
            bodyFatPercent = metrics.bodyFatPercent,
            bmi = bmi,
            bmr = bmr,
            source = metrics.source
        )

        dao.insertBodySnapshot(snapshot)
        dao.upsertHealthSyncState(
            HealthSyncStateEntity(
                source = metrics.source,
                lastSyncedAtEpochMillis = System.currentTimeMillis(),
                status = "synced",
                lastError = null
            )
        )

        return snapshot.toBodyMetricsSnapshot()
    }

    suspend fun userProfile(): UserProfileSnapshot? =
        dao.userProfile()?.toUserProfileSnapshot()

    suspend fun saveUserProfile(input: UserProfileInput): UserProfileSnapshot {
        val entity = UserProfileEntity(
            id = UserProfileEntityId,
            heightCm = input.heightCm?.takeIf { it > 0.0 },
            weightKg = input.weightKg?.takeIf { it > 0.0 },
            ageYears = input.ageYears?.takeIf { it in 13..100 },
            trainingDaysIsoCsv = sanitizeTrainingDays(input.trainingDaysIso).joinToString(","),
            targetSessionMinutes = sanitizeTargetSessionMinutes(input.targetSessionMinutes),
            updatedAtEpochMillis = System.currentTimeMillis()
        )
        dao.upsertUserProfile(entity)
        return entity.toUserProfileSnapshot()
    }

    suspend fun saveCompletedSession(
        workoutDay: WorkoutDay,
        startedAtEpochMillis: Long,
        endedAtEpochMillis: Long,
        completedSets: List<CompletedSetRecord>,
        estimatedCalories: Double
    ): SessionSummarySnapshot {
        require(endedAtEpochMillis >= startedAtEpochMillis) { "session end must be after start" }

        seedDefaultProgram()

        val previousComparableSessions = dao.comparableSessionsBefore(
            planDayId = workoutDay.id,
            startedAtEpochMillis = startedAtEpochMillis,
            limit = ComparisonSessionLimit
        )
        val sessionId = "${workoutDay.id}-$startedAtEpochMillis"
        val performedSets = completedSets.mapIndexed { index, completedSet ->
            PerformedSetEntity(
                id = "$sessionId-${index + 1}",
                sessionId = sessionId,
                exerciseId = completedSet.exerciseId,
                exerciseName = completedSet.exerciseName,
                setIndex = completedSet.setIndex,
                reps = completedSet.performedReps ?: completedSet.plannedRepsMax,
                plannedRepsMin = completedSet.plannedRepsMin,
                plannedRepsMax = completedSet.plannedRepsMax,
                performedReps = completedSet.performedReps,
                weightKg = completedSet.weightKg,
                restSeconds = completedSet.restSeconds,
                completedAtEpochMillis = completedSet.completedAtEpochMillis.coerceIn(
                    startedAtEpochMillis,
                    endedAtEpochMillis
                ),
                rir = completedSet.rir,
                source = completedSet.source
            )
        }
        val totalVolumeKg = performedSets.sumOf { set ->
            Science.totalVolumeKg(reps = set.reps, weightKg = set.weightKg)
        }
        val session = WorkoutSessionEntity(
            id = sessionId,
            planDayId = workoutDay.id,
            startedAtEpochMillis = startedAtEpochMillis,
            endedAtEpochMillis = endedAtEpochMillis,
            estimatedCalories = estimatedCalories,
            totalVolumeKg = totalVolumeKg
        )

        database.withTransaction {
            dao.insertWorkoutSession(session)
            if (performedSets.isNotEmpty()) {
                dao.insertPerformedSets(performedSets)
            }
        }

        return session.toSummarySnapshot(
            completedSets = performedSets.size,
            previousComparables = previousComparableSessions
        )
    }

    suspend fun dailyCoachActivity(limit: Int = 14): List<DailyCoachActivitySnapshot> {
        require(limit > 0) { "limit must be positive" }
        return dao.dailyCoachActivity(limit).map { row ->
            DailyCoachActivitySnapshot(
                dateIso = row.dateIso,
                sessionCount = row.sessionCount,
                completedSets = row.completedSets,
                workoutCalories = row.workoutCalories,
                durationMinutes = row.durationMinutes,
                totalVolumeKg = row.totalVolumeKg
            )
        }
    }

private fun BodySnapshotEntity.toBodyMetricsSnapshot(): BodyMetricsSnapshot {
    return BodyMetricsSnapshot(
        id = id,
            dateIso = dateIso,
            weightKg = weightKg,
            heightCm = heightCm,
            bodyFatPercent = bodyFatPercent,
            bmi = bmi,
            bmr = bmr,
        source = source
    )
}

private fun UserProfileEntity.toUserProfileSnapshot(): UserProfileSnapshot =
    UserProfileSnapshot(
        heightCm = heightCm,
        weightKg = weightKg,
        ageYears = ageYears,
        trainingDaysIso = trainingDaysIsoCsv
            .split(",")
            .mapNotNull { it.toIntOrNull() }
            .toSet()
            .let(::sanitizeTrainingDays),
        targetSessionMinutes = sanitizeTargetSessionMinutes(targetSessionMinutes),
        updatedAtEpochMillis = updatedAtEpochMillis
    )

private val UserProfileEntityId = "profile"

private fun WorkoutSessionEntity.toSummarySnapshot(
        completedSets: Int,
        previousComparables: List<WorkoutSessionEntity>
    ): SessionSummarySnapshot {
        val averageComparableVolumeKg = previousComparables.takeIf { it.isNotEmpty() }
            ?.map { it.totalVolumeKg }
            ?.average()
        val averageComparableCalories = previousComparables.takeIf { it.isNotEmpty() }
            ?.mapNotNull { it.estimatedCalories }
            ?.takeIf { it.isNotEmpty() }
            ?.average()
        val volumeDeltaKg = averageComparableVolumeKg?.let { totalVolumeKg - it }
        val recordType = when {
            averageComparableVolumeKg == null -> "baseline"
            volumeDeltaKg != null && volumeDeltaKg > 0.0 -> "volume_record"
            volumeDeltaKg == 0.0 -> "stable"
            else -> "below_previous"
        }

        return SessionSummarySnapshot(
            sessionId = id,
            planDayId = planDayId,
            planTitle = DefaultProgram.weeklyPlan.firstOrNull { it.id == planDayId }?.title ?: planDayId,
            endedAtEpochMillis = endedAtEpochMillis ?: startedAtEpochMillis,
            completedSets = completedSets,
            totalVolumeKg = totalVolumeKg,
            estimatedCalories = estimatedCalories,
            previousComparableVolumeKg = averageComparableVolumeKg,
            previousComparableCalories = averageComparableCalories,
            averageComparableVolumeKg = averageComparableVolumeKg,
            averageComparableCalories = averageComparableCalories,
            averageComparableSessionCount = previousComparables.size,
            volumeDeltaKg = volumeDeltaKg,
            recordType = recordType,
            nextHint = nextHint(recordType, volumeDeltaKg)
        )
    }

    private fun nextHint(recordType: String, volumeDeltaKg: Double?): String {
        return when (recordType) {
            "baseline" -> "Premiere reference pour cette seance. Garde la meme structure au prochain passage."
            "volume_record" -> "Record de volume. Si la technique reste propre, garde la charge et vise encore une rep."
            "stable" -> "Volume stable. Prochaine piste : viser +1 rep sur la derniere serie."
            else -> {
                val roundedDelta = volumeDeltaKg?.toInt() ?: 0
                "Volume en retrait (${roundedDelta} kg). Reprends la meme charge avant d'augmenter."
            }
        }
    }

}
