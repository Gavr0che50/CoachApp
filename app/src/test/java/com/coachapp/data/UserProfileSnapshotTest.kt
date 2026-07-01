package com.coachapp.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserProfileSnapshotTest {
    @Test
    fun sanitizesTrainingDays() {
        assertEquals(setOf(1, 3, 7), sanitizeTrainingDays(setOf(0, 1, 3, 7, 8)))
    }

    @Test
    fun sanitizesTargetSessionMinutes() {
        assertEquals(20, sanitizeTargetSessionMinutes(10))
        assertEquals(45, sanitizeTargetSessionMinutes(45))
        assertEquals(90, sanitizeTargetSessionMinutes(120))
        assertEquals(DefaultTargetSessionMinutes, sanitizeTargetSessionMinutes(null))
    }

    @Test
    fun profileBodySnapshotComputesBmi() {
        val profile = UserProfileSnapshot(
            heightCm = 180.0,
            weightKg = 81.0,
            ageYears = 35,
            trainingDaysIso = setOf(1, 3, 5),
            targetSessionMinutes = 45,
            updatedAtEpochMillis = 1L
        )

        val snapshot = profile.bodySnapshot(LocalDate.of(2026, 7, 1))

        assertEquals("Profil", snapshot?.source)
        assertEquals("2026-07-01", snapshot?.dateIso)
        assertEquals(25.0, snapshot?.bmi ?: 0.0, 0.01)
    }

    @Test
    fun keepsCurrentHealthConnectSnapshotFirst() {
        val today = LocalDate.of(2026, 7, 1)
        val health = BodyMetricsSnapshot(
            id = "hc",
            dateIso = "2026-07-01",
            weightKg = 82.0,
            heightCm = 181.0,
            bodyFatPercent = null,
            bmi = 25.0,
            bmr = null,
            source = "Health Connect"
        )
        val profile = UserProfileSnapshot(180.0, 80.0, 30, setOf(1), 30, 1L)

        assertEquals(health, effectiveBodySnapshot(health, profile, today))
    }

    @Test
    fun usesProfileWhenHealthConnectSnapshotIsOutdated() {
        val today = LocalDate.of(2026, 7, 1)
        val health = BodyMetricsSnapshot(
            id = "hc",
            dateIso = "2026-06-20",
            weightKg = 82.0,
            heightCm = 181.0,
            bodyFatPercent = null,
            bmi = 25.0,
            bmr = null,
            source = "Health Connect"
        )
        val profile = UserProfileSnapshot(180.0, 80.0, 30, setOf(1), 30, 1L)

        val snapshot = effectiveBodySnapshot(health, profile, today)

        assertEquals("Profil", snapshot?.source)
        assertEquals(80.0, snapshot?.weightKg ?: 0.0, 0.01)
    }

    @Test
    fun returnsNullWithoutAnyBodyTruth() {
        val profile = UserProfileSnapshot(null, null, null, setOf(1), 30, 1L)

        assertNull(profile.bodySnapshot(LocalDate.of(2026, 7, 1)))
    }
}
