package com.coachapp.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.coachapp.data.CoachRepository
import com.coachapp.data.local.CoachDatabase
import com.coachapp.health.HealthConnectHealthDataGateway
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DailyActivityInsightWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val database = CoachDatabase.create(applicationContext)
        val repository = CoachRepository(database)
        val healthGateway = HealthConnectHealthDataGateway(applicationContext)
        val referenceDate = LocalDate.now().minusDays(1)
        val fromDate = referenceDate.minusDays(DailyInsightBaselineDays.toLong())

        val coachByDate = repository.dailyCoachActivity(limit = DailyInsightBaselineDays + 1)
            .associateBy { LocalDate.parse(it.dateIso) }

        val healthByDate = if (
            HealthConnectHealthDataGateway.DAILY_ACTIVITY_READ_PERMISSIONS
                .all { it in healthGateway.grantedPermissions() }
        ) {
            healthGateway.dailyActivity(from = fromDate, to = referenceDate)
                .associateBy { LocalDate.parse(it.dateIso) }
        } else {
            emptyMap()
        }

        val days = generateSequence(fromDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(referenceDate) }
            .map { date ->
                val coach = coachByDate[date]
                val health = healthByDate[date]
                DailyActivityContribution(
                    date = date,
                    coachCalories = coach?.workoutCalories ?: 0.0,
                    healthCalories = health?.activeEnergyKcal,
                    completedSets = coach?.completedSets ?: 0,
                    steps = health?.steps
                )
            }
            .toList()

        val insight = buildDailyActivityInsight(
            days = days,
            referenceDate = referenceDate
        ) ?: return Result.success()

        DailyActivityInsightNotifier(applicationContext).show(insight)
        return Result.success()
    }
}

object DailyActivityInsightScheduler {
    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyActivityInsightWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNextMorningMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun delayUntilNextMorningMillis(): Long {
        val now = ZonedDateTime.now()
        var nextRun = now.toLocalDate().atTime(LocalTime.of(8, 0)).atZone(now.zone)
        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }
        return Duration.between(now, nextRun).toMillis()
    }

    private const val WORK_NAME = "daily_activity_insight"
}
