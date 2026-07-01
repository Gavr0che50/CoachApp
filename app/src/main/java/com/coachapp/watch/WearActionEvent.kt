package com.coachapp.watch

data class WearActionEvent(
    val sequence: Int,
    val type: WearActionType,
    val sessionId: String?
)

enum class WearActionType {
    SetDone,
    FinishSession,
    WeightUp,
    WeightDown,
    RepsUp,
    RepsDown,
    RestUp,
    RestDown,
    AddSet,
    RemoveSet
}
