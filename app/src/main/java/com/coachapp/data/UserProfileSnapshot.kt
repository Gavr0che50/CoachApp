package com.coachapp.data

import com.coachapp.core.Science
import com.coachapp.core.TrainingSchedule
import java.time.LocalDate

const val UserProfileSource = "Profil"
val DefaultTrainingDaysIso: Set<Int> = TrainingSchedule.DefaultTrainingDaysIso
const val DefaultTargetSessionMinutes: Int = TrainingSchedule.DefaultTargetMinutes

data class UserProfileInput(
    val heightCm: Double?,
    val weightKg: Double?,
    val ageYears: Int?,
    val trainingDaysIso: Set<Int>,
    val targetSessionMinutes: Int?
)

data class UserProfileSnapshot(
    val heightCm: Double?,
    val weightKg: Double?,
    val ageYears: Int?,
    val trainingDaysIso: Set<Int>,
    val targetSessionMinutes: Int,
    val updatedAtEpochMillis: Long
) {
    fun bodySnapshot(date: LocalDate = LocalDate.now()): BodyMetricsSnapshot? {
        if (heightCm == null && weightKg == null) return null
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
    TrainingSchedule.sanitizeTrainingDays(days)

fun sanitizeTargetSessionMinutes(minutes: Int?): Int =
    TrainingSchedule.sanitizeTargetMinutes(minutes)

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
