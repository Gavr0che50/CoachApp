package com.coachapp.wear

import android.content.Context
import com.coachapp.core.WatchProtocol
import com.google.android.gms.wearable.Wearable

class PhoneActionMessenger(context: Context) {
    private val messageClient = Wearable.getMessageClient(context.applicationContext)
    private val nodeClient = Wearable.getNodeClient(context.applicationContext)

    fun sendSetDone(sessionId: String?) {
        sendAction(WatchProtocol.PATH_SET_DONE, sessionId)
    }

    fun sendFinishSession(sessionId: String?) {
        sendAction(WatchProtocol.PATH_FINISH_SESSION, sessionId)
    }

    fun sendWeightUp(sessionId: String?) {
        sendAction(WatchProtocol.PATH_WEIGHT_UP, sessionId)
    }

    fun sendWeightDown(sessionId: String?) {
        sendAction(WatchProtocol.PATH_WEIGHT_DOWN, sessionId)
    }

    fun sendRepsUp(sessionId: String?) {
        sendAction(WatchProtocol.PATH_REPS_UP, sessionId)
    }

    fun sendRepsDown(sessionId: String?) {
        sendAction(WatchProtocol.PATH_REPS_DOWN, sessionId)
    }

    fun sendRestUp(sessionId: String?) {
        sendAction(WatchProtocol.PATH_REST_UP, sessionId)
    }

    fun sendRestDown(sessionId: String?) {
        sendAction(WatchProtocol.PATH_REST_DOWN, sessionId)
    }

    fun sendAddSet(sessionId: String?) {
        sendAction(WatchProtocol.PATH_ADD_SET, sessionId)
    }

    fun sendRemoveSet(sessionId: String?) {
        sendAction(WatchProtocol.PATH_REMOVE_SET, sessionId)
    }

    private fun sendAction(path: String, sessionId: String?) {
        val payload = sessionId.orEmpty().encodeToByteArray()
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, path, payload)
            }
        }
    }
}
