package com.coachapp.health

import android.app.Activity
import android.content.Context
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.LocalDateFilter
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.Ordering
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

internal class SamsungHealthDataSdkBridge(
    context: Context
) : SamsungHealthBridge {
    private val appContext = context.applicationContext
    private val healthDataStore: HealthDataStore by lazy {
        HealthDataService.getStore(appContext)
    }

    private val requiredPermissions = setOf(
        Permission.of(DataTypes.ACTIVITY_SUMMARY, AccessType.READ),
        Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ),
        Permission.of(DataTypes.HEART_RATE, AccessType.READ),
        Permission.of(DataTypes.STEPS, AccessType.READ),
        Permission.of(DataTypes.USER_PROFILE, AccessType.READ)
    )

    override suspend fun hasRequiredPermissions(): Boolean =
        runCatching {
            healthDataStore.getGrantedPermissions(requiredPermissions)
                .containsAll(requiredPermissions)
        }.getOrDefault(false)

    override suspend fun requestPermissions(activity: Activity): Boolean =
        runCatching {
            val granted = healthDataStore.getGrantedPermissions(requiredPermissions)
            if (granted.containsAll(requiredPermissions)) {
                true
            } else {
                val missing = requiredPermissions - granted
                healthDataStore.requestPermissions(missing, activity).containsAll(missing)
            }
        }.recoverCatching { error ->
            if (error is ResolvablePlatformException && error.hasResolution) {
                error.resolve(activity)
            }
            false
        }.getOrDefault(false)

    override suspend fun latestBodyMetrics(): BodyMetrics? =
        runSamsung {
            val userProfile = healthDataStore.readData(
                DataTypes.USER_PROFILE.readDataRequestBuilder.build()
            ).dataList.firstOrNull()

            val bodyComposition = healthDataStore.readData(
                DataTypes.BODY_COMPOSITION.readDataRequestBuilder
                    .setOrdering(Ordering.DESC)
                    .setLimit(1)
                    .build()
            ).dataList.firstOrNull()

            val weightKg = bodyComposition?.getValue(DataType.BodyCompositionType.WEIGHT)
                ?: userProfile?.getValue(DataType.UserProfileDataType.WEIGHT)
            val heightCm = bodyComposition?.getValue(DataType.BodyCompositionType.HEIGHT)
                ?: userProfile?.getValue(DataType.UserProfileDataType.HEIGHT)
            val bodyFatPercent = bodyComposition?.getValue(DataType.BodyCompositionType.BODY_FAT)

            if (weightKg == null && heightCm == null && bodyFatPercent == null) {
                null
            } else {
                BodyMetrics(
                    weightKg = weightKg?.toDouble(),
                    heightCm = heightCm?.toDouble(),
                    bodyFatPercent = bodyFatPercent?.toDouble(),
                    source = SamsungHealthDataGateway.SOURCE,
                    recordedAtEpochMillis = null
                )
            }
        }

    override suspend fun restingHeartRateBpm(): Long? =
        runSamsung {
            val end = LocalDateTime.now()
            val start = end.minusDays(90)
            val readRequest = DataTypes.HEART_RATE.readDataRequestBuilder
                .setLocalTimeFilter(LocalTimeFilter.of(start, end))
                .setOrdering(Ordering.DESC)
                .setLimit(1)
                .build()

            healthDataStore.readData(readRequest).dataList.firstOrNull()
                ?.getValue(DataType.HeartRateType.HEART_RATE)
                ?.toLong()
        }

    override suspend fun activeEnergyKcalForCurrentDay(): Double? {
        val today = LocalDate.now()
        return activeEnergyKcal(today.atStartOfDay(), LocalDateTime.now())
    }

    override suspend fun dailyActivity(
        from: LocalDate,
        to: LocalDate
    ): List<DailyHealthActivitySnapshot> {
        require(!to.isBefore(from)) { "Expected to on or after from" }
        val snapshots = mutableListOf<DailyHealthActivitySnapshot>()
        var date = from

        while (!date.isAfter(to)) {
            val start = date.atStartOfDay()
            val end = date.plusDays(1).atStartOfDay()
            val activeEnergyKcal = activeEnergyKcal(start, end)
            val steps = steps(start, end)
            val missingNotes = buildList {
                if (activeEnergyKcal == null) add("Energie active Samsung Health indisponible")
                if (steps == null) add("Pas Samsung Health indisponibles")
            }

            snapshots += DailyHealthActivitySnapshot(
                dateIso = date.toString(),
                activeEnergyKcal = activeEnergyKcal,
                steps = steps,
                missingNotes = missingNotes
            )
            date = date.plusDays(1)
        }

        return snapshots
    }

    private suspend fun activeEnergyKcal(
        start: LocalDateTime,
        end: LocalDateTime
    ): Double? =
        runSamsung {
            val request = DataType.ActivitySummaryType.TOTAL_ACTIVE_CALORIES_BURNED.requestBuilder
                .setLocalTimeFilter(LocalTimeFilter.of(start, end))
                .build()
            healthDataStore.aggregateData(request).dataList
                .mapNotNull { it.value }
                .sum()
                .takeIf { it > 0f }
                ?.toDouble()
        }

    private suspend fun steps(
        start: LocalDateTime,
        end: LocalDateTime
    ): Long? =
        runSamsung {
            val request = DataType.StepsType.TOTAL.requestBuilder
                .setLocalTimeFilter(LocalTimeFilter.of(start, end))
                .build()
            healthDataStore.aggregateData(request).dataList
                .mapNotNull { it.value }
                .sum()
                .takeIf { it > 0L }
        }

    private suspend fun <T> runSamsung(block: suspend () -> T): T? =
        try {
            block()
        } catch (error: HealthDataException) {
            null
        }
}
