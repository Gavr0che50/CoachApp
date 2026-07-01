package com.coachapp

import android.app.Application
import com.coachapp.core.WorkoutDay
import com.coachapp.data.CompletedSetRecord
import com.coachapp.data.CoachRepository
import com.coachapp.data.DailyCoachActivitySnapshot
import com.coachapp.data.SessionSummarySnapshot
import com.coachapp.data.UserProfileInput
import com.coachapp.data.UserProfileSnapshot
import com.coachapp.data.UserProfileSource
import com.coachapp.data.effectiveBodySnapshot
import com.coachapp.data.local.CoachDatabase
import com.coachapp.health.DailyHealthActivityResult
import com.coachapp.health.HealthSyncResult
import com.coachapp.health.SamsungFirstHealthDataGateway
import com.coachapp.notifications.DailyActivityInsightScheduler
import com.coachapp.notifications.SessionSummaryNotifier
import com.coachapp.notifications.WorkoutReminderScheduler
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoachAppApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database: CoachDatabase by lazy { CoachDatabase.create(this) }
    private val repository: CoachRepository by lazy { CoachRepository(database) }
    private val healthDataGateway: SamsungFirstHealthDataGateway by lazy {
        SamsungFirstHealthDataGateway(this)
    }
    private val summaryNotifier: SessionSummaryNotifier by lazy { SessionSummaryNotifier(this) }

    override fun onCreate() {
        super.onCreate()
        DailyActivityInsightScheduler.schedule(this)
        WorkoutReminderScheduler.schedule(this)
        applicationScope.launch {
            repository.seedDefaultProgram()
        }
    }

    suspend fun latestSessionSummary(): SessionSummarySnapshot? =
        withContext(Dispatchers.IO) {
            repository.latestSessionSummary()
        }

    suspend fun dailyCoachActivity(): List<DailyCoachActivitySnapshot> =
        withContext(Dispatchers.IO) {
            repository.dailyCoachActivity()
        }

    suspend fun dailyHealthActivity(): DailyHealthActivityResult =
        withContext(Dispatchers.IO) {
            val requiredPermissions = SamsungFirstHealthDataGateway.DAILY_ACTIVITY_READ_PERMISSIONS
            val missingPermissions = requiredPermissions - healthDataGateway.grantedPermissions()
            if (missingPermissions.isNotEmpty()) {
                return@withContext DailyHealthActivityResult(
                    snapshots = emptyList(),
                    missingNote = healthDataGateway.samsungAvailabilityNote()
                        ?: "Autorisations Samsung Health activite manquantes"
                )
            }

            val today = LocalDate.now()
            val snapshots = healthDataGateway.dailyActivity(
                from = today.minusDays(13),
                to = today
            )
            val hasAnyHealthData = snapshots.any {
                it.activeEnergyKcal != null || it.steps != null
            }
            DailyHealthActivityResult(
                snapshots = if (hasAnyHealthData) snapshots else emptyList(),
                missingNote = if (hasAnyHealthData) null else "Aucune donnee Samsung Health sur la periode"
            )
        }

    fun healthConnectReadPermissions(): Set<String> {
        return healthDataGateway.healthConnectReadPermissions()
    }

    fun requestSamsungHealthPermissions(activity: android.app.Activity) {
        applicationScope.launch(Dispatchers.Main) {
            healthDataGateway.requestSamsungPermissions(activity)
        }
    }

    suspend fun syncHealthConnect(): HealthSyncResult =
        withContext(Dispatchers.IO) {
            val grantedPermissions = healthDataGateway.grantedPermissions()
            val missingPermissions = healthConnectReadPermissions() - grantedPermissions
            if (missingPermissions.isNotEmpty()) {
                return@withContext HealthSyncResult(
                    bodySnapshot = null,
                    restingHeartRateBpm = null,
                    activeEnergyKcalToday = null,
                    missingNotes = listOf(
                        healthDataGateway.samsungAvailabilityNote()
                            ?: "Autorisations Samsung Health manquantes"
                    )
                )
            }

            val bodyMetrics = healthDataGateway.latestBodyMetrics()
            val healthBodySnapshot = bodyMetrics?.let { repository.saveBodyMetricsSnapshot(it) }
            val profile = repository.userProfile()
            val bodySnapshot = effectiveBodySnapshot(
                healthSnapshot = healthBodySnapshot,
                profile = profile,
                today = LocalDate.now()
            )
            val restingHeartRate = healthDataGateway.restingHeartRateBpm()
            val activeEnergy = healthDataGateway.activeEnergyKcalForCurrentDay()
            val missingNotes = buildList {
                if (bodySnapshot == null) add("Aucune donnee poids/taille/masse grasse disponible")
                if (bodySnapshot?.source == UserProfileSource) add("Profil utilise pour poids/taille")
                if (restingHeartRate == null) add("Frequence cardiaque de repos indisponible")
                if (activeEnergy == null) add("Depense energetique active indisponible aujourd'hui")
            }

            HealthSyncResult(
                bodySnapshot = bodySnapshot,
                restingHeartRateBpm = restingHeartRate,
                activeEnergyKcalToday = activeEnergy,
                missingNotes = missingNotes
            )
        }

    suspend fun saveCompletedSession(
        workoutDay: WorkoutDay,
        startedAtEpochMillis: Long,
        endedAtEpochMillis: Long,
        completedSets: List<CompletedSetRecord>,
        estimatedCalories: Double
    ): SessionSummarySnapshot =
        withContext(Dispatchers.IO) {
            val summary = repository.saveCompletedSession(
                workoutDay = workoutDay,
                startedAtEpochMillis = startedAtEpochMillis,
                endedAtEpochMillis = endedAtEpochMillis,
                completedSets = completedSets,
                estimatedCalories = estimatedCalories
            )
            summaryNotifier.showSessionSummary(summary)
            summary
        }

    suspend fun userProfile(): UserProfileSnapshot? =
        withContext(Dispatchers.IO) {
            repository.userProfile()
        }

    suspend fun saveUserProfile(input: UserProfileInput): UserProfileSnapshot =
        withContext(Dispatchers.IO) {
            repository.saveUserProfile(input)
        }
}
