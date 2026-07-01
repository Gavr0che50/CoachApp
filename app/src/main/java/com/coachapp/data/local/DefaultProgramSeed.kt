package com.coachapp.data.local

import com.coachapp.core.DefaultProgram
import com.coachapp.core.WorkoutDay

object DefaultProgramSeed {
    fun exercises(days: List<WorkoutDay> = DefaultProgram.weeklyPlan): List<ExerciseEntity> {
        return days
            .flatMap { day -> day.blocks.map { block -> block.exercise } }
            .distinctBy { exercise -> exercise.id }
            .map { exercise ->
                ExerciseEntity(
                    id = exercise.id,
                    name = exercise.name,
                    muscles = exercise.muscles,
                    equipment = exercise.equipment,
                    illustrationAsset = exercise.illustrationAsset,
                    videoAsset = exercise.videoAsset
                )
            }
    }

    fun workoutPlans(days: List<WorkoutDay> = DefaultProgram.weeklyPlan): List<WorkoutPlanEntity> {
        return days.map { day ->
            WorkoutPlanEntity(
                id = day.id,
                dayOfWeekIso = day.dayOfWeekIso,
                title = day.title,
                targetMinutes = day.targetMinutes
            )
        }
    }

    fun workoutPlanExercises(days: List<WorkoutDay> = DefaultProgram.weeklyPlan): List<WorkoutPlanExerciseEntity> {
        return days.flatMap { day ->
            day.blocks.mapIndexed { position, block ->
                val firstSet = block.sets.first()
                WorkoutPlanExerciseEntity(
                    planDayId = day.id,
                    position = position,
                    exerciseId = block.exercise.id,
                    setCount = block.sets.size,
                    repsMin = firstSet.repsMin,
                    repsMax = firstSet.repsMax,
                    weightKg = firstSet.weightKg,
                    restSeconds = firstSet.restSeconds,
                    rirTarget = firstSet.rirTarget,
                    notes = block.notes
                )
            }
        }
    }
}
