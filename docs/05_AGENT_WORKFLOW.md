# 05 - Workflow des agents

## Cycle standard

1. Lire `docs/AGENT_GUIDE.md`.
2. Lire la spec liee a la tache.
3. Creer une branche courte.
4. Modifier peu de fichiers.
5. Ajouter ou mettre a jour les tests.
6. Lancer `gradle projectCheck`.
7. Ouvrir une PR avec resume, validation, limites.

## Format PR

```md
## Objectif

## Changements

## Validation

## Limites / TODO
```

## Ordre recommande

1. Stabiliser build + wrapper Gradle.
2. Ajouter tests `core`.
3. Implementer stockage Room.
4. Implementer vrai flow de seance telephone.
5. Implementer Data Layer telephone <-> montre.
6. Ajouter timer de repos sur montre.
7. Ajouter Health Connect.
8. Ajouter integration Samsung Health Data SDK si necessaire.
9. Ajouter notification de resume.
10. Ajouter assets d'exercices licencies.
