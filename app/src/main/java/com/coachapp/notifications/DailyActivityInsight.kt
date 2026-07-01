package com.coachapp.notifications

import java.time.LocalDate
import kotlin.math.roundToInt

const val DailyInsightBaselineDays = 7
const val DailyInsightThresholdRatio = 0.10

data class DailyActivityContribution(
    val date: LocalDate,
    val coachCalories: Double,
    val healthCalories: Double?,
    val completedSets: Int,
    val steps: Long?
) {
    val knownCalories: Double
        get() = coachCalories + (healthCalories ?: 0.0)

    val hasAnySource: Boolean
        get() = coachCalories > 0.0 || healthCalories != null || steps != null
}

data class DailyActivityInsight(
    val title: String,
    val message: String,
    val longMessage: String
)

fun buildDailyActivityInsight(
    days: List<DailyActivityContribution>,
    referenceDate: LocalDate,
    thresholdRatio: Double = DailyInsightThresholdRatio
): DailyActivityInsight? {
    val yesterday = days.firstOrNull { it.date == referenceDate } ?: return null
    if (!yesterday.hasAnySource) return null

    val baseline = days
        .filter { it.date.isBefore(referenceDate) && it.hasAnySource }
        .sortedByDescending { it.date }
        .take(DailyInsightBaselineDays)

    val baselineAverage = baseline
        .takeIf { it.isNotEmpty() }
        ?.map { it.knownCalories }
        ?.average()

    val label = classifyDailyActivity(
        value = yesterday.knownCalories,
        baselineAverage = baselineAverage,
        thresholdRatio = thresholdRatio
    )
    val yesterdayCalories = yesterday.knownCalories.roundToInt()
    val baselineText = baselineAverage?.roundToInt()?.let { " vs moyenne 7j $it kcal" }.orEmpty()
    val healthText = when {
        yesterday.healthCalories != null && yesterday.steps != null ->
            "Sante: ${yesterday.healthCalories.roundToInt()} kcal actives, ${yesterday.steps} pas."
        yesterday.healthCalories != null ->
            "Sante: ${yesterday.healthCalories.roundToInt()} kcal actives."
        else -> "Health Connect absent ou non autorise."
    }
    val coachText = "CoachApp: ${yesterday.coachCalories.roundToInt()} kcal, ${yesterday.completedSets} series."

    return DailyActivityInsight(
        title = "Bilan d'hier: $label",
        message = "$yesterdayCalories kcal estimees$baselineText.",
        longMessage = "$yesterdayCalories kcal estimees$baselineText. $coachText $healthText"
    )
}

fun classifyDailyActivity(
    value: Double,
    baselineAverage: Double?,
    thresholdRatio: Double = DailyInsightThresholdRatio
): String {
    if (baselineAverage == null || baselineAverage <= 0.0) return "plutot constant"
    val upper = baselineAverage * (1.0 + thresholdRatio)
    val lower = baselineAverage * (1.0 - thresholdRatio)
    return when {
        value > upper -> "plutot actif"
        value < lower -> "plutot non actif"
        else -> "plutot constant"
    }
}
