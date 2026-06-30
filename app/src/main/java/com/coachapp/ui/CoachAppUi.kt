package com.coachapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coachapp.core.DefaultProgram
import com.coachapp.core.ExerciseBlock
import com.coachapp.core.Science
import java.time.LocalDate

@Composable
fun CoachAppUi() {
    val today = remember { DefaultProgram.dayForIsoDay(LocalDate.now().dayOfWeek.value) }
    var currentBlockIndex by remember { mutableIntStateOf(0) }
    var currentSetIndex by remember { mutableIntStateOf(0) }
    var completedSets by remember { mutableIntStateOf(0) }
    var sessionDone by remember { mutableStateOf(false) }

    val totalSets = today.blocks.sumOf { it.sets.size }
    val currentBlock = today.blocks.getOrNull(currentBlockIndex)
    val currentSet = currentBlock?.sets?.getOrNull(currentSetIndex)
    val estimatedCalories = Science.resistanceTrainingCalories(
        weightKg = 80.0,
        minutes = today.targetMinutes,
        intensityMet = 5.0
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("CoachApp", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(today.title, style = MaterialTheme.typography.titleLarge)
                Text("Objectif : ${today.targetMinutes} min · $completedSets / $totalSets series")

                if (sessionDone) {
                    SessionSummary(completedSets, totalSets, estimatedCalories)
                } else if (currentBlock != null && currentSet != null) {
                    CurrentExerciseCard(currentBlock, currentSet.index, currentSet.repsMin, currentSet.repsMax, currentSet.weightKg, currentSet.restSeconds)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = {
                            completedSets += 1
                            val nextSetIndex = currentSetIndex + 1
                            if (nextSetIndex < currentBlock.sets.size) {
                                currentSetIndex = nextSetIndex
                            } else {
                                val nextBlockIndex = currentBlockIndex + 1
                                if (nextBlockIndex < today.blocks.size) {
                                    currentBlockIndex = nextBlockIndex
                                    currentSetIndex = 0
                                } else {
                                    sessionDone = true
                                }
                            }
                        }) {
                            Text("Valider la serie")
                        }
                        OutlinedButton(onClick = { sessionDone = true }) {
                            Text("Terminer")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Programme du jour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                today.blocks.forEach { block ->
                    PlannedExerciseCard(block)
                }
            }
        }
    }
}

@Composable
private fun CurrentExerciseCard(
    block: ExerciseBlock,
    setIndex: Int,
    repsMin: Int,
    repsMax: Int,
    weightKg: Double,
    restSeconds: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Exercice en cours", style = MaterialTheme.typography.labelLarge)
            Text(block.exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Serie $setIndex · $repsMin-$repsMax reps · ${weightKg} kg")
            Text("Repos : ${restSeconds}s · Media : ${block.exercise.illustrationAsset}")
        }
    }
}

@Composable
private fun PlannedExerciseCard(block: ExerciseBlock) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(block.exercise.name, fontWeight = FontWeight.Bold)
            Text("${block.sets.size} series · ${block.sets.first().repsMin}-${block.sets.first().repsMax} reps · ${block.sets.first().restSeconds}s repos")
            Text("Groupes : ${block.exercise.muscles.joinToString()}")
        }
    }
}

@Composable
private fun SessionSummary(completedSets: Int, totalSets: Int, estimatedCalories: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Seance terminee", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Series validees : $completedSets / $totalSets")
            Text("Calories estimees : ${estimatedCalories.toInt()} kcal")
            Text("Prochaine etape : comparer avec la derniere seance identique et detecter les records.")
        }
    }
}
