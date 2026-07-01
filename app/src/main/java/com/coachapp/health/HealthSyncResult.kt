package com.coachapp.health

import com.coachapp.data.BodyMetricsSnapshot

data class HealthSyncResult(
    val bodySnapshot: BodyMetricsSnapshot?,
    val restingHeartRateBpm: Long?,
    val activeEnergyKcalToday: Double?,
    val missingNotes: List<String>
)

