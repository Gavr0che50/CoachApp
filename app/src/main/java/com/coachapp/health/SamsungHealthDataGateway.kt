package com.coachapp.health

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.time.LocalDate

class SamsungHealthDataGateway(
    context: Context
) : HealthDataGateway {
    private val appContext = context.applicationContext
    private val bridge: SamsungHealthBridge? by lazy { loadBridge() }

    suspend fun hasRequiredPermissions(): Boolean =
        bridge?.hasRequiredPermissions() == true

    suspend fun requestPermissions(activity: Activity): Boolean =
        if (bridge == null) {
            openSamsungHealth(activity)
            false
        } else {
            bridge?.requestPermissions(activity) == true
        }

    fun availabilityNote(): String? = when {
        !isSamsungHealthInstalled() -> "Samsung Health non installe"
        bridge == null -> "SDK Samsung Health absent"
        else -> null
    }

    override suspend fun latestBodyMetrics(): BodyMetrics? =
        bridge?.latestBodyMetrics()

    override suspend fun restingHeartRateBpm(): Long? =
        bridge?.restingHeartRateBpm()

    override suspend fun activeEnergyKcalForCurrentDay(): Double? =
        bridge?.activeEnergyKcalForCurrentDay()

    override suspend fun dailyActivity(
        from: LocalDate,
        to: LocalDate
    ): List<DailyHealthActivitySnapshot> =
        bridge?.dailyActivity(from, to).orEmpty()

    fun openSamsungHealth(activity: Activity): Boolean {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(SAMSUNG_HEALTH_PACKAGE)
        val intent = launchIntent ?: Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$SAMSUNG_HEALTH_PACKAGE")
        )

        return runCatching {
            activity.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.recoverCatching {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$SAMSUNG_HEALTH_PACKAGE")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }.isSuccess
    }

    private fun isSamsungHealthInstalled(): Boolean =
        runCatching {
            appContext.packageManager.getPackageInfo(SAMSUNG_HEALTH_PACKAGE, PackageManager.GET_ACTIVITIES)
        }.isSuccess

    private fun loadBridge(): SamsungHealthBridge? =
        runCatching {
            Class.forName(BRIDGE_CLASS_NAME)
                .getConstructor(Context::class.java)
                .newInstance(appContext) as SamsungHealthBridge
        }.getOrNull()

    companion object {
        const val SOURCE = "Samsung Health"
        private const val SAMSUNG_HEALTH_PACKAGE = "com.sec.android.app.shealth"
        private const val BRIDGE_CLASS_NAME = "com.coachapp.health.SamsungHealthDataSdkBridge"
    }
}

internal interface SamsungHealthBridge {
    suspend fun hasRequiredPermissions(): Boolean
    suspend fun requestPermissions(activity: Activity): Boolean
    suspend fun latestBodyMetrics(): BodyMetrics?
    suspend fun restingHeartRateBpm(): Long?
    suspend fun activeEnergyKcalForCurrentDay(): Double?
    suspend fun dailyActivity(from: LocalDate, to: LocalDate): List<DailyHealthActivitySnapshot>
}
