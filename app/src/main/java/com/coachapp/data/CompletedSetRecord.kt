package com.coachapp.data

data class CompletedSetRecord(
    val exerciseId: String,
    val exerciseName: String,
    val setIndex: Int,
    val plannedRepsMin: Int,
    val plannedRepsMax: Int,
    val performedReps: Int?,
    val weightKg: Double,
    val restSeconds: Int,
    val completedAtEpochMillis: Long,
    val rir: Int?,
    val source: String
)
