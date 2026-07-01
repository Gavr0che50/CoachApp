package com.coachapp.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.coachapp.MainActivity

class DailyActivityInsightNotifier(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun show(insight: DailyActivityInsight) {
        ensureChannel()
        if (!canPostNotifications()) return

        notificationManager.notify(
            NOTIFICATION_ID,
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(insight.title)
                .setContentText(insight.message)
                .setStyle(Notification.BigTextStyle().bigText(insight.longMessage))
                .setContentIntent(contentIntent())
                .setAutoCancel(true)
                .build()
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Bilan activite matin",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Bilan CoachApp du jour precedent compare a la moyenne recente."
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val CHANNEL_ID = "daily_activity_insight"
        const val NOTIFICATION_ID = 2001
        const val REQUEST_CODE = 2001
    }
}
