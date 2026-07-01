package com.coachapp.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ScienceTest {
    @Test
    fun bmiUsesHeightInCentimeters() {
        val result = Science.bmi(weightKg = 80.0, heightCm = 180.0)

        assertEquals(24.69, result, 0.01)
    }

    @Test
    fun mifflinStJeorBmrUsesMaleAndFemaleOffsets() {
        val male = Science.mifflinStJeorBmr(
            weightKg = 80.0,
            heightCm = 180.0,
            ageYears = 30,
            male = true
        )
        val female = Science.mifflinStJeorBmr(
            weightKg = 80.0,
            heightCm = 180.0,
            ageYears = 30,
            male = false
        )

        assertEquals(1780.0, male, 0.01)
        assertEquals(1614.0, female, 0.01)
    }

    @Test
    fun leanMassBmrUsesKatchMcArdleFormula() {
        val result = Science.leanMassBmr(weightKg = 80.0, bodyFatPercent = 20.0)

        assertEquals(1752.4, result, 0.01)
    }

    @Test
    fun resistanceTrainingCaloriesUsesMetFormula() {
        val result = Science.resistanceTrainingCalories(
            weightKg = 80.0,
            minutes = 30,
            intensityMet = 5.0
        )

        assertEquals(210.0, result, 0.01)
    }

    @Test
    fun totalVolumeMultipliesRepsByWeight() {
        val result = Science.totalVolumeKg(reps = 10, weightKg = 60.0)

        assertEquals(600.0, result, 0.01)
    }

    @Test
    fun nextSuggestedLoadProgressesOnlyWhenCompletedAndRirAllowsIt() {
        val progressed = Science.nextSuggestedLoadKg(
            currentKg = 60.0,
            allSetsCompleted = true,
            lastSetRir = 2
        )
        val tooHard = Science.nextSuggestedLoadKg(
            currentKg = 60.0,
            allSetsCompleted = true,
            lastSetRir = 1
        )
        val incomplete = Science.nextSuggestedLoadKg(
            currentKg = 60.0,
            allSetsCompleted = false,
            lastSetRir = 3
        )

        assertEquals(62.5, progressed, 0.01)
        assertEquals(60.0, tooHard, 0.01)
        assertEquals(60.0, incomplete, 0.01)
    }
}
