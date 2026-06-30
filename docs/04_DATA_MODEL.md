# 04 - Data model

## Core models existants

Le module `core` contient deja :

- `Exercise`
- `SetTarget`
- `ExerciseBlock`
- `WorkoutDay`
- `Science`
- `DefaultProgram`
- `WatchProtocol`
- `WatchSessionState`

## Entites cible Room

### ExerciseEntity

- id
- name
- muscles
- equipment
- illustrationAsset
- videoAsset

### WorkoutSessionEntity

- id
- planDayId
- startedAt
- endedAt
- estimatedCalories
- totalVolumeKg

### PerformedSetEntity

- id
- sessionId
- exerciseId
- setIndex
- reps
- weightKg
- completedAt
- rir

### BodySnapshotEntity

- id
- date
- weightKg
- heightCm
- bodyFatPercent
- bmi
- bmr
- source

### ComparableWorkoutStats

Objet calcule pour le resume :

- currentVolumeKg
- previousComparableVolumeKg
- volumeDeltaKg
- currentCalories
- previousCalories
- recordType
- nextHint
