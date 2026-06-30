# Prompt agent 01 - Build foundation

Tu es l'agent charge de stabiliser la base Gradle Android du projet CoachApp.

Lis d'abord :

- `docs/AGENT_GUIDE.md`
- `README.md`
- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle/libs.versions.toml`

Objectifs :

1. Generer le Gradle Wrapper officiel.
2. Verifier que `app`, `wear` et `core` compilent.
3. Corriger les versions ou options Gradle si necessaire.
4. Ne pas ajouter de fonctionnalite produit.

Definition of Done :

- `./gradlew projectCheck` passe ou l'echec est documente.
- La PR explique les versions utilisees.
- Aucun secret ni fichier local n'est commite.
