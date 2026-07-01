package com.coachapp.data

data class DailyCoachActivitySnapshot(
    val dateIso: String,
    val sessionCount: Int,
    val completedSets: Int,
    val workoutCalories: Double,
    val durationMinutes: Double,
    val totalVolumeKg: Double
)
