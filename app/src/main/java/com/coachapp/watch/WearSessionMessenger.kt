package com.coachapp.watch

import android.content.Context
import com.coachapp.core.WatchProtocol
import com.coachapp.core.WatchSessionState
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Wearable

class WearSessionMessenger(context: Context) {
    private val messageClient = Wearable.getMessageClient(context.applicationContext)
    private val nodeClient = Wearable.getNodeClient(context.applicationContext)

    fun sendSessionStateToConnectedNodes(state: WatchSessionState) {
        val payload = WatchProtocol.encodeSessionState(state)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, WatchProtocol.PATH_SESSION_STATE, payload)
            }
        }
    }

    fun sendSessionFinishedToConnectedNodes(sessionId: String) {
        val payload = sessionId.encodeToByteArray()
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, WatchProtocol.PATH_FINISH_SESSION, payload)
            }
        }
    }

    fun sendSetCompleted(nodeId: String, sessionId: String): Task<Int> {
        val payload = sessionId.encodeToByteArray()
        return messageClient.sendMessage(nodeId, WatchProtocol.PATH_SET_DONE, payload)
    }
}
