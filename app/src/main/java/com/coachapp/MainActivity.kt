package com.coachapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.coachapp.core.WatchProtocol
import com.coachapp.ui.CoachAppUi
import com.coachapp.watch.WearActionEvent
import com.coachapp.watch.WearActionType
import com.coachapp.watch.WearSessionMessenger
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    private val wearActionEvent = mutableStateOf<WearActionEvent?>(null)
    private var wearActionSequence = 0

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Notifications are optional; if denied, the app still saves and displays summaries.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        val coachApp = application as CoachAppApplication
        val wearMessenger = WearSessionMessenger(this)
        setContent {
            CoachAppUi(
                latestSessionSummary = coachApp::latestSessionSummary,
                dailyCoachActivity = coachApp::dailyCoachActivity,
                dailyHealthActivity = coachApp::dailyHealthActivity,
                wearActionEvent = wearActionEvent.value,
                onRequestHealthConnectPermissions = {
                    coachApp.requestSamsungHealthPermissions(this)
                },
                onSyncHealthConnect = coachApp::syncHealthConnect,
                userProfile = coachApp::userProfile,
                onSaveUserProfile = coachApp::saveUserProfile,
                onSessionStateChanged = wearMessenger::sendSessionStateToConnectedNodes,
                onSessionFinished = { workoutDay, startedAt, endedAt, completedSets, calories ->
                    val summary = coachApp.saveCompletedSession(
                        workoutDay = workoutDay,
                        startedAtEpochMillis = startedAt,
                        endedAtEpochMillis = endedAt,
                        completedSets = completedSets,
                        estimatedCalories = calories
                    )
                    wearMessenger.sendSessionFinishedToConnectedNodes(summary.sessionId)
                    summary
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        Wearable.getMessageClient(this).removeListener(this)
        super.onPause()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val type = when (messageEvent.path) {
            WatchProtocol.PATH_SET_DONE -> WearActionType.SetDone
            WatchProtocol.PATH_FINISH_SESSION -> WearActionType.FinishSession
            WatchProtocol.PATH_WEIGHT_UP -> WearActionType.WeightUp
            WatchProtocol.PATH_WEIGHT_DOWN -> WearActionType.WeightDown
            WatchProtocol.PATH_REPS_UP -> WearActionType.RepsUp
            WatchProtocol.PATH_REPS_DOWN -> WearActionType.RepsDown
            WatchProtocol.PATH_REST_UP -> WearActionType.RestUp
            WatchProtocol.PATH_REST_DOWN -> WearActionType.RestDown
            WatchProtocol.PATH_ADD_SET -> WearActionType.AddSet
            WatchProtocol.PATH_REMOVE_SET -> WearActionType.RemoveSet
            else -> return
        }
        wearActionSequence += 1
        wearActionEvent.value = WearActionEvent(
            sequence = wearActionSequence,
            type = type,
            sessionId = messageEvent.data.decodeToString().ifBlank { null }
        )
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
