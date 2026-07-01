package com.coachapp.health

import android.app.Activity
import android.content.Context
import java.time.LocalDate

class SamsungFirstHealthDataGateway(
    context: Context
) : HealthDataGateway {
    private val samsungGateway = SamsungHealthDataGateway(context)
    private val healthConnectGateway = HealthConnectHealthDataGateway(context)

    suspend fun grantedPermissions(): Set<String> =
        if (samsungGateway.hasRequiredPermissions()) {
            READ_PERMISSIONS
        } else {
            healthConnectGateway.grantedPermissions()
        }

    fun healthConnectReadPermissions(): Set<String> = READ_PERMISSIONS

    suspend fun requestSamsungPermissions(activity: Activity): Boolean =
        samsungGateway.requestPermissions(activity)

    fun samsungAvailabilityNote(): String? =
        samsungGateway.availabilityNote()

    override suspend fun latestBodyMetrics(): BodyMetrics? =
        samsungGateway.latestBodyMetrics() ?: healthConnectGateway.latestBodyMetrics()

    override suspend fun restingHeartRateBpm(): Long? =
        samsungGateway.restingHeartRateBpm() ?: healthConnectGateway.restingHeartRateBpm()

    override suspend fun activeEnergyKcalForCurrentDay(): Double? =
        samsungGateway.activeEnergyKcalForCurrentDay()
            ?: healthConnectGateway.activeEnergyKcalForCurrentDay()

    override suspend fun dailyActivity(
        from: LocalDate,
        to: LocalDate
    ): List<DailyHealthActivitySnapshot> {
        val samsungSnapshots = samsungGateway.dailyActivity(from, to)
        return samsungSnapshots.ifEmpty {
            healthConnectGateway.dailyActivity(from, to)
        }
    }

    companion object {
        val DAILY_ACTIVITY_READ_PERMISSIONS: Set<String> =
            HealthConnectHealthDataGateway.DAILY_ACTIVITY_READ_PERMISSIONS
        val READ_PERMISSIONS: Set<String> =
            HealthConnectHealthDataGateway.READ_PERMISSIONS
    }
}
