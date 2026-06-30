# 03 - Science spec

CoachApp doit etre precis dans ses calculs, mais rester une app fitness. Les valeurs sont des estimations et doivent toujours afficher leur source.

## IMC

```text
IMC = poids_kg / taille_m^2
```

Usage : indicateur general, pas une evaluation individuelle de composition corporelle.

## Metabolisme basal

Formule par defaut si sexe et age connus : Mifflin-St Jeor.

```text
Homme  = 10 * poids_kg + 6.25 * taille_cm - 5 * age + 5
Femme  = 10 * poids_kg + 6.25 * taille_cm - 5 * age - 161
```

Si la masse grasse est fiable, l'app peut aussi calculer Katch-McArdle :

```text
masse_maigre_kg = poids_kg * (1 - masse_grasse_pct / 100)
BMR = 370 + 21.6 * masse_maigre_kg
```

## Calories musculation

MVP : estimation par MET.

```text
kcal = MET * 3.5 * poids_kg / 200 * minutes
```

Valeur par defaut : 5 MET pour une seance de musculation moderee a soutenue. L'app doit afficher `estimation` et non `mesure exacte`.

## Progression

Regle de depart : double progression.

- Chaque exercice a une plage de repetitions.
- Si toutes les series atteignent le haut de plage avec au moins 2 repetitions en reserve, proposer +2.5 kg au prochain passage.
- Sinon, conserver la charge et viser plus de repetitions.
- Si la technique est marquee comme mauvaise ou douleur signalee, ne pas augmenter.

## Tests requis

- IMC avec taille en cm.
- BMR homme/femme.
- BMR masse maigre.
- Calories par MET.
- Volume total.
- Suggestion de charge suivante.
