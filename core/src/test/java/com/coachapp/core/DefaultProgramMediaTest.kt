package com.coachapp.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultProgramMediaTest {
    @Test
    fun defaultProgramUsesBundledIllustrationsAndNoPlaceholderVideos() {
        val exercises = DefaultProgram.weeklyPlan.flatMap { day -> day.blocks.map { it.exercise } }

        assertEquals(15, exercises.map { it.id }.toSet().size)
        exercises.forEach { exercise ->
            assertTrue(
                "${exercise.id} illustration should point to Android assets",
                exercise.illustrationAsset.startsWith("exercises/")
            )
            assertTrue(
                "${exercise.id} illustration should be a jpg",
                exercise.illustrationAsset.endsWith("${exercise.id}.jpg")
            )
            assertFalse(
                "${exercise.id} illustration should not use the old placeholder prefix",
                exercise.illustrationAsset.startsWith("assets/")
            )
            assertNull("${exercise.id} should not point to an unlicensed placeholder video", exercise.videoAsset)
        }
    }
}
