package com.coachapp.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.coachapp.core.WatchProtocol
import com.coachapp.core.WatchSessionState
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    private val remoteSessionState = mutableStateOf<WatchSessionState?>(null)
    private val remoteSessionFinished = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val phoneActionMessenger = PhoneActionMessenger(this)
        setContent {
            WatchWorkoutApp(
                remoteState = remoteSessionState.value,
                remoteFinished = remoteSessionFinished.value,
                onRemoteSetDone = { phoneActionMessenger.sendSetDone(remoteSessionState.value?.sessionId) },
                onRemoteFinish = { phoneActionMessenger.sendFinishSession(remoteSessionState.value?.sessionId) },
                onRemoteWeightUp = { phoneActionMessenger.sendWeightUp(remoteSessionState.value?.sessionId) },
                onRemoteWeightDown = { phoneActionMessenger.sendWeightDown(remoteSessionState.value?.sessionId) },
                onRemoteRepsUp = { phoneActionMessenger.sendRepsUp(remoteSessionState.value?.sessionId) },
                onRemoteRepsDown = { phoneActionMessenger.sendRepsDown(remoteSessionState.value?.sessionId) },
                onRemoteRestUp = { phoneActionMessenger.sendRestUp(remoteSessionState.value?.sessionId) },
                onRemoteRestDown = { phoneActionMessenger.sendRestDown(remoteSessionState.value?.sessionId) },
                onRemoteAddSet = { phoneActionMessenger.sendAddSet(remoteSessionState.value?.sessionId) },
                onRemoteRemoveSet = { phoneActionMessenger.sendRemoveSet(remoteSessionState.value?.sessionId) }
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
        when (messageEvent.path) {
            WatchProtocol.PATH_SESSION_STATE -> {
                val state = WatchProtocol.decodeSessionState(messageEvent.data) ?: return
                runOnUiThread {
                    remoteSessionState.value = state
                    remoteSessionFinished.value = false
                }
            }
            WatchProtocol.PATH_FINISH_SESSION -> {
                runOnUiThread {
                    remoteSessionFinished.value = true
                }
            }
        }
    }
}
