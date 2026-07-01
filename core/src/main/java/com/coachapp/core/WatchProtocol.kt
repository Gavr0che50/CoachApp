package com.coachapp.core

object WatchProtocol {
    const val PATH_SESSION_STATE = "/coachapp/session/state"
    const val PATH_SET_DONE = "/coachapp/session/set_done"
    const val PATH_WEIGHT_UP = "/coachapp/session/weight_up"
    const val PATH_WEIGHT_DOWN = "/coachapp/session/weight_down"
    const val PATH_REPS_UP = "/coachapp/session/reps_up"
    const val PATH_REPS_DOWN = "/coachapp/session/reps_down"
    const val PATH_REST_UP = "/coachapp/session/rest_up"
    const val PATH_REST_DOWN = "/coachapp/session/rest_down"
    const val PATH_ADD_SET = "/coachapp/session/add_set"
    const val PATH_REMOVE_SET = "/coachapp/session/remove_set"
    const val PATH_FINISH_SESSION = "/coachapp/session/finish"

    const val KEY_SESSION_ID = "session_id"
    const val KEY_EXERCISE_ID = "exercise_id"
    const val KEY_SET_INDEX = "set_index"
    const val KEY_WEIGHT_KG = "weight_kg"
    const val KEY_REPS = "reps"

    private const val FIELD_SEPARATOR = "\u001F"

    fun encodeSessionState(state: WatchSessionState): ByteArray {
        return listOf(
            state.sessionId,
            state.exerciseName,
            state.setIndex.toString(),
            state.setCount.toString(),
            state.repsMin.toString(),
            state.repsMax.toString(),
            state.reps.toString(),
            state.weightKg.toString(),
            state.restSeconds.toString(),
            state.restRemainingSeconds.toString(),
            state.isResting.toString()
        ).joinToString(FIELD_SEPARATOR).encodeToByteArray()
    }

    fun decodeSessionState(payload: ByteArray): WatchSessionState? {
        val parts = payload.decodeToString().split(FIELD_SEPARATOR)
        if (parts.size < 8) {
            return null
        }

        val hasExplicitReps = parts.size >= 11
        val repsIndex = if (hasExplicitReps) 6 else 5
        val weightIndex = if (hasExplicitReps) 7 else 6
        val restSecondsIndex = if (hasExplicitReps) 8 else 7
        val restRemainingIndex = if (hasExplicitReps) 9 else 8
        val isRestingIndex = if (hasExplicitReps) 10 else 9

        return WatchSessionState(
            sessionId = parts[0],
            exerciseName = parts[1],
            setIndex = parts[2].toIntOrNull() ?: return null,
            setCount = parts[3].toIntOrNull() ?: return null,
            repsMin = parts[4].toIntOrNull() ?: return null,
            repsMax = parts[5].toIntOrNull() ?: return null,
            reps = parts[repsIndex].toIntOrNull() ?: return null,
            weightKg = parts[weightIndex].toDoubleOrNull() ?: return null,
            restSeconds = parts[restSecondsIndex].toIntOrNull() ?: return null,
            restRemainingSeconds = parts.getOrNull(restRemainingIndex)?.toIntOrNull() ?: 0,
            isResting = parts.getOrNull(isRestingIndex)?.toBooleanStrictOrNull() ?: false
        )
    }
}

data class WatchSessionState(
    val sessionId: String,
    val exerciseName: String,
    val setIndex: Int,
    val setCount: Int,
    val repsMin: Int,
    val repsMax: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val restRemainingSeconds: Int = 0,
    val isResting: Boolean = false
)
