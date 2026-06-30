package com.coachapp.core

object WatchProtocol {
    const val PATH_SESSION_STATE = "/coachapp/session/state"
    const val PATH_SET_DONE = "/coachapp/session/set_done"
    const val PATH_WEIGHT_UP = "/coachapp/session/weight_up"
    const val PATH_WEIGHT_DOWN = "/coachapp/session/weight_down"
    const val PATH_FINISH_SESSION = "/coachapp/session/finish"

    const val KEY_SESSION_ID = "session_id"
    const val KEY_EXERCISE_ID = "exercise_id"
    const val KEY_SET_INDEX = "set_index"
    const val KEY_WEIGHT_KG = "weight_kg"
    const val KEY_REPS = "reps"
}

data class WatchSessionState(
    val sessionId: String,
    val exerciseName: String,
    val setIndex: Int,
    val setCount: Int,
    val repsMin: Int,
    val repsMax: Int,
    val weightKg: Double,
    val restSeconds: Int
)
