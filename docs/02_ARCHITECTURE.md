# 02 - Architecture

## Modules

```text
app/   -> application Android telephone, Compose Material 3, notification de resume
wear/  -> application Wear OS, validation pendant la seance
core/  -> modeles, formules, programme par defaut, protocole montre
```

## Flux seance

1. `app` choisit le `WorkoutDay` du jour depuis `core`.
2. L'utilisateur lance la seance.
3. `app` envoie l'etat courant vers `wear`.
4. `wear` affiche l'exercice et renvoie les actions utilisateur.
5. `app` sauvegarde la seance et calcule le resume.
6. `app` affiche une notification de fin.

## Stockage local cible

Premier stockage recommande : Room.

Entites cible :

- `ExerciseEntity`
- `WorkoutPlanEntity`
- `WorkoutSessionEntity`
- `PerformedSetEntity`
- `BodySnapshotEntity`
- `HealthSyncStateEntity`

## Principes

- `core` ne depend pas de Compose.
- `wear` collecte les actions en plein effort.
- `app` reste la source principale de session.
- Les integrations externes passent par des interfaces testables.
