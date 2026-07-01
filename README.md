# CoachApp

CoachApp est une application Android + Wear OS ultra simple pour suivre la progression en musculation.

Objectif d'usage : ouvrir l'application, voir le programme du jour, lancer la seance, valider les series depuis la Galaxy Watch, ajuster le poids a la volee, terminer la seance depuis la montre, puis recevoir un resume clair : charge totale, progression par rapport a la derniere seance identique, estimation de calories, records et piste d'amelioration.

> Projet initialise pour etre developpe par agents ChatGPT/Codex en autonomie. Les agents doivent lire `docs/AGENT_GUIDE.md`, puis les documents dans `docs/`, avant toute modification.

## Perimetre MVP

- Programme 5 jours/semaine, 30 minutes par seance.
- Exercices avec consignes, illustration/video courte a ajouter sous forme d'assets proprietaires ou libres.
- Series, repetitions, poids, temps de repos et progression a la volee.
- Companion Wear OS : exercice courant, serie courante, validation, ajustement du poids, timer de repos, fin de seance.
- Calculs science-first : volume, surcharge progressive, IMC, metabolisme basal, estimation des calories par MET.
- Integration sante : Samsung Health Data SDK en priorite, Health Connect conserve en fallback technique.

## Architecture

```text
CoachApp/
├── app/                 # Application Android telephone
├── wear/                # Application Wear OS / Galaxy Watch
├── core/                # Modeles, protocole montre, calculs science-first
├── docs/                # Specifications produit, science, architecture, prompts agents
├── .github/             # CI GitHub Actions
└── gradle/              # Version catalog Gradle
```

## Stack proposee

- Kotlin
- Jetpack Compose Material 3
- Wear Compose
- Samsung Health Data SDK direct via passerelle optionnelle
- Health Connect en fallback si Samsung Health n'est pas disponible
- Google Play Services Wearable Data Layer pour la communication telephone <-> montre
- Gradle Kotlin DSL

## Demarrage local

Pre-requis : Android Studio recent ou Android SDK command-line tools, JDK 17,
Android SDK Platform 37.0 et Build Tools 36.0.0.

```bash
./gradlew projectCheck
```

Sous Windows PowerShell :

```powershell
.\gradlew.bat projectCheck
```

## Regles importantes

1. Ne pas vendre les estimations comme medicales. L'app est fitness/wellness.
2. Les formules doivent etre tracables, testees, documentees et modifiables.
3. Aucune video/image d'exercice ne doit etre copiee sans licence claire.
4. La montre doit rester utilisable en plein effort : gros boutons, peu de texte, etats explicites.
5. Chaque agent doit livrer par PR petite, testee, avec notes de validation.

## Documentation principale

- `docs/AGENT_GUIDE.md` : protocole obligatoire pour agents.
- `docs/00_PROJECT_BRIEF.md` : vision et contraintes.
- `docs/01_PRODUCT_SPEC.md` : fonctionnalites et UX.
- `docs/02_ARCHITECTURE.md` : modules et flux techniques.
- `docs/03_SCIENCE_SPEC.md` : regles d'entrainement, calories, IMC, BMR.
- `docs/04_DATA_MODEL.md` : modele de donnees cible.
- `docs/05_AGENT_WORKFLOW.md` : methode de travail des agents.
- `docs/prompts/` : prompts prets a coller dans des agents ChatGPT.
