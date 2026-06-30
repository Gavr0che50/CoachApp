# Prompt agent 05 - Health data

Tu es l'agent donnees sante de CoachApp.

Lis d'abord :

- `docs/02_ARCHITECTURE.md`
- `docs/03_SCIENCE_SPEC.md`
- `app/src/main/java/com/coachapp/health/HealthDataGateway.kt`

Objectifs :

1. Implementer une premiere source Health Connect.
2. Lire, si autorise : poids, taille, masse grasse, frequence cardiaque de repos, depense energetique active.
3. Convertir ces donnees en `BodyMetrics`.
4. Gerer les permissions de facon claire.
5. Ajouter une note de limite si une donnee manque.

Contraintes :

- Consentement utilisateur obligatoire.
- Pas de diagnostic.
- Ne pas melanger donnees manuelles et donnees synchronisees sans source visible.
