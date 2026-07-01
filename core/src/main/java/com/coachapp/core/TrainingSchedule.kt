package com.coachapp.core

import java.time.LocalDate

data class ScheduledWorkout(
    val date: LocalDate,
    val workoutDay: WorkoutDay
)

object TrainingSchedule {
    val DefaultTrainingDaysIso: Set<Int> = setOf(1, 2, 3, 4, 5)
    const val DefaultTargetMinutes: Int = 30
    private const val MinTargetMinutes = 20
    private const val MaxTargetMinutes = 90

    fun sanitizeTrainingDays(days: Set<Int>): Set<Int> =
        days.filter { it in 1..7 }.toSortedSet()

    fun sanitizeTargetMinutes(minutes: Int?): Int =
        (minutes ?: DefaultTargetMinutes).coerceIn(MinTargetMinutes, MaxTargetMinutes)

    fun planFor(
        trainingDaysIso: Set<Int>,
        targetMinutes: Int
    ): List<WorkoutDay> {
        val days = sanitizeTrainingDays(trainingDaysIso).ifEmpty { DefaultTrainingDaysIso }
        val templates = templatesForFrequency(days.size)
        val sanitizedMinutes = sanitizeTargetMinutes(targetMinutes)

        return days.mapIndexed { index, isoDay ->
            templates[index % templates.size].adaptedTo(isoDay, sanitizedMinutes)
        }
    }

    fun nextWorkouts(
        from: LocalDate,
        trainingDaysIso: Set<Int>,
        targetMinutes: Int,
        count: Int = 3
    ): List<ScheduledWorkout> {
        require(count > 0) { "count must be positive" }
        val plan = planFor(trainingDaysIso, targetMinutes)
        val byIsoDay = plan.associateBy { it.dayOfWeekIso }
        val scheduled = mutableListOf<ScheduledWorkout>()
        var date = from

        while (scheduled.size < count) {
            byIsoDay[date.dayOfWeek.value]?.let { workoutDay ->
                scheduled += ScheduledWorkout(date, workoutDay)
            }
            date = date.plusDays(1)
        }

        return scheduled
    }

    private fun templatesForFrequency(dayCount: Int): List<WorkoutDay> {
        val byId = DefaultProgram.weeklyPlan.associateBy { it.id }
        return when (dayCount) {
            1 -> listOf(byId.getValue("day-5-full-body"))
            2 -> listOf(byId.getValue("day-5-full-body"), byId.getValue("day-4-upper"))
            3 -> listOf(
                byId.getValue("day-1-push"),
                byId.getValue("day-2-pull"),
                byId.getValue("day-3-legs")
            )
            4 -> listOf(
                byId.getValue("day-1-push"),
                byId.getValue("day-2-pull"),
                byId.getValue("day-3-legs"),
                byId.getValue("day-4-upper")
            )
            else -> DefaultProgram.weeklyPlan
        }
    }

    private fun WorkoutDay.adaptedTo(dayOfWeekIso: Int, targetMinutes: Int): WorkoutDay {
        val blockCount = if (targetMinutes <= 25) 2 else blocks.size
        val setCount = when {
            targetMinutes <= 25 -> 2
            targetMinutes <= 35 -> 3
            targetMinutes <= 50 -> 4
            else -> 5
        }

        return copy(
            dayOfWeekIso = dayOfWeekIso,
            targetMinutes = targetMinutes,
            blocks = blocks.take(blockCount).map { block ->
                block.copy(sets = block.sets.resizedTo(setCount))
            }
        )
    }

    private fun List<SetTarget>.resizedTo(setCount: Int): List<SetTarget> {
        if (isEmpty()) return emptyList()
        return (1..setCount).map { index ->
            val source = getOrNull(index - 1) ?: last()
            source.copy(index = index)
        }
    }
}
