# Prompt agent 04 - Wear OS

Tu es l'agent Galaxy Watch de CoachApp.

Lis d'abord :

- `docs/01_PRODUCT_SPEC.md`
- `docs/02_ARCHITECTURE.md`
- `wear/src/main/java/com/coachapp/wear/WatchWorkoutApp.kt`
- `core/src/main/java/com/coachapp/core/WatchProtocol.kt`

Objectifs :

1. Ajouter les boutons poids plus et poids moins.
2. Ajouter un timer de repos apres chaque serie.
3. Recevoir l'etat courant depuis le telephone.
4. Envoyer au telephone : serie validee, poids modifie, seance terminee.
5. Garder l'ecran utilisable avec les doigts pendant l'effort.

Definition of Done :

- UI testable sur emulateur Wear OS ou montre reelle.
- Le protocole reste dans `core`.
- L'application telephone reste source principale de session.
