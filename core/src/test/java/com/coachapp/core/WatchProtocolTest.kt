package com.coachapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchProtocolTest {
    @Test
    fun sessionStateRoundTripIncludesRestTimer() {
        val state = WatchSessionState(
            sessionId = "session-1",
            exerciseName = "Squat",
            setIndex = 2,
            setCount = 3,
            repsMin = 8,
            repsMax = 10,
            reps = 9,
            weightKg = 50.0,
            restSeconds = 120,
            restRemainingSeconds = 87,
            isResting = true
        )

        val decoded = WatchProtocol.decodeSessionState(WatchProtocol.encodeSessionState(state))

        assertEquals(state, decoded)
    }

    @Test
    fun sessionStateDecoderAcceptsLegacyPayloadWithoutRestTimer() {
        val legacyPayload = listOf(
            "session-1",
            "Squat",
            "1",
            "3",
            "8",
            "10",
            "50.0",
            "120"
        ).joinToString("\u001F").encodeToByteArray()

        val decoded = WatchProtocol.decodeSessionState(legacyPayload)

        assertEquals(0, decoded?.restRemainingSeconds)
        assertFalse(decoded?.isResting ?: true)
        assertEquals(10, decoded?.reps)
    }

    @Test
    fun sessionStateDecoderRejectsMalformedPayload() {
        val decoded = WatchProtocol.decodeSessionState("session-only".encodeToByteArray())

        assertTrue(decoded == null)
    }
}
