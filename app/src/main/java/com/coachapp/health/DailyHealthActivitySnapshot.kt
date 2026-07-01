package com.coachapp.health

data class DailyHealthActivityResult(
    val snapshots: List<DailyHealthActivitySnapshot>,
    val missingNote: String?
)

data class DailyHealthActivitySnapshot(
    val dateIso: String,
    val activeEnergyKcal: Double?,
    val steps: Long?,
    val missingNotes: List<String>
)
