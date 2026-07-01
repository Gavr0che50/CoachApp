package com.coachapp.data

data class BodyMetricsSnapshot(
    val id: String,
    val dateIso: String,
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPercent: Double?,
    val bmi: Double?,
    val bmr: Double?,
    val source: String
)

