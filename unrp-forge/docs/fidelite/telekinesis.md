# Telekinesis

Sources : `powers/telekinesis.json`, `events/TelekinesisEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +6 armure ; chutes à 70 % | identique | ✅ |
| **Pression** (X, maintenir) | écrase la cible visée ; accroupi : champ d'écrasement | identique (14 blocs, rayon 4) | ≈ |
| **Manipulation** (C) | recharge 120 ; soulève une créature (120 ticks) ou un bloc (400 ticks) ; de nouveau : lancer ; accroupi : reposer ; blocs interdits refusés | identique | ✅ (dégâts ≈) |
| **Orbite** (V) | recharge 120 ; objets en orbite qui repoussent ce qui les touche | roches en orbite 10 s, repoussent et blessent | ≈ |
| **Domination absolue** (B) | expulsion vers le ciel ; accroupi : écrasement au sol avec lenteur | identique (12 blocs) | ✅ (dégâts ≈) |
| Résistance au recul | +0,3 pouvoir allumé (+0,3 de plus pendant l'Orbite) ; ce n'étaient pas des coups renforcés, la fiche se trompait | identique | ✅ |
| Poses | `anim_crush`, `anim_orbit` | `tk_crush`, `tk_lift`, `tk_orbit`, `tk_dominion_up`, `tk_slam` | 🎨 |
| Déblocages | capacités 2 à 4 | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
