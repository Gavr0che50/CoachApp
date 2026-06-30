package com.coachapp.core

object DefaultProgram {
    val weeklyPlan: List<WorkoutDay> = listOf(
        WorkoutDay(
            id = "day-1-push",
            dayOfWeekIso = 1,
            title = "Push haut du corps",
            targetMinutes = 30,
            blocks = listOf(
                block("bench-press", "Developpe couche", listOf("pectoraux", "triceps"), "barre", 3, 8, 10, 40.0, 90),
                block("incline-dumbbell-press", "Developpe incline halteres", listOf("pectoraux", "epaules"), "halteres", 3, 8, 12, 18.0, 75),
                block("cable-triceps", "Extension triceps poulie", listOf("triceps"), "poulie", 3, 10, 15, 20.0, 60)
            )
        ),
        WorkoutDay(
            id = "day-2-pull",
            dayOfWeekIso = 2,
            title = "Pull dos et biceps",
            targetMinutes = 30,
            blocks = listOf(
                block("lat-pulldown", "Tirage vertical", listOf("dos", "biceps"), "machine", 3, 8, 12, 45.0, 90),
                block("seated-row", "Rowing assis", listOf("dos"), "machine", 3, 8, 12, 45.0, 75),
                block("dumbbell-curl", "Curl halteres", listOf("biceps"), "halteres", 3, 10, 15, 12.0, 60)
            )
        ),
        WorkoutDay(
            id = "day-3-legs",
            dayOfWeekIso = 3,
            title = "Jambes",
            targetMinutes = 30,
            blocks = listOf(
                block("squat", "Squat", listOf("quadriceps", "fessiers"), "barre", 3, 6, 10, 50.0, 120),
                block("romanian-deadlift", "Souleve de terre roumain", listOf("ischios", "fessiers"), "barre", 3, 8, 10, 45.0, 90),
                block("standing-calf-raise", "Mollets debout", listOf("mollets"), "machine", 3, 12, 20, 35.0, 60)
            )
        ),
        WorkoutDay(
            id = "day-4-upper",
            dayOfWeekIso = 4,
            title = "Haut du corps mixte",
            targetMinutes = 30,
            blocks = listOf(
                block("overhead-press", "Developpe militaire", listOf("epaules", "triceps"), "barre", 3, 6, 10, 30.0, 90),
                block("chest-supported-row", "Rowing buste supporte", listOf("dos"), "halteres", 3, 8, 12, 18.0, 75),
                block("plank", "Gainage", listOf("abdos"), "poids du corps", 3, 30, 45, 0.0, 45)
            )
        ),
        WorkoutDay(
            id = "day-5-full-body",
            dayOfWeekIso = 5,
            title = "Full body rapide",
            targetMinutes = 30,
            blocks = listOf(
                block("leg-press", "Presse a cuisses", listOf("quadriceps", "fessiers"), "machine", 3, 10, 12, 90.0, 90),
                block("push-up", "Pompes", listOf("pectoraux", "triceps"), "poids du corps", 3, 8, 20, 0.0, 60),
                block("cable-face-pull", "Face pull", listOf("epaules", "haut du dos"), "poulie", 3, 12, 20, 15.0, 60)
            )
        )
    )

    fun dayForIsoDay(dayOfWeekIso: Int): WorkoutDay =
        weeklyPlan.firstOrNull { it.dayOfWeekIso == dayOfWeekIso } ?: weeklyPlan.first()

    private fun block(
        id: String,
        name: String,
        muscles: List<String>,
        equipment: String,
        sets: Int,
        repsMin: Int,
        repsMax: Int,
        weightKg: Double,
        restSeconds: Int
    ): ExerciseBlock {
        return ExerciseBlock(
            exercise = Exercise(
                id = id,
                name = name,
                muscles = muscles,
                equipment = equipment,
                illustrationAsset = "assets/exercises/$id.svg",
                videoAsset = "assets/exercises/$id.mp4"
            ),
            sets = (1..sets).map { index ->
                SetTarget(
                    index = index,
                    repsMin = repsMin,
                    repsMax = repsMax,
                    weightKg = weightKg,
                    restSeconds = restSeconds
                )
            }
        )
    }
}
