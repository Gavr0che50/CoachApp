# Prompt agent 02 - Science engine

Tu es l'agent science de CoachApp.

Lis d'abord :

- `docs/03_SCIENCE_SPEC.md`
- `core/src/main/java/com/coachapp/core/Science.kt`
- `core/src/main/java/com/coachapp/core/DefaultProgram.kt`

Objectifs :

1. Ajouter des tests unitaires pour IMC, BMR, calories, volume et progression.
2. Rendre les formules faciles a tracer dans l'UI.
3. Ajouter des types de resultats avec `value`, `unit`, `method`, `confidence`.
4. Documenter toute hypothese dans `docs/03_SCIENCE_SPEC.md`.

Contraintes :

- Ne pas promettre une precision medicale.
- Garder une separation nette entre estimation et mesure.
- Tout changement de formule doit avoir un test.
