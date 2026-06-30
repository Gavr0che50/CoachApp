package com.coachapp.health

interface HealthDataGateway {
    suspend fun latestBodyMetrics(): BodyMetrics?
    suspend fun restingHeartRateBpm(): Long?
    suspend fun activeEnergyKcalForCurrentDay(): Double?
}

data class BodyMetrics(
    val weightKg: Double?,
    val heightCm: Double?,
    val bodyFatPercent: Double?,
    val source: String
)
