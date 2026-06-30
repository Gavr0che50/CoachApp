package com.coachapp.core

import kotlin.math.pow

object Science {
    fun bmi(weightKg: Double, heightCm: Double): Double {
        require(weightKg > 0.0) { "weightKg must be positive" }
        require(heightCm > 0.0) { "heightCm must be positive" }
        val meters = heightCm / 100.0
        return weightKg / meters.pow(2.0)
    }

    fun mifflinStJeorBmr(
        weightKg: Double,
        heightCm: Double,
        ageYears: Int,
        male: Boolean
    ): Double {
        require(weightKg > 0.0) { "weightKg must be positive" }
        require(heightCm > 0.0) { "heightCm must be positive" }
        require(ageYears in 13..100) { "ageYears must be realistic" }
        val sexOffset = if (male) 5.0 else -161.0
        return 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + sexOffset
    }

    fun leanMassBmr(weightKg: Double, bodyFatPercent: Double): Double {
        require(weightKg > 0.0) { "weightKg must be positive" }
        require(bodyFatPercent in 3.0..70.0) { "bodyFatPercent must be realistic" }
        val leanMassKg = weightKg * (1.0 - bodyFatPercent / 100.0)
        return 370.0 + 21.6 * leanMassKg
    }

    fun resistanceTrainingCalories(
        weightKg: Double,
        minutes: Int,
        intensityMet: Double = 5.0
    ): Double {
        require(weightKg > 0.0) { "weightKg must be positive" }
        require(minutes > 0) { "minutes must be positive" }
        require(intensityMet in 3.0..8.0) { "intensityMet must stay within resistance-training bounds" }
        return intensityMet * 3.5 * weightKg / 200.0 * minutes
    }

    fun totalVolumeKg(reps: Int, weightKg: Double): Double {
        require(reps >= 0) { "reps must be positive or zero" }
        require(weightKg >= 0.0) { "weightKg must be positive or zero" }
        return reps * weightKg
    }

    fun nextSuggestedLoadKg(
        currentKg: Double,
        allSetsCompleted: Boolean,
        lastSetRir: Int?,
        incrementKg: Double = 2.5
    ): Double {
        require(currentKg >= 0.0) { "currentKg must be positive or zero" }
        require(incrementKg > 0.0) { "incrementKg must be positive" }
        return when {
            allSetsCompleted && (lastSetRir == null || lastSetRir >= 2) -> currentKg + incrementKg
            allSetsCompleted -> currentKg
            else -> currentKg
        }
    }
}
