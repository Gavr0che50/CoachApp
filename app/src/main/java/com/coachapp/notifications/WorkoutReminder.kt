package com.coachapp.notifications

import com.coachapp.core.DefaultProgram
import com.coachapp.core.WorkoutDay
import com.coachapp.data.DefaultTrainingDaysIso
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

const val WorkoutReminderHour = 18
const val WorkoutReminderMinute = 0

data class WorkoutReminder(
    val title: String,
    val message: String
)

fun workoutReminderForDate(
    date: LocalDate,
    completedSetsToday: Int,
    trainingDaysIso: Set<Int> = DefaultTrainingDaysIso,
    plan: List<WorkoutDay> = DefaultProgram.weeklyPlan
): WorkoutReminder? {
    if (completedSetsToday > 0) {
        return null
    }

    if (date.dayOfWeek.value !in trainingDaysIso) {
        return null
    }

    val workout = plan.firstOrNull { it.dayOfWeekIso == date.dayOfWeek.value }

    return WorkoutReminder(
        title = "Seance du jour",
        message = "${workout?.title ?: "Seance libre"}: ${workout?.targetMinutes ?: 30} min. Ouvre CoachApp quand tu demarres."
    )
}

fun delayUntilNextWorkoutReminder(
    now: ZonedDateTime,
    reminderTime: LocalTime = LocalTime.of(WorkoutReminderHour, WorkoutReminderMinute)
): Duration {
    var nextRun = now.toLocalDate().atTime(reminderTime).atZone(now.zone)
    if (!nextRun.isAfter(now)) {
        nextRun = nextRun.plusDays(1)
    }
    return Duration.between(now, nextRun)
}
