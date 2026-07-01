package com.coachapp.data.local

data class DailyCoachActivityRow(
    val dateIso: String,
    val sessionCount: Int,
    val completedSets: Int,
    val workoutCalories: Double,
    val durationMinutes: Double,
    val totalVolumeKg: Double
)
