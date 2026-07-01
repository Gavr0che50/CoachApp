package com.coachapp.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class HealthConnectHealthDataGateway(
    context: Context
) : HealthDataGateway {
    private val appContext = context.applicationContext

    suspend fun grantedPermissions(): Set<String> {
        return availableClient()?.permissionController?.getGrantedPermissions().orEmpty()
    }

    override suspend fun latestBodyMetrics(): BodyMetrics? {
        val client = availableClient() ?: return null
        val end = Instant.now()
        val start = end.minus(365, ChronoUnit.DAYS)
        val weightRecord = latestRecord<WeightRecord>(client, start, end)
        val heightRecord = latestRecord<HeightRecord>(client, start, end)
        val bodyFatRecord = latestRecord<BodyFatRecord>(client, start, end)
        val weightKg = weightRecord?.weight?.inKilograms
        val heightCm = heightRecord?.height?.inMeters?.times(100.0)
        val bodyFatPercent = bodyFatRecord?.percentage?.value
        val recordedAtEpochMillis = listOfNotNull(
            weightRecord?.time,
            heightRecord?.time,
            bodyFatRecord?.time
        ).maxOrNull()?.toEpochMilli()

        if (weightKg == null && heightCm == null && bodyFatPercent == null) {
            return null
        }

        return BodyMetrics(
            weightKg = weightKg,
            heightCm = heightCm,
            bodyFatPercent = bodyFatPercent,
            source = SOURCE,
            recordedAtEpochMillis = recordedAtEpochMillis
        )
    }

    override suspend fun restingHeartRateBpm(): Long? {
        val client = availableClient() ?: return null
        val end = Instant.now()
        val start = end.minus(90, ChronoUnit.DAYS)
        return latestRecord<RestingHeartRateRecord>(client, start, end)?.beatsPerMinute
    }

    override suspend fun activeEnergyKcalForCurrentDay(): Double? {
        val client = availableClient() ?: return null
        val zoneId = ZoneId.systemDefault()
        val start = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant()
        val end = Instant.now()
        return aggregateEnergy(client, ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL, start, end)?.inKilocalories
    }

    private fun availableClient(): HealthConnectClient? {
        return if (HealthConnectClient.getSdkStatus(appContext) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(appContext)
        } else {
            null
        }
    }

    override suspend fun dailyActivity(
        from: LocalDate,
        to: LocalDate
    ): List<DailyHealthActivitySnapshot> {
        require(!to.isBefore(from)) { "to must be on or after from" }
        val client = availableClient() ?: return emptyList()
        val zoneId = ZoneId.systemDefault()
        val snapshots = mutableListOf<DailyHealthActivitySnapshot>()
        var date = from

        while (!date.isAfter(to)) {
            val start = date.atStartOfDay(zoneId).toInstant()
            val end = date.plusDays(1).atStartOfDay(zoneId).toInstant()
            val activeEnergyKcal = aggregateEnergy(
                client,
                ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                start,
                end
            )?.inKilocalories
            val steps = aggregateLong(client, StepsRecord.COUNT_TOTAL, start, end)
            val missingNotes = buildList {
                if (activeEnergyKcal == null) add("Energie active Health Connect indisponible")
                if (steps == null) add("Pas Health Connect indisponibles")
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

    private suspend inline fun <reified T : Record> latestRecord(
        client: HealthConnectClient,
        start: Instant,
        end: Instant
    ): T? {
        return client.readRecords(
            ReadRecordsRequest(
                recordType = T::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
                ascendingOrder = false,
                pageSize = 1
            )
        ).records.firstOrNull()
    }

    private suspend fun aggregateEnergy(
        client: HealthConnectClient,
        metric: AggregateMetric<androidx.health.connect.client.units.Energy>,
        start: Instant,
        end: Instant
    ): androidx.health.connect.client.units.Energy? {
        return client.aggregate(
            AggregateRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )[metric]
    }

    private suspend fun aggregateLong(
        client: HealthConnectClient,
        metric: AggregateMetric<Long>,
        start: Instant,
        end: Instant
    ): Long? {
        return client.aggregate(
            AggregateRequest(
                metrics = setOf(metric),
                timeRangeFilter = TimeRangeFilter.between(start, end)
            )
        )[metric]
    }

    companion object {
        const val SOURCE = "Health Connect"

        val DAILY_ACTIVITY_READ_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class)
        )

        val READ_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(HeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        ) + DAILY_ACTIVITY_READ_PERMISSIONS
    }
}
