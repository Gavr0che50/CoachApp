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
import com.coachapp.data.SessionSummarySnapshot

class SessionSummaryNotifier(
    private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showSessionSummary(summary: SessionSummarySnapshot) {
        ensureChannel()

        if (!canPostNotifications()) {
            return
        }

        notificationManager.notify(
            NOTIFICATION_ID,
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Seance terminee")
                .setContentText(notificationText(summary))
                .setStyle(Notification.BigTextStyle().bigText(bigText(summary)))
                .setContentIntent(contentIntent())
                .setAutoCancel(true)
                .build()
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Resumes de seance",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Resume concis affiche quand une seance CoachApp est terminee."
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notificationText(summary: SessionSummarySnapshot): String {
        return when (summary.recordType) {
            "volume_record" -> "Record volume ${formatDelta(summary)}. Calories estimees : ${formatCalories(summary)}."
            "stable" -> "Volume stable. Calories estimees : ${formatCalories(summary)}."
            "baseline" -> "Reference creee. Calories estimees : ${formatCalories(summary)}."
            else -> "Volume ${formatDelta(summary)}. Calories estimees : ${formatCalories(summary)}."
        }
    }

    private fun bigText(summary: SessionSummarySnapshot): String {
        return "${notificationText(summary)} ${summary.nextHint}"
    }

    private fun formatDelta(summary: SessionSummarySnapshot): String {
        val delta = summary.volumeDeltaKg ?: return "initial"
        val sign = if (delta > 0.0) "+" else ""
        return "$sign${delta.toInt()} kg"
    }

    private fun formatCalories(summary: SessionSummarySnapshot): String {
        return "${summary.estimatedCalories?.toInt() ?: 0} kcal"
    }

    private companion object {
        const val CHANNEL_ID = "session_summary"
        const val NOTIFICATION_ID = 1001
    }
}
