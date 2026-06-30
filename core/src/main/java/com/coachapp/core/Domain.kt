package com.coachapp.core

data class Exercise(
    val id: String,
    val name: String,
    val muscles: List<String>,
    val equipment: String,
    val illustrationAsset: String,
    val videoAsset: String? = null
)

data class SetTarget(
    val index: Int,
    val repsMin: Int,
    val repsMax: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val rirTarget: Int = 2
)

data class ExerciseBlock(
    val exercise: Exercise,
    val sets: List<SetTarget>,
    val notes: String = ""
)

data class WorkoutDay(
    val id: String,
    val dayOfWeekIso: Int,
    val title: String,
    val targetMinutes: Int,
    val blocks: List<ExerciseBlock>
)
