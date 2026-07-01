package com.coachapp.health

import java.time.LocalDate

interface HealthDataGateway {
    suspend fun latestBodyMetrics(): BodyMetrics?
    suspend fun restingHeartRateBpm(): Long?
    suspend fun activeEnergyKcalForCurrentDay(): Double?
    suspend fun dailyActivity(from: LocalDate, to: LocalDate): List<DailyHealthActivitySnapshot>
}

data class BodyMetrics(
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPercent: Double?,
    val source: String,
    val recordedAtEpochMillis: Long? = null
)
