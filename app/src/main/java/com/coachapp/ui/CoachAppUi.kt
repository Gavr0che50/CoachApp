package com.coachapp.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.coachapp.core.ExerciseBlock
import com.coachapp.core.ScheduledWorkout
import com.coachapp.core.Science
import com.coachapp.core.SetTarget
import com.coachapp.core.TrainingSchedule
import com.coachapp.core.WatchSessionState
import com.coachapp.core.WorkoutDay
import com.coachapp.data.CompletedSetRecord
import com.coachapp.data.DailyCoachActivitySnapshot
import com.coachapp.data.DefaultTargetSessionMinutes
import com.coachapp.data.DefaultTrainingDaysIso
import com.coachapp.data.SessionSummarySnapshot
import com.coachapp.data.UserProfileInput
import com.coachapp.data.UserProfileSnapshot
import com.coachapp.health.DailyHealthActivityResult
import com.coachapp.health.DailyHealthActivitySnapshot
import com.coachapp.health.HealthSyncResult
import com.coachapp.watch.WearActionEvent
import com.coachapp.watch.WearActionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val PhoneColors = darkColorScheme(
    primary = Color(0xFFB78CFF),
    onPrimary = Color(0xFF22113F),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF09211F),
    background = Color(0xFF07070B),
    onBackground = Color(0xFFF3EEFF),
    surface = Color(0xFF111018),
    onSurface = Color(0xFFF3EEFF),
    surfaceVariant = Color(0xFF1C1827),
    onSurfaceVariant = Color(0xFFD7CBEA),
    outline = Color(0xFF6F627F)
)

private val CardColor = Color(0xFF171321)
private val ElevatedCardColor = Color(0xFF211936)
private val ImageFallbackColor = Color(0xFF231D31)
private val CompletedCardColor = Color(0xFF24232B)
private val SuccessGreen = Color(0xFF4ADE80)
private val TrainingDayLabels = listOf(
    1 to "Lun",
    2 to "Mar",
    3 to "Mer",
    4 to "Jeu",
    5 to "Ven",
    6 to "Sam",
    7 to "Dim"
)
private const val WeightStepKg = 2.4
private const val RestStepSeconds = 30
private const val MaxRestSeconds = 180

private data class SessionSetTarget(
    val index: Int,
    val repsMin: Int,
    val repsMax: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int,
    val rirTarget: Int
)

@Composable
fun CoachAppUi(
    latestSessionSummary: suspend () -> SessionSummarySnapshot? = { null },
    dailyCoachActivity: suspend () -> List<DailyCoachActivitySnapshot> = { emptyList() },
    dailyHealthActivity: suspend () -> DailyHealthActivityResult = {
        DailyHealthActivityResult(emptyList(), "Samsung Health indisponible")
    },
    wearActionEvent: WearActionEvent? = null,
    onRequestHealthConnectPermissions: () -> Unit = {},
    onSyncHealthConnect: suspend () -> HealthSyncResult = {
        HealthSyncResult(
            bodySnapshot = null,
            restingHeartRateBpm = null,
            activeEnergyKcalToday = null,
            missingNotes = listOf("Samsung Health indisponible")
        )
    },
    userProfile: suspend () -> UserProfileSnapshot? = { null },
    onSaveUserProfile: suspend (UserProfileInput) -> UserProfileSnapshot = { input ->
        UserProfileSnapshot(
            heightCm = input.heightCm,
            weightKg = input.weightKg,
            ageYears = input.ageYears,
            trainingDaysIso = input.trainingDaysIso,
            targetSessionMinutes = input.targetSessionMinutes ?: DefaultTargetSessionMinutes,
            updatedAtEpochMillis = System.currentTimeMillis()
        )
    },
    onSessionStateChanged: (WatchSessionState) -> Unit = {},
    onSessionFinished: suspend (WorkoutDay, Long, Long, List<CompletedSetRecord>, Double) -> SessionSummarySnapshot? = { _, _, _, _, _ -> null },
) {
    MaterialTheme(colorScheme = PhoneColors) {
        CoachAppContent(
            latestSessionSummary = latestSessionSummary,
            dailyCoachActivity = dailyCoachActivity,
            dailyHealthActivity = dailyHealthActivity,
            wearActionEvent = wearActionEvent,
            onRequestHealthConnectPermissions = onRequestHealthConnectPermissions,
            onSyncHealthConnect = onSyncHealthConnect,
            userProfile = userProfile,
            onSaveUserProfile = onSaveUserProfile,
            onSessionStateChanged = onSessionStateChanged,
            onSessionFinished = onSessionFinished
        )
    }
}

@Composable
private fun CoachAppContent(
    latestSessionSummary: suspend () -> SessionSummarySnapshot?,
    dailyCoachActivity: suspend () -> List<DailyCoachActivitySnapshot>,
    dailyHealthActivity: suspend () -> DailyHealthActivityResult,
    wearActionEvent: WearActionEvent?,
    onRequestHealthConnectPermissions: () -> Unit,
    onSyncHealthConnect: suspend () -> HealthSyncResult,
    userProfile: suspend () -> UserProfileSnapshot?,
    onSaveUserProfile: suspend (UserProfileInput) -> UserProfileSnapshot,
    onSessionStateChanged: (WatchSessionState) -> Unit,
    onSessionFinished: suspend (WorkoutDay, Long, Long, List<CompletedSetRecord>, Double) -> SessionSummarySnapshot?
) {
    val todayDate = LocalDate.now()
    var profile by remember { mutableStateOf<UserProfileSnapshot?>(null) }
    var profileStatus by remember { mutableStateOf<String?>(null) }
    val trainingDays = profile?.trainingDaysIso ?: DefaultTrainingDaysIso
    val targetSessionMinutes = profile?.targetSessionMinutes ?: DefaultTargetSessionMinutes
    val upcomingWorkouts = remember(trainingDays, targetSessionMinutes, todayDate) {
        TrainingSchedule.nextWorkouts(
            from = todayDate,
            trainingDaysIso = trainingDays,
            targetMinutes = targetSessionMinutes,
            count = 3
        )
    }
    val nextScheduledWorkout = upcomingWorkouts.first()
    val today = nextScheduledWorkout.workoutDay
    val scope = rememberCoroutineScope()
    var selectedPage by remember { mutableStateOf(CoachPage.Session) }
    val sessionSetsByBlock = remember(today) {
        today.blocks.map { block ->
            mutableStateListOf<SessionSetTarget>().apply {
                block.sets.forEach { set -> add(set.toSessionSetTarget()) }
            }
        }
    }
    var currentBlockIndex by remember { mutableIntStateOf(0) }
    var currentSetIndex by remember { mutableIntStateOf(0) }
    var completedSets by remember { mutableIntStateOf(0) }
    val completedSetRecords = remember(today) { mutableStateListOf<CompletedSetRecord>() }
    val completedSetsByBlock = remember(today) {
        mutableStateListOf<Int>().apply {
            repeat(today.blocks.size) { add(0) }
        }
    }
    var sessionDone by remember { mutableStateOf(false) }
    var startedAtEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var latestSummary by remember { mutableStateOf<SessionSummarySnapshot?>(null) }
    var persistenceStatus by remember { mutableStateOf<String?>(null) }
    var healthSyncResult by remember { mutableStateOf<HealthSyncResult?>(null) }
    var healthSyncStatus by remember { mutableStateOf<String?>(null) }
    var dailyActivity by remember { mutableStateOf<List<DailyCoachActivitySnapshot>>(emptyList()) }
    var dailyActivityStatus by remember { mutableStateOf<String?>(null) }
    var dailyHealthSnapshots by remember { mutableStateOf<List<DailyHealthActivitySnapshot>>(emptyList()) }
    var dailyHealthActivityStatus by remember { mutableStateOf<String?>(null) }
    var restRemainingSeconds by remember { mutableIntStateOf(0) }

    val totalSets = sessionSetsByBlock.sumOf { it.size }
    val currentBlock = today.blocks.getOrNull(currentBlockIndex)
    val currentSetTargets = sessionSetsByBlock.getOrNull(currentBlockIndex).orEmpty()
    val currentSet = currentSetTargets.getOrNull(currentSetIndex)
    val currentWeightKg = currentSet?.weightKg ?: 0.0
    val isResting = restRemainingSeconds > 0 && !sessionDone
    val estimatedCalories = Science.resistanceTrainingCalories(
        weightKg = 80.0,
        minutes = today.targetMinutes
    )

    suspend fun refreshDailyActivity() {
        dailyActivityStatus = "Chargement activite"
        dailyHealthActivityStatus = "Chargement Samsung Health"
        runCatching {
            dailyCoachActivity()
        }.onSuccess { snapshots ->
            dailyActivity = snapshots
            dailyActivityStatus = if (snapshots.isEmpty()) {
                "Aucune seance sauvegardee sur la periode."
            } else {
                null
            }
        }.onFailure {
            dailyActivity = emptyList()
            dailyActivityStatus = "Historique CoachApp indisponible"
        }
        runCatching {
            dailyHealthActivity()
        }.onSuccess { result ->
            dailyHealthSnapshots = result.snapshots
            dailyHealthActivityStatus = result.missingNote
                ?: "Samsung Health: energie active et pas disponibles"
        }.onFailure {
            dailyHealthSnapshots = emptyList()
            dailyHealthActivityStatus = "Samsung Health indisponible"
        }
    }

    LaunchedEffect(Unit) {
        latestSummary = latestSessionSummary()
        runCatching { userProfile() }
            .onSuccess { profile = it }
            .onFailure { profileStatus = "Profil indisponible" }
        refreshDailyActivity()
    }

    fun finishSession(finalCompletedSets: Int = completedSets) {
        if (sessionDone) {
            return
        }

        val endedAtEpochMillis = System.currentTimeMillis()
        sessionDone = true
        restRemainingSeconds = 0
        persistenceStatus = "Sauvegarde en cours"
        scope.launch {
            runCatching {
                onSessionFinished(
                    today,
                    startedAtEpochMillis,
                    endedAtEpochMillis,
                    completedSetRecords.toList(),
                    estimatedCalories
                )
            }.onSuccess { savedSummary ->
                latestSummary = savedSummary ?: latestSessionSummary()
                persistenceStatus = "Seance sauvegardee"
                refreshDailyActivity()
            }.onFailure {
                persistenceStatus = "Sauvegarde impossible"
            }
        }
    }

    fun focusBlock(index: Int) {
        today.blocks.getOrNull(index) ?: return
        val targetSets = sessionSetsByBlock.getOrNull(index) ?: return
        val completedForBlock = completedSetsByBlock.getOrNull(index) ?: return
        if (completedForBlock >= targetSets.size) {
            return
        }

        currentBlockIndex = index
        currentSetIndex = completedForBlock.coerceAtMost(targetSets.lastIndex.coerceAtLeast(0))
        restRemainingSeconds = 0
    }

    fun completeCurrentSet(source: String) {
        val blockIndex = currentBlockIndex
        val block = currentBlock ?: return
        val completedSet = currentSet ?: return
        val targetSets = sessionSetsByBlock.getOrNull(blockIndex) ?: return
        val completedForBlock = completedSetsByBlock.getOrNull(blockIndex) ?: return
        if (completedForBlock >= targetSets.size) {
            return
        }

        completedSetRecords.add(
            CompletedSetRecord(
                exerciseId = block.exercise.id,
                exerciseName = block.exercise.name,
                setIndex = completedSet.index,
                plannedRepsMin = completedSet.repsMin,
                plannedRepsMax = completedSet.repsMax,
                performedReps = completedSet.reps,
                weightKg = completedSet.weightKg,
                restSeconds = completedSet.restSeconds,
                completedAtEpochMillis = System.currentTimeMillis(),
                rir = completedSet.rirTarget,
                source = source
            )
        )

        val nextCompletedForBlock = (completedForBlock + 1).coerceAtMost(targetSets.size)
        completedSetsByBlock[blockIndex] = nextCompletedForBlock
        val nextCompletedSets = completedSets + 1
        completedSets = nextCompletedSets

        if (nextCompletedForBlock < targetSets.size) {
            currentSetIndex = nextCompletedForBlock
            restRemainingSeconds = completedSet.restSeconds
            return
        }

        val nextBlockIndex = today.blocks.indices.firstOrNull { index ->
            index != blockIndex && completedSetsByBlock[index] < sessionSetsByBlock[index].size
        }
        if (nextBlockIndex != null) {
            currentBlockIndex = nextBlockIndex
            currentSetIndex = completedSetsByBlock[nextBlockIndex]
            restRemainingSeconds = completedSet.restSeconds
            return
        }

        finishSession(nextCompletedSets)
    }

    fun updateCurrentSet(repsDelta: Int = 0, weightDeltaKg: Double = 0.0, restDeltaSeconds: Int = 0) {
        val targetSets = sessionSetsByBlock.getOrNull(currentBlockIndex) ?: return
        val set = targetSets.getOrNull(currentSetIndex) ?: return
        val nextRestSeconds = (set.restSeconds + restDeltaSeconds).coerceIn(0, MaxRestSeconds)
        val nextSet = set.copy(
            reps = (set.reps + repsDelta).coerceAtLeast(1),
            weightKg = (set.weightKg + weightDeltaKg).coerceAtLeast(0.0),
            restSeconds = nextRestSeconds
        )
        targetSets[currentSetIndex] = nextSet

        if (restDeltaSeconds != 0 && restRemainingSeconds > 0) {
            val restDeltaApplied = nextRestSeconds - set.restSeconds
            restRemainingSeconds = (restRemainingSeconds + restDeltaApplied).coerceIn(0, nextRestSeconds)
        }
    }

    fun addSetToCurrentBlock() {
        val block = currentBlock ?: return
        val targetSets = sessionSetsByBlock.getOrNull(currentBlockIndex) ?: return
        val nextIndex = targetSets.size + 1
        val lastCompleted = completedSetRecords.lastOrNull { it.exerciseId == block.exercise.id }
        val pattern = lastCompleted?.toSessionSetTarget(nextIndex)
            ?: targetSets.lastOrNull()?.copy(index = nextIndex)
            ?: return
        targetSets.add(pattern)
    }

    fun removeSetFromCurrentBlock() {
        val targetSets = sessionSetsByBlock.getOrNull(currentBlockIndex) ?: return
        val completedForBlock = completedSetsByBlock.getOrNull(currentBlockIndex) ?: return
        if (targetSets.size <= 1 || completedForBlock >= targetSets.size) {
            return
        }

        targetSets.removeAt(targetSets.lastIndex)
        if (currentSetIndex >= targetSets.size) {
            currentSetIndex = targetSets.lastIndex
        }
    }

    LaunchedEffect(restRemainingSeconds, sessionDone) {
        if (restRemainingSeconds > 0 && !sessionDone) {
            delay(1000)
            restRemainingSeconds = (restRemainingSeconds - 1).coerceAtLeast(0)
        }
    }

    LaunchedEffect(wearActionEvent?.sequence) {
        when (wearActionEvent?.type) {
            WearActionType.SetDone -> if (!isResting) completeCurrentSet(source = "wear")
            WearActionType.FinishSession -> finishSession()
            WearActionType.WeightUp -> updateCurrentSet(weightDeltaKg = WeightStepKg)
            WearActionType.WeightDown -> updateCurrentSet(weightDeltaKg = -WeightStepKg)
            WearActionType.RepsUp -> updateCurrentSet(repsDelta = 1)
            WearActionType.RepsDown -> updateCurrentSet(repsDelta = -1)
            WearActionType.RestUp -> updateCurrentSet(restDeltaSeconds = RestStepSeconds)
            WearActionType.RestDown -> updateCurrentSet(restDeltaSeconds = -RestStepSeconds)
            WearActionType.AddSet -> addSetToCurrentBlock()
            WearActionType.RemoveSet -> removeSetFromCurrentBlock()
            null -> Unit
        }
    }

    LaunchedEffect(
        currentBlockIndex,
        currentSetIndex,
        currentWeightKg,
        currentSet?.reps,
        currentSet?.restSeconds,
        currentSetTargets.size,
        restRemainingSeconds,
        sessionDone,
        startedAtEpochMillis
    ) {
        val block = currentBlock
        val set = currentSet
        if (!sessionDone && block != null && set != null) {
            onSessionStateChanged(
                WatchSessionState(
                    sessionId = "local-$startedAtEpochMillis",
                    exerciseName = block.exercise.name,
                    setIndex = set.index,
                    setCount = currentSetTargets.size,
                    repsMin = set.repsMin,
                    repsMax = set.repsMax,
                    reps = set.reps,
                    weightKg = set.weightKg,
                    restSeconds = set.restSeconds,
                    restRemainingSeconds = restRemainingSeconds,
                    isResting = isResting
                )
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Text("CoachApp", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        PageTabs(
            selectedPage = selectedPage,
            onSelect = { selectedPage = it }
        )
        if (selectedPage == CoachPage.Profile) {
            ProfileScreen(
                profile = profile,
                status = profileStatus,
                onSave = { input ->
                    profileStatus = "Sauvegarde en cours"
                    scope.launch {
                        runCatching { onSaveUserProfile(input) }
                            .onSuccess {
                                profile = it
                                profileStatus = "Profil sauvegarde"
                            }
                            .onFailure { profileStatus = "Sauvegarde impossible" }
                    }
                }
            )
        } else {
        Text(today.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("${today.targetMinutes} min - $totalSets series", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (nextScheduledWorkout.date != todayDate) {
                Text(
                    "Prochaine seance: ${formatScheduleDate(nextScheduledWorkout.date)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HealthConnectCard(
                status = healthSyncStatus,
                result = healthSyncResult,
                onRequestPermissions = onRequestHealthConnectPermissions,
                onSync = {
                    healthSyncStatus = "Synchronisation Samsung en cours"
                    scope.launch {
                        runCatching { onSyncHealthConnect() }
                            .onSuccess { result ->
                                healthSyncResult = result
                                refreshDailyActivity()
                                healthSyncStatus = if (result.missingNotes.isEmpty()) {
                                    "Donnees Samsung synchronisees"
                                } else {
                                    "Synchronisation partielle"
                                }
                            }
                            .onFailure {
                                healthSyncStatus = "Synchronisation impossible"
                            }
                    }
                }
            )

            DailyActivityHistogram(
                coachSnapshots = dailyActivity,
                healthSnapshots = dailyHealthSnapshots,
                status = dailyActivityStatus,
                healthSourceNote = dailyHealthActivityStatus
            )

            if (sessionDone) {
                SessionSummary(
                    completedSets = completedSets,
                    totalSets = totalSets,
                    estimatedCalories = estimatedCalories,
                    persistenceStatus = persistenceStatus,
                    latestSummary = latestSummary
                )
                OutlinedButton(
                    onClick = {
                            currentBlockIndex = 0
                            currentSetIndex = 0
                            completedSets = 0
                            completedSetRecords.clear()
                            sessionSetsByBlock.forEachIndexed { index, targets ->
                                targets.clear()
                                today.blocks[index].sets.forEach { set -> targets.add(set.toSessionSetTarget()) }
                            }
                            completedSetsByBlock.indices.forEach { completedSetsByBlock[it] = 0 }
                            sessionDone = false
                            persistenceStatus = null
                            restRemainingSeconds = 0
                        startedAtEpochMillis = System.currentTimeMillis()
                    },
                    colors = purpleOutlinedButtonColors()
                ) {
                    Text("Nouvelle seance")
                }
            } else if (currentBlock != null && currentSet != null) {
                CurrentExerciseCard(
                    block = currentBlock,
                    sets = currentSetTargets,
                    currentSetIndex = currentSetIndex,
                    restRemainingSeconds = restRemainingSeconds
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                    onClick = { completeCurrentSet(source = "phone") },
                        enabled = !isResting,
                        colors = purpleButtonColors()
                    ) {
                        Text(if (isResting) "Repos" else "Valider la serie")
                    }
                    OutlinedButton(
                        onClick = { finishSession() },
                        colors = purpleOutlinedButtonColors()
                    ) {
                        Text("Terminer")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Programme du jour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            today.blocks.forEachIndexed { index, block ->
                val blockCompletedSets = completedSetsByBlock.getOrElse(index) { 0 }
                PlannedExerciseCard(
                    block = block,
                    isCurrent = index == currentBlockIndex && !sessionDone,
                    isCompleted = blockCompletedSets >= sessionSetsByBlock[index].size,
                    completedSetCount = blockCompletedSets,
                    totalSetCount = sessionSetsByBlock[index].size,
                    onFocus = { focusBlock(index) }
                )
            }

            UpcomingWorkoutsCard(upcomingWorkouts)
        }
    }
}
}

private enum class CoachPage {
    Session,
    Profile
}

@Composable
private fun PageTabs(
    selectedPage: CoachPage,
    onSelect: (CoachPage) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val sessionSelected = selectedPage == CoachPage.Session
        val profileSelected = selectedPage == CoachPage.Profile
        if (sessionSelected) {
            Button(onClick = { onSelect(CoachPage.Session) }, colors = purpleButtonColors()) {
                Text("Seance")
            }
        } else {
            OutlinedButton(onClick = { onSelect(CoachPage.Session) }, colors = purpleOutlinedButtonColors()) {
                Text("Seance")
            }
        }
        if (profileSelected) {
            Button(onClick = { onSelect(CoachPage.Profile) }, colors = purpleButtonColors()) {
                Text("Mon profil")
            }
        } else {
            OutlinedButton(onClick = { onSelect(CoachPage.Profile) }, colors = purpleOutlinedButtonColors()) {
                Text("Mon profil")
            }
        }
    }
}

@Composable
private fun UpcomingWorkoutsCard(workouts: List<ScheduledWorkout>) {
    var expanded by remember { mutableStateOf(true) }

    CoachCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("3 prochains entrainements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { expanded = !expanded }, colors = purpleOutlinedButtonColors()) {
                Text(if (expanded) "Masquer" else "Voir")
            }
        }

        if (expanded) {
            workouts.forEach { scheduled ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${formatScheduleDate(scheduled.date)} - ${scheduled.workoutDay.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${scheduled.workoutDay.targetMinutes} min - " +
                            scheduled.workoutDay.blocks.joinToString(" | ") { block ->
                                "${block.exercise.name} (${block.sets.size}x)"
                            },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    profile: UserProfileSnapshot?,
    status: String?,
    onSave: (UserProfileInput) -> Unit
) {
    var heightText by remember(profile) { mutableStateOf(profile?.heightCm?.formatOneDecimal().orEmpty()) }
    var weightText by remember(profile) { mutableStateOf(profile?.weightKg?.formatOneDecimal().orEmpty()) }
    var ageText by remember(profile) { mutableStateOf(profile?.ageYears?.toString().orEmpty()) }
    var targetSessionMinutesText by remember(profile) {
        mutableStateOf((profile?.targetSessionMinutes ?: DefaultTargetSessionMinutes).toString())
    }
    var selectedDays by remember(profile) {
        mutableStateOf(profile?.trainingDaysIso ?: DefaultTrainingDaysIso)
    }

    CoachCard {
        Text("Mon profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Source locale si Samsung Health n'est pas a jour", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("Taille cm") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Poids kg") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ageText,
            onValueChange = { ageText = it },
            label = { Text("Age") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = targetSessionMinutesText,
            onValueChange = { targetSessionMinutesText = it },
            label = { Text("Duree seance min") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Jours d'entrainement", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        TrainingDayCalendar(
            selectedDays = selectedDays,
            onToggleDay = { day ->
                selectedDays = if (day in selectedDays) {
                    selectedDays - day
                } else {
                    selectedDays + day
                }
            }
        )
        Button(
            onClick = {
                onSave(
                    UserProfileInput(
                    heightCm = heightText.toDoubleOrNull(),
                    weightKg = weightText.toDoubleOrNull(),
                    ageYears = ageText.toIntOrNull(),
                    trainingDaysIso = selectedDays,
                    targetSessionMinutes = targetSessionMinutesText.toIntOrNull()
                )
            )
            },
            colors = purpleButtonColors()
        ) {
            Text("Sauvegarder")
        }
        status?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
    }
}

@Composable
private fun TrainingDayCalendar(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(TrainingDayLabels.take(4), TrainingDayLabels.drop(4)).forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowDays.forEach { (day, label) ->
                    val selected = day in selectedDays
                    if (selected) {
                        Button(
                            onClick = { onToggleDay(day) },
                            modifier = Modifier.weight(1f),
                            colors = purpleButtonColors()
                        ) {
                            Text(label)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onToggleDay(day) },
                            modifier = Modifier.weight(1f),
                            colors = purpleOutlinedButtonColors()
                        ) {
                            Text(label)
                        }
                    }
                }
                repeat(4 - rowDays.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HealthConnectCard(
    status: String?,
    result: HealthSyncResult?,
    onRequestPermissions: () -> Unit,
    onSync: () -> Unit
) {
    CoachCard {
        Text("Samsung Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRequestPermissions, colors = purpleOutlinedButtonColors()) {
                Text("Autoriser")
            }
            Button(onClick = onSync, colors = purpleButtonColors()) {
                Text("Synchroniser")
            }
        }
        status?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
        result?.bodySnapshot?.let { snapshot ->
            snapshot.weightKg?.let { Text("Poids : ${it.formatOneDecimal()} kg") }
            snapshot.heightCm?.let { Text("Taille : ${it.formatOneDecimal()} cm") }
            snapshot.bmi?.let { Text("IMC estime : ${it.formatOneDecimal()}") }
            snapshot.bmr?.let { Text("BMR estime : ${it.toInt()} kcal/j") }
        }
        result?.restingHeartRateBpm?.let { Text("FC repos : $it bpm") }
        result?.activeEnergyKcalToday?.let { Text("Energie active aujourd'hui : ${it.toInt()} kcal") }
        result?.missingNotes?.takeIf { it.isNotEmpty() }?.forEach { note ->
            Text(note, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DailyActivityHistogram(
    coachSnapshots: List<DailyCoachActivitySnapshot>,
    healthSnapshots: List<DailyHealthActivitySnapshot>,
    status: String?,
    healthSourceNote: String?
) {
    val coachByDate = coachSnapshots.associateBy { it.dateIso }
    val healthByDate = healthSnapshots.associateBy { it.dateIso }
    val dates = (coachByDate.keys + healthByDate.keys).sorted()
    val maxCalories = dates.maxOfOrNull { dateIso ->
        val coachCalories = coachByDate[dateIso]?.workoutCalories ?: 0.0
        val healthCalories = healthByDate[dateIso]?.activeEnergyKcal ?: 0.0
        coachCalories + healthCalories
    } ?: 0.0
    var showAllDays by remember { mutableStateOf(false) }
    val visibleDates = if (showAllDays) dates else dates.takeLast(1)

    CoachCard {
        Text("Activite recente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Calories estimees par jour",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            ActivityHistoryTab(
                expanded = showAllDays,
                enabled = dates.isNotEmpty(),
                onClick = { showAllDays = !showAllDays },
                modifier = Modifier.align(Alignment.TopStart)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(start = 58.dp)
                    .fillMaxWidth()
            ) {
                if (dates.isEmpty()) {
                    Text(
                        status ?: "Aucune seance sauvegardee.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    visibleDates.forEach { dateIso ->
                val coach = coachByDate[dateIso]
                val health = healthByDate[dateIso]
                val coachCalories = coach?.workoutCalories ?: 0.0
                val healthCalories = health?.activeEnergyKcal ?: 0.0
                val healthStepsText = health?.steps?.let { "$it pas" } ?: "pas indisponibles"
                val totalCalories = coachCalories + healthCalories
                val totalFraction = if (maxCalories > 0.0 && totalCalories > 0.0) {
                    (totalCalories / maxCalories).toFloat().coerceIn(0.05f, 1f)
                } else {
                    0f
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatActivityDate(dateIso), fontWeight = FontWeight.Bold)
                        Text("${totalCalories.toInt()} kcal estimees")
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2A2339))
                    ) {
                        if (totalFraction > 0f) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(totalFraction)
                                    .fillMaxHeight()
                            ) {
                                if (coachCalories > 0.0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(coachCalories.toFloat())
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                if (healthCalories > 0.0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(healthCalories.toFloat())
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "CoachApp: ${coachCalories.toInt()} kcal, ${coach?.completedSets ?: 0} series",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Sante: ${healthCalories.toInt()} kcal, $healthStepsText",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    }
                }
            }
        }

    }
}

}

@Composable
private fun ActivityHistoryTab(
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabColor = if (enabled) MaterialTheme.colorScheme.primary else Color(0xFF3A304F)
    val textColor = if (enabled) MaterialTheme.colorScheme.onPrimary else Color(0xFFB7AAC9)

    Box(
        modifier = modifier
            .width(48.dp)
            .height(if (expanded) 220.dp else 128.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tabColor)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (expanded) "Reduire" else "Tous",
            modifier = Modifier.rotate(-90f),
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun CurrentExerciseCard(
    block: ExerciseBlock,
    sets: List<SessionSetTarget>,
    currentSetIndex: Int,
    restRemainingSeconds: Int
) {
    val currentSet = sets[currentSetIndex]
    CoachCard(highlight = true) {
        ExerciseAssetImage(
            assetPath = block.exercise.illustrationAsset,
            contentDescription = "Illustration ${block.exercise.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
        Text("Exercice en cours", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(block.exercise.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Serie ${currentSet.index} - ${currentSet.reps} reps - ${currentSet.weightKg.formatOneDecimal()} kg")
        Text(
            if (restRemainingSeconds > 0) {
                "Repos en cours : ${formatTimer(restRemainingSeconds)}"
            } else {
                "Repos prevu : ${currentSet.restSeconds}s"
            },
            color = if (restRemainingSeconds > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(block.notes, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Series restantes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        sets.drop(currentSetIndex).forEachIndexed { offset, set ->
            val isCurrent = offset == 0
            val label = if (isCurrent) "Maintenant" else "Ensuite"
            Text(
                "$label - Serie ${set.index}: ${set.reps} reps, ${set.weightKg.formatOneDecimal()} kg, repos ${set.restSeconds}s",
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        }
    }
@Composable
private fun PlannedExerciseCard(
    block: ExerciseBlock,
    isCurrent: Boolean,
    isCompleted: Boolean,
    completedSetCount: Int,
    totalSetCount: Int,
    onFocus: () -> Unit
) {
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryTextColor = if (isCompleted) mutedTextColor else MaterialTheme.colorScheme.onSurface
    CoachCard(
        highlight = isCurrent,
        containerColor = when {
            isCompleted -> CompletedCardColor
            isCurrent -> ElevatedCardColor
            else -> CardColor
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isCompleted && !isCurrent,
                onClick = onFocus
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        ) {
            ExerciseAssetImage(
                assetPath = block.exercise.illustrationAsset,
                contentDescription = "Illustration ${block.exercise.name}",
                modifier = Modifier.fillMaxSize()
            )
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA111018)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✓",
                        color = SuccessGreen,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Text(block.exercise.name, fontWeight = FontWeight.Bold, color = primaryTextColor)
        Text(
            "$completedSetCount/$totalSetCount series - ${block.exercise.equipment}",
            color = mutedTextColor
        )
        Text(block.notes, color = mutedTextColor)
        if (isCompleted) {
            Text("Exercice termine", color = SuccessGreen, fontWeight = FontWeight.Bold)
        } else if (isCurrent) {
            Text("En focus", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CoachCard(
    highlight: Boolean = false,
    containerColor: Color = if (highlight) ElevatedCardColor else CardColor,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlight) 4.dp else 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun ExerciseAssetImage(
    assetPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        runCatching {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ImageFallbackColor)
        )
    }
}

@Composable
private fun SessionSummary(
    completedSets: Int,
    totalSets: Int,
    estimatedCalories: Double,
    persistenceStatus: String?,
    latestSummary: SessionSummarySnapshot?
) {
    CoachCard(highlight = true) {
        Text("Seance terminee", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Series validees : $completedSets / $totalSets")
        Text("Calories estimees : ${estimatedCalories.toInt()} kcal")
        latestSummary?.let { summary ->
            Text("Volume : ${summary.totalVolumeKg.toInt()} kg")
        Text("Progression effective : ${formatVolumeDelta(summary)}")
            Text("Statut : ${recordLabel(summary.recordType)}")
            Text(summary.nextHint, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Fin : ${formatEndedAt(summary.endedAtEpochMillis)}")
        }
        persistenceStatus?.let { Text(it, color = MaterialTheme.colorScheme.secondary) }
    }
}

@Composable
private fun purpleButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = Color(0xFF3A304F),
    disabledContentColor = Color(0xFFB7AAC9)
)

@Composable
private fun purpleOutlinedButtonColors() = ButtonDefaults.outlinedButtonColors(
    contentColor = MaterialTheme.colorScheme.primary,
    disabledContentColor = Color(0xFF8C7EA0)
)

private fun formatVolumeDelta(summary: SessionSummarySnapshot): String {
    val delta = summary.volumeDeltaKg ?: return "reference"
    val sign = if (delta > 0.0) "+" else ""
    val averageText = summary.averageComparableVolumeKg?.toInt()?.let { average ->
        val count = summary.averageComparableSessionCount
        " vs moyenne $average kg" + if (count > 0) " ($count seances)" else ""
    }.orEmpty()
    return "$sign${delta.toInt()} kg$averageText"
}

private fun recordLabel(recordType: String): String {
    return when (recordType) {
        "baseline" -> "Reference initiale"
        "volume_record" -> "Record volume"
        "stable" -> "Volume stable"
        else -> "Sous derniere seance comparable"
    }
}

private fun SetTarget.toSessionSetTarget(): SessionSetTarget {
    return SessionSetTarget(
        index = index,
        repsMin = repsMin,
        repsMax = repsMax,
        reps = repsMax,
        weightKg = weightKg,
        restSeconds = restSeconds,
        rirTarget = rirTarget
    )
}

private fun CompletedSetRecord.toSessionSetTarget(index: Int): SessionSetTarget {
    return SessionSetTarget(
        index = index,
        repsMin = plannedRepsMin,
        repsMax = plannedRepsMax,
        reps = performedReps ?: plannedRepsMax,
        weightKg = weightKg,
        restSeconds = restSeconds,
        rirTarget = rir ?: 2
    )
}

private fun formatEndedAt(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun formatActivityDate(dateIso: String): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    return runCatching {
        LocalDate.parse(dateIso).format(formatter)
    }.getOrDefault(dateIso)
}

private fun formatScheduleDate(date: LocalDate): String {
    val dayLabel = TrainingDayLabels.firstOrNull { it.first == date.dayOfWeek.value }?.second
        ?: date.dayOfWeek.value.toString()
    val formatter = DateTimeFormatter.ofPattern("dd/MM")
    return "$dayLabel ${date.format(formatter)}"
}

private fun formatTimer(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val minutes = clamped / 60
    val seconds = clamped % 60
    return "%d:%02d".format(minutes, seconds)
}

private fun Double.formatOneDecimal(): String {
    val rounded = kotlin.math.round(this * 10.0) / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
