# CoachApp

CoachApp est une application Android + Wear OS ultra simple pour suivre la progression en musculation.

Objectif d'usage : ouvrir l'application, voir le programme du jour, lancer la séance, valider les séries depuis la Galaxy Watch, ajuster le poids à la volée, terminer la séance depuis la montre, puis recevoir un résumé clair : charge totale, progression par rapport à la dernière séance identique, estimation de calories, records et piste d'amélioration.

> Projet initialisé pour être développé par agents ChatGPT/Codex en autonomie. Les agents doivent lire `AGENTS.md`, puis les documents dans `docs/`, avant toute modification.

## Périmètre MVP

- Programme 5 jours/semaine, 30 minutes par séance.
- Exercices avec consignes, illustration/vidéo courte à ajouter sous forme d'assets propriétaires ou libres.
- Séries, répétitions, poids, temps de repos et progression à la volée.
- Companion Wear OS : exercice courant, série courante, validation, ajustement du poids, timer de repos, fin de séance.
- Calculs science-first : volume, surcharge progressive, IMC, métabolisme basal, estimation des calories par MET.
- Intégration santé : Health Connect en priorité, Samsung Health Data SDK si besoin de données Samsung plus riches.

## Architecture

```text
CoachApp/
├── app/                 # Application Android téléphone
├── wear/                # Application Wear OS / Galaxy Watch
├── shared/              # Modèles, protocole montre, calculs science-first
├── docs/                # Spécifications produit, science, architecture, prompts agents
├── .github/             # CI et instructions GitHub/Copilot/Codex
└── gradle/              # Version catalog Gradle
```

## Stack proposée

- Kotlin
- Jetpack Compose Material 3
- Wear Compose
- Health Connect
- Samsung Health Data SDK à intégrer derrière une interface optionnelle
- Google Play Services Wearable Data Layer pour la communication téléphone ↔ montre
- Gradle Kotlin DSL

## Démarrage local

Pré-requis : Android Studio récent, JDK 17, Android SDK avec API 37 si disponible.

```bash
gradle :app:assembleDebug :wear:assembleDebug
```

Le dépôt ne contient pas encore le JAR binaire du Gradle Wrapper. Première tâche agent recommandée :

```bash
gradle wrapper --gradle-version 9.4.1
./gradlew :app:assembleDebug :wear:assembleDebug
```

## Règles importantes

1. Ne pas vendre les estimations comme médicales. L'app est fitness/wellness.
2. Les formules doivent être traçables, testées, documentées et modifiables.
3. Aucune vidéo/image d'exercice ne doit être copiée sans licence claire.
4. La montre doit rester utilisable en plein effort : gros boutons, peu de texte, états explicites.
5. Chaque agent doit livrer par PR petite, testée, avec notes de validation.

## Documentation principale

- `AGENTS.md` : protocole obligatoire pour agents.
- `docs/00_PROJECT_BRIEF.md` : vision et contraintes.
- `docs/01_PRODUCT_SPEC.md` : fonctionnalités et UX.
- `docs/02_ARCHITECTURE.md` : modules et flux techniques.
- `docs/03_SCIENCE_SPEC.md` : règles d'entraînement, calories, IMC, BMR.
- `docs/04_DATA_MODEL.md` : modèle de données cible.
- `docs/05_AGENT_WORKFLOW.md` : méthode de travail des agents.
- `docs/prompts/` : prompts prêts à coller dans des agents ChatGPT.
