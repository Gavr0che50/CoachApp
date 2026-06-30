package com.coachapp.watch

import android.content.Context
import com.coachapp.core.WatchProtocol
import com.google.android.gms.wearable.Wearable

class WearSessionMessenger(private val context: Context) {
    private val messageClient = Wearable.getMessageClient(context)

    suspend fun sendSetCompleted(nodeId: String, sessionId: String) {
        val payload = sessionId.encodeToByteArray()
        messageClient.sendMessage(nodeId, WatchProtocol.PATH_SET_DONE, payload)
    }
}
