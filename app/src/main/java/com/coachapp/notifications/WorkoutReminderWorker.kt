package com.coachapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.coachapp.data.CoachRepository
import com.coachapp.data.DefaultTrainingDaysIso
import com.coachapp.data.local.CoachDatabase
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class WorkoutReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val database = CoachDatabase.create(applicationContext)
        val repository = CoachRepository(database)
        val completedSetsToday = repository.dailyCoachActivity(limit = 1)
            .firstOrNull { it.dateIso == today.toString() }
            ?.completedSets ?: 0
        val trainingDaysIso = repository.userProfile()?.trainingDaysIso ?: DefaultTrainingDaysIso

        workoutReminderForDate(
            date = today,
            completedSetsToday = completedSetsToday,
            trainingDaysIso = trainingDaysIso
        )?.let { reminder ->
            WorkoutReminderNotifier(applicationContext).show(reminder)
        }

        return Result.success()
    }
}

object WorkoutReminderScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextWorkoutReminder(ZonedDateTime.now()).toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private const val WORK_NAME = "workout_reminder"
}
