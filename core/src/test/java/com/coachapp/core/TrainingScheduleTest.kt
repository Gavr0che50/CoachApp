package com.coachapp.core

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingScheduleTest {
    @Test
    fun threeDaysPlanUsesPushPullLegsInSelectedDayOrder() {
        val plan = TrainingSchedule.planFor(setOf(1, 3, 5), targetMinutes = 45)

        assertEquals(listOf(1, 3, 5), plan.map { it.dayOfWeekIso })
        assertEquals(
            listOf("Push haut du corps", "Pull dos et biceps", "Jambes"),
            plan.map { it.title }
        )
        assertEquals(listOf(45, 45, 45), plan.map { it.targetMinutes })
        assertEquals(4, plan.first().blocks.first().sets.size)
    }

    @Test
    fun shortSessionsReduceVolume() {
        val plan = TrainingSchedule.planFor(setOf(2), targetMinutes = 20)

        assertEquals("Full body rapide", plan.single().title)
        assertEquals(20, plan.single().targetMinutes)
        assertEquals(2, plan.single().blocks.size)
        assertEquals(2, plan.single().blocks.first().sets.size)
    }

    @Test
    fun nextWorkoutsSkipsNonTrainingDays() {
        val next = TrainingSchedule.nextWorkouts(
            from = LocalDate.of(2026, 7, 1),
            trainingDaysIso = setOf(2, 4),
            targetMinutes = 30,
            count = 3
        )

        assertEquals(
            listOf(
                LocalDate.of(2026, 7, 2),
                LocalDate.of(2026, 7, 7),
                LocalDate.of(2026, 7, 9)
            ),
            next.map { it.date }
        )
    }
}
