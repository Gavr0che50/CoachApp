package com.coachapp.data

import com.coachapp.core.Science
import java.time.LocalDate

const val UserProfileSource = "Profil"
val DefaultTrainingDaysIso = setOf(1, 2, 3, 4, 5)

data class UserProfileInput(
    val heightCm: Double?,
    val weightKg: Double?,
    val ageYears: Int?,
    val trainingDaysIso: Set<Int>
)

data class UserProfileSnapshot(
    val heightCm: Double?,
    val weightKg: Double?,
    val ageYears: Int?,
    val trainingDaysIso: Set<Int>,
    val updatedAtEpochMillis: Long
) {
    fun bodySnapshot(date: LocalDate = LocalDate.now()): BodyMetricsSnapshot? {
        if (heightCm == null && weightKg == null) {
            return null
        }

        val bmi = weightKg?.let { weight ->
            heightCm?.let { height ->
                runCatching { Science.bmi(weight, height) }.getOrNull()
            }
        }

        return BodyMetricsSnapshot(
            id = "profile-${date}",
            dateIso = date.toString(),
            weightKg = weightKg,
            heightCm = heightCm,
            bodyFatPercent = null,
            bmi = bmi,
            bmr = null,
            source = UserProfileSource
        )
    }
}

fun sanitizeTrainingDays(days: Set<Int>): Set<Int> =
    days.filter { it in 1..7 }.toSortedSet()

fun effectiveBodySnapshot(
    healthSnapshot: BodyMetricsSnapshot?,
    profile: UserProfileSnapshot?,
    today: LocalDate = LocalDate.now()
): BodyMetricsSnapshot? {
    if (healthSnapshot?.dateIso == today.toString()) {
        return healthSnapshot
    }
    return profile?.bodySnapshot(today)
}
