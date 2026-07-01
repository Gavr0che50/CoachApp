package com.coachapp.data

data class SessionSummarySnapshot(
    val sessionId: String,
    val planDayId: String,
    val planTitle: String,
    val endedAtEpochMillis: Long,
    val completedSets: Int,
    val totalVolumeKg: Double,
    val estimatedCalories: Double?,
    val previousComparableVolumeKg: Double?,
    val previousComparableCalories: Double?,
    val averageComparableVolumeKg: Double? = previousComparableVolumeKg,
    val averageComparableCalories: Double? = previousComparableCalories,
    val averageComparableSessionCount: Int = if (previousComparableVolumeKg == null) 0 else 1,
    val volumeDeltaKg: Double?,
    val recordType: String,
    val nextHint: String
)
