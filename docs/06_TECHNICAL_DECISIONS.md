# 06 - Technical decisions

## Android stack

- Kotlin + Gradle Kotlin DSL.
- Jetpack Compose Material 3 for phone UI.
- Wear Compose for Galaxy Watch UI.
- Google Play Services Wearable for phone/watch communication.
- Health Connect first for health permissions and common fitness data.

## Samsung data

Use Samsung Health Data SDK only behind `HealthDataGateway` when Health Connect does not expose the needed data. The older Samsung Health Android SDK must not be used for new work.

## Exercise media

Assets in `DefaultProgram` are placeholders. Agents must replace them only with owned or clearly licensed media.

## Precision policy

CoachApp can be precise in formulas and comparisons, but calories and metabolism remain estimates. The UI must always expose the method used.
