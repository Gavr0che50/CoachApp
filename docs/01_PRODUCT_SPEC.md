# 01 - Product spec

## MVP telephone

- Accueil : programme du jour, duree cible, nombre total de series.
- Seance : exercice courant, series restantes, charge prevue, repos.
- Resume : series validees, volume, calories estimees, comparaison avec la derniere seance identique.
- Profil : poids, taille, masse grasse, IMC, BMR estime, source des donnees.

## MVP Galaxy Watch

- Afficher l'exercice courant.
- Afficher la serie courante.
- Valider une serie.
- Augmenter ou baisser la charge par pas configurable.
- Afficher le timer de repos.
- Terminer la seance.

## Notification de fin

Contenu minimum :

- `Seance terminee`.
- Volume total en kg.
- Difference de volume versus derniere seance comparable.
- Calories estimees.
- Record battu ou encouragement.
- Conseil pour la prochaine fois.

## Exemples de messages

Record battu :

```text
Bravo. +420 kg de volume versus la derniere seance Push. Calories estimees : 185 kcal.
```

Record non battu :

```text
Seance solide. Volume stable. Prochaine piste : garder la meme charge et viser +1 rep sur la derniere serie.
```
