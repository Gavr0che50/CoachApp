package com.coachapp.wear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.coachapp.core.DefaultProgram
import java.time.LocalDate

@Composable
fun WatchWorkoutApp() {
    val workoutDay = remember { DefaultProgram.dayForIsoDay(LocalDate.now().dayOfWeek.value) }
    var blockIndex by remember { mutableIntStateOf(0) }
    var setIndex by remember { mutableIntStateOf(0) }
    var completedSets by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }

    val totalSets = workoutDay.blocks.sumOf { it.sets.size }
    val block = workoutDay.blocks.getOrNull(blockIndex)
    val set = block?.sets?.getOrNull(setIndex)

    MaterialTheme {
        Scaffold {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (done || block == null || set == null) {
                    Text("Fini", style = MaterialTheme.typography.title2)
                    Text("$completedSets / $totalSets series", textAlign = TextAlign.Center)
                } else {
                    Text(block.exercise.name, style = MaterialTheme.typography.title2, textAlign = TextAlign.Center)
                    Text("Serie ${set.index}/${block.sets.size}")
                    Text("${set.repsMin}-${set.repsMax} reps")
                    Text("${set.weightKg} kg - repos ${set.restSeconds}s", textAlign = TextAlign.Center)
                    Button(onClick = {
                        completedSets += 1
                        val nextSet = setIndex + 1
                        if (nextSet < block.sets.size) {
                            setIndex = nextSet
                        } else {
                            val nextBlock = blockIndex + 1
                            if (nextBlock < workoutDay.blocks.size) {
                                blockIndex = nextBlock
                                setIndex = 0
                            } else {
                                done = true
                            }
                        }
                    }) {
                        Text("Valider")
                    }
                    Button(onClick = { done = true }) {
                        Text("Terminer")
                    }
                }
            }
        }
    }
}
