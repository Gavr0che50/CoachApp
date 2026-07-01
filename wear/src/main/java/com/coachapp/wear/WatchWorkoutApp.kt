package com.coachapp.wear

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.coachapp.core.DefaultProgram
import com.coachapp.core.WatchSessionState
import java.time.LocalDate
import kotlinx.coroutines.delay

private val WatchBackground = Color(0xFF050509)
private val WatchSurface = Color(0xFF171121)
private val WatchPurple = Color(0xFFB78CFF)
private val WatchPurpleDeep = Color(0xFF3B245E)
private val WatchText = Color(0xFFF5EEFF)
private val WatchMuted = Color(0xFFC8B9DC)
private val WatchMint = Color(0xFF7EE3D5)
private val RingTrack = Color(0xFF21182F)
private const val DebugWeightStepKg = 2.4
private const val DebugRestStepSeconds = 30
private const val DebugMaxRestSeconds = 180

@Composable
fun WatchWorkoutApp(
    remoteState: WatchSessionState? = null,
    remoteFinished: Boolean = false,
    onRemoteSetDone: () -> Unit = {},
    onRemoteFinish: () -> Unit = {},
    onRemoteWeightUp: () -> Unit = {},
    onRemoteWeightDown: () -> Unit = {},
    onRemoteRepsUp: () -> Unit = {},
    onRemoteRepsDown: () -> Unit = {},
    onRemoteRestUp: () -> Unit = {},
    onRemoteRestDown: () -> Unit = {},
    onRemoteAddSet: () -> Unit = {},
    onRemoteRemoveSet: () -> Unit = {}
) {
    val playSignal = rememberWatchSignalPlayer()
    var previousState by remember { mutableStateOf<WatchSessionState?>(null) }
    var previousFinished by remember { mutableStateOf(false) }

    LaunchedEffect(remoteState, remoteFinished) {
        if (remoteFinished && !previousFinished) {
            playSignal(WatchSignal.SessionFinished)
        } else if (remoteState != null) {
            val previous = previousState
            when {
                previous == null -> playSignal(WatchSignal.SeriesStart)
                remoteState.isResting && !previous.isResting -> {
                    playSignal(WatchSignal.SeriesStop)
                    delay(170)
                    playSignal(WatchSignal.RestStart)
                }
                !remoteState.isResting && previous.isResting -> {
                    playSignal(WatchSignal.RestEnd)
                    delay(170)
                    playSignal(WatchSignal.SeriesStart)
                }
                !remoteState.isResting &&
                    (remoteState.setIndex != previous.setIndex || remoteState.exerciseName != previous.exerciseName) ->
                    playSignal(WatchSignal.SeriesStart)
            }
        }
        previousState = remoteState
        previousFinished = remoteFinished
    }

    MaterialTheme {
        Scaffold(timeText = { TimeText(timeTextStyle = MaterialTheme.typography.caption1.copy(color = WatchMuted)) }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WatchBackground)
            ) {
                if (remoteFinished) {
                    RemoteFinishedContent(remoteState)
                } else if (remoteState != null) {
                    RestProgressRing(
                        restSeconds = remoteState.restSeconds,
                        restRemainingSeconds = if (remoteState.isResting) remoteState.restRemainingSeconds else 0
                    )
                    RemoteSessionContent(
                        state = remoteState,
                onSetDone = onRemoteSetDone,
                onFinish = onRemoteFinish,
                onWeightUp = onRemoteWeightUp,
                onWeightDown = onRemoteWeightDown,
                onRepsUp = onRemoteRepsUp,
                onRepsDown = onRemoteRepsDown,
                onRestUp = onRemoteRestUp,
                onRestDown = onRemoteRestDown,
                onAddSet = onRemoteAddSet,
                onRemoveSet = onRemoteRemoveSet
            )
                } else {
                    LocalFallbackContent(playSignal)
                }
            }
        }
    }
}

@Composable
private fun LocalFallbackContent(playSignal: (WatchSignal) -> Unit) {
    val workoutDay = remember { DefaultProgram.dayForIsoDay(LocalDate.now().dayOfWeek.value) }
    var blockIndex by remember { mutableIntStateOf(0) }
    var setIndex by remember { mutableIntStateOf(0) }
    var completedSets by remember { mutableIntStateOf(0) }
    var restRemainingSeconds by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var hadActiveRest by remember { mutableStateOf(false) }
    var editMode by remember(blockIndex, setIndex) { mutableStateOf(WatchEditMode.Reps) }
    var debugVersion by remember { mutableIntStateOf(0) }
    val debugSets = remember(workoutDay) {
        workoutDay.blocks.map { block ->
            block.sets.map { target ->
                LocalDebugSet(
                    index = target.index,
                    reps = target.repsMax,
                    weightKg = target.weightKg,
                    restSeconds = target.restSeconds
                )
            }.toMutableList()
        }
    }

    val totalSets = debugVersion.let { debugSets.sumOf { it.size } }
    val block = workoutDay.blocks.getOrNull(blockIndex)
    val currentSets = debugVersion.let { debugSets.getOrNull(blockIndex).orEmpty() }
    val set = currentSets.getOrNull(setIndex)
    val isResting = restRemainingSeconds > 0 && !done
    val editLabel = when (editMode) {
        WatchEditMode.Reps -> "Reps"
        WatchEditMode.Weight -> "Kg"
        WatchEditMode.Rest -> "Repos"
        WatchEditMode.Set -> "Set"
    }
    val editValue = when (editMode) {
        WatchEditMode.Reps -> set?.reps?.toString().orEmpty()
        WatchEditMode.Weight -> set?.weightKg?.formatOneDecimal().orEmpty()
        WatchEditMode.Rest -> "${set?.restSeconds ?: 0}s"
        WatchEditMode.Set -> currentSets.size.toString()
    }

    fun updateCurrentSet(repsDelta: Int = 0, weightDeltaKg: Double = 0.0, restDeltaSeconds: Int = 0) {
        val mutableSets = debugSets.getOrNull(blockIndex) ?: return
        val current = mutableSets.getOrNull(setIndex) ?: return
        val updated = current.copy(
            reps = (current.reps + repsDelta).coerceAtLeast(1),
            weightKg = (current.weightKg + weightDeltaKg).coerceAtLeast(0.0),
            restSeconds = (current.restSeconds + restDeltaSeconds).coerceIn(0, DebugMaxRestSeconds)
        )
        mutableSets[setIndex] = updated
        if (restDeltaSeconds != 0 && restRemainingSeconds > 0) {
            restRemainingSeconds = (restRemainingSeconds + restDeltaSeconds).coerceIn(0, updated.restSeconds)
        }
        debugVersion += 1
    }

    fun addDebugSet() {
        val mutableSets = debugSets.getOrNull(blockIndex) ?: return
        val pattern = mutableSets.getOrNull((setIndex - 1).coerceAtLeast(0)) ?: mutableSets.lastOrNull() ?: return
        mutableSets.add(pattern.copy(index = mutableSets.size + 1))
        debugVersion += 1
    }

    fun removeDebugSet() {
        val mutableSets = debugSets.getOrNull(blockIndex) ?: return
        if (mutableSets.size <= 1 || setIndex >= mutableSets.lastIndex) return
        mutableSets.removeAt(mutableSets.lastIndex)
        debugVersion += 1
    }

    val increaseAction = when (editMode) {
        WatchEditMode.Reps -> { { updateCurrentSet(repsDelta = 1) } }
        WatchEditMode.Weight -> { { updateCurrentSet(weightDeltaKg = DebugWeightStepKg) } }
        WatchEditMode.Rest -> { { updateCurrentSet(restDeltaSeconds = DebugRestStepSeconds) } }
        WatchEditMode.Set -> { { addDebugSet() } }
    }
    val decreaseAction = when (editMode) {
        WatchEditMode.Reps -> { { updateCurrentSet(repsDelta = -1) } }
        WatchEditMode.Weight -> { { updateCurrentSet(weightDeltaKg = -DebugWeightStepKg) } }
        WatchEditMode.Rest -> { { updateCurrentSet(restDeltaSeconds = -DebugRestStepSeconds) } }
        WatchEditMode.Set -> { { removeDebugSet() } }
    }

    LaunchedEffect(Unit) {
        playSignal(WatchSignal.SeriesStart)
    }

    LaunchedEffect(restRemainingSeconds, done) {
        if (restRemainingSeconds > 0 && !done) {
            hadActiveRest = true
            delay(1000)
            restRemainingSeconds = (restRemainingSeconds - 1).coerceAtLeast(0)
        } else if (hadActiveRest && !done) {
            hadActiveRest = false
            playSignal(WatchSignal.RestEnd)
            delay(170)
            playSignal(WatchSignal.SeriesStart)
        }
    }

    RestProgressRing(
        restSeconds = set?.restSeconds ?: 0,
        restRemainingSeconds = restRemainingSeconds
    )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Top)
        ) {
        if (done || block == null || set == null) {
            FinishedMark(text = "Seance terminee", subtext = "$completedSets / $totalSets")
            return@Column
        }

        Text(text = "${block.exercise.name} - S${set.index}/${currentSets.size}", textAlign = TextAlign.Center, style = MaterialTheme.typography.caption1, color = WatchText, fontWeight = FontWeight.Bold)
        Text(
            text = "${set.reps}r - ${set.weightKg.formatOneDecimal()}kg - ${if (isResting) "R ${formatTimer(restRemainingSeconds)}" else "Pret"}",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption1,
            color = if (isResting) WatchMint else WatchPurple,
            fontWeight = FontWeight.Bold
        )
        WatchCompactAdjustmentDial(
            label = editLabel,
            value = editValue,
            onIncrease = increaseAction,
            onDecrease = decreaseAction
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WatchCompactButton(text = "Rep", modifier = Modifier.width(40.dp), selected = editMode == WatchEditMode.Reps, onClick = { editMode = WatchEditMode.Reps })
            WatchCompactButton(text = "Kg", modifier = Modifier.width(40.dp), selected = editMode == WatchEditMode.Weight, onClick = { editMode = WatchEditMode.Weight })
            WatchCompactButton(text = "Rest", modifier = Modifier.width(40.dp), selected = editMode == WatchEditMode.Rest, onClick = { editMode = WatchEditMode.Rest })
            WatchCompactButton(text = "Set", modifier = Modifier.width(40.dp), selected = editMode == WatchEditMode.Set, onClick = { editMode = WatchEditMode.Set })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WatchCompactButton(text = "-", modifier = Modifier.width(48.dp), onClick = decreaseAction)
            WatchCompactButton(text = "+", modifier = Modifier.width(48.dp), onClick = increaseAction)
        }
        WatchCompactButton(
            text = if (isResting) "Repos" else "OK",
            modifier = Modifier.fillMaxWidth(0.58f),
            selected = true,
            enabled = !isResting,
            onClick = {
                playSignal(WatchSignal.SeriesStop)
                completedSets += 1
                if (setIndex + 1 < currentSets.size) {
                    setIndex += 1
                    restRemainingSeconds = set.restSeconds
                    playSignal(WatchSignal.RestStart)
                } else if (blockIndex + 1 < workoutDay.blocks.size) {
                    blockIndex += 1
                    setIndex = 0
                    restRemainingSeconds = set.restSeconds
                    playSignal(WatchSignal.RestStart)
                } else {
                    done = true
                    restRemainingSeconds = 0
                    playSignal(WatchSignal.SessionFinished)
                }
            }
        )
    }
}

@Composable
private fun RemoteSessionContent(
    state: WatchSessionState,
    onSetDone: () -> Unit,
    onFinish: () -> Unit,
    onWeightUp: () -> Unit,
    onWeightDown: () -> Unit,
    onRepsUp: () -> Unit,
    onRepsDown: () -> Unit,
    onRestUp: () -> Unit,
    onRestDown: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: () -> Unit
) {
    var editMode by remember(state.sessionId, state.setIndex) { mutableStateOf(WatchEditMode.Reps) }
    val editLabel = when (editMode) {
        WatchEditMode.Reps -> "Reps"
        WatchEditMode.Weight -> "Kg"
        WatchEditMode.Rest -> "Repos"
        WatchEditMode.Set -> "Set"
    }
    val editValue = when (editMode) {
        WatchEditMode.Reps -> state.reps.toString()
        WatchEditMode.Weight -> state.weightKg.formatOneDecimal()
        WatchEditMode.Rest -> "${state.restSeconds}s"
        WatchEditMode.Set -> state.setCount.toString()
    }
    val increaseAction = when (editMode) {
        WatchEditMode.Reps -> onRepsUp
        WatchEditMode.Weight -> onWeightUp
        WatchEditMode.Rest -> onRestUp
        WatchEditMode.Set -> onAddSet
    }
    val decreaseAction = when (editMode) {
        WatchEditMode.Reps -> onRepsDown
        WatchEditMode.Weight -> onWeightDown
        WatchEditMode.Rest -> onRestDown
        WatchEditMode.Set -> onRemoveSet
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 34.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        Text(text = "Telephone", textAlign = TextAlign.Center, color = WatchMuted)
        Text(text = state.exerciseName, textAlign = TextAlign.Center, style = MaterialTheme.typography.title3, color = WatchText)
        Text(text = "Serie ${state.setIndex}/${state.setCount}", textAlign = TextAlign.Center, color = WatchMuted)
        Text(text = "${state.reps} reps - ${state.weightKg.formatOneDecimal()} kg", textAlign = TextAlign.Center, color = WatchText)
        Text(
            text = if (state.isResting) {
                "Repos ${formatTimer(state.restRemainingSeconds)}"
            } else {
                "Pret"
            },
            textAlign = TextAlign.Center,
            color = if (state.isResting) WatchMint else WatchPurple,
            fontWeight = FontWeight.Bold
        )
        WatchAdjustmentDial(
            label = editLabel,
            value = editValue,
            onIncrease = increaseAction,
            onDecrease = decreaseAction
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WatchActionButton(text = "Rep", modifier = Modifier.width(42.dp), color = if (editMode == WatchEditMode.Reps) WatchPurple else WatchSurface, onClick = { editMode = WatchEditMode.Reps })
            WatchActionButton(text = "Kg", modifier = Modifier.width(42.dp), color = if (editMode == WatchEditMode.Weight) WatchPurple else WatchSurface, onClick = { editMode = WatchEditMode.Weight })
            WatchActionButton(text = "Rest", modifier = Modifier.width(42.dp), color = if (editMode == WatchEditMode.Rest) WatchPurple else WatchSurface, onClick = { editMode = WatchEditMode.Rest })
            WatchActionButton(text = "Set", modifier = Modifier.width(42.dp), color = if (editMode == WatchEditMode.Set) WatchPurple else WatchSurface, onClick = { editMode = WatchEditMode.Set })
        }
        WatchActionButton(
            text = if (state.isResting) "Repos" else "OK",
            modifier = Modifier.fillMaxWidth(0.78f),
            enabled = !state.isResting,
            onClick = onSetDone
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WatchActionButton(text = "-", modifier = Modifier.width(58.dp), color = WatchSurface, onClick = decreaseAction)
            WatchActionButton(text = "+", modifier = Modifier.width(58.dp), color = WatchSurface, onClick = increaseAction)
        }
        WatchActionButton(
            text = "Fin",
            modifier = Modifier.fillMaxWidth(0.72f),
            color = Color(0xFF2A2137),
            textColor = WatchPurple,
            onClick = onFinish
        )
    }
}

@Composable
private fun WatchCompactAdjustmentDial(
    label: String,
    value: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.58f)
            .height(32.dp)
            .background(WatchPurpleDeep, RoundedCornerShape(8.dp))
            .pointerInput(label, value) {
                var dragTotal = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> dragTotal += dragAmount },
                    onDragEnd = {
                        when {
                            dragTotal < -18f -> onIncrease()
                            dragTotal > 18f -> onDecrease()
                        }
                        dragTotal = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("$label $value", color = WatchText, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WatchCompactButton(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val background = if (selected) WatchPurple else WatchSurface
    val foreground = if (selected) WatchSurface else WatchPurple
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) background else WatchSurface)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) foreground else WatchMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun WatchAdjustmentDial(
    label: String,
    value: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .height(54.dp)
            .background(WatchPurpleDeep, RoundedCornerShape(8.dp))
            .pointerInput(label, value) {
                var dragTotal = 0f
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount -> dragTotal += dragAmount },
                    onDragEnd = {
                        when {
                            dragTotal < -24f -> onIncrease()
                            dragTotal > 24f -> onDecrease()
                        }
                        dragTotal = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = WatchMuted, style = MaterialTheme.typography.caption2)
            Text(value, color = WatchText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RemoteFinishedContent(state: WatchSessionState?) {
    val text = if (state == null) "Seance terminee" else "Seance terminee"
    val subtext = state?.exerciseName ?: "Bon travail"
    FinishedMark(text = text, subtext = subtext)
}

@Composable
private fun FinishedMark(text: String, subtext: String) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.62f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "finished-scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "finished-alpha"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension - 34.dp.toPx()
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawCircle(
                color = WatchPurple.copy(alpha = 0.18f * alpha),
                radius = diameter / 2f,
                center = center
            )
            drawArc(
                color = WatchPurple.copy(alpha = alpha),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 34.dp)
                .scale(scale)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("✓", style = MaterialTheme.typography.display1, color = WatchMint, textAlign = TextAlign.Center)
            Text(text, style = MaterialTheme.typography.title3, color = WatchText, textAlign = TextAlign.Center)
            Text(subtext, color = WatchMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun RestProgressRing(restSeconds: Int, restRemainingSeconds: Int) {
    val progress = if (restSeconds > 0 && restRemainingSeconds > 0) {
        (restRemainingSeconds.toFloat() / restSeconds.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 8.dp.toPx()
        val diameter = size.minDimension - 18.dp.toPx()
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        drawArc(
            color = RingTrack,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        if (progress > 0f) {
            drawArc(
                color = WatchPurple,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun rememberWatchSignalPlayer(): (WatchSignal) -> Unit {
    val context = LocalContext.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }
    val vibrator = remember(context) { context.defaultVibrator() }

    return remember(toneGenerator, vibrator) {
        { signal ->
            val tone = when (signal) {
                WatchSignal.SeriesStart -> ToneGenerator.TONE_PROP_ACK
                WatchSignal.SeriesStop -> ToneGenerator.TONE_PROP_NACK
                WatchSignal.RestStart -> ToneGenerator.TONE_PROP_BEEP
                WatchSignal.RestEnd -> ToneGenerator.TONE_PROP_ACK
                WatchSignal.SessionFinished -> ToneGenerator.TONE_PROP_PROMPT
            }
            val duration = when (signal) {
                WatchSignal.SeriesStart -> 90
                WatchSignal.SeriesStop -> 90
                WatchSignal.RestStart -> 130
                WatchSignal.RestEnd -> 180
                WatchSignal.SessionFinished -> 360
            }
            toneGenerator.startTone(tone, duration)
            vibrator?.vibrateCompat(
                when (signal) {
                    WatchSignal.SeriesStart -> 35
                    WatchSignal.SeriesStop -> 45
                    WatchSignal.RestStart -> 70
                    WatchSignal.RestEnd -> 90
                    WatchSignal.SessionFinished -> 180
                }
            )
        }
    }
}

private fun Context.defaultVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

private fun Vibrator.vibrateCompat(durationMillis: Long) {
    if (!hasVibrator()) {
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrate(durationMillis)
    }
}

@Composable
private fun WatchActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = WatchPurple,
    textColor: Color = Color(0xFF1A0E2C),
    onClick: () -> Unit
) {
    val background = if (enabled) color else WatchPurpleDeep
    val foreground = if (enabled) textColor else WatchMuted
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = foreground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
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

private data class LocalDebugSet(
    val index: Int,
    val reps: Int,
    val weightKg: Double,
    val restSeconds: Int
)

private enum class WatchEditMode {
    Reps,
    Weight,
    Rest,
    Set
}

private enum class WatchSignal {
    SeriesStart,
    SeriesStop,
    RestStart,
    RestEnd,
    SessionFinished
}
