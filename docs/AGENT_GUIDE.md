# Guide de travail

Lire les documents du dossier `docs/` avant de modifier le code.

Commande minimale avant pull request :

```bash
gradle projectCheck
```

Branches recommandees : `agent/<numero>-<sujet>`.

Repartition :

- `app/` : interface Android telephone.
- `wear/` : interface Galaxy Watch.
- `core/` : modeles, protocole et calculs.

Ne pas ajouter de media d'exercice sans licence claire. Ne pas presenter les estimations calories ou metabolisme comme donnees medicales. Ajouter des tests quand une formule change.
