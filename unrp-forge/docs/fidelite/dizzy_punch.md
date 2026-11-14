# Dizzy Punch

Sources : `powers/dizzy_punch.json`, `events/DizzyPunchEvents`, `events/EarlyPowerEvents` (Haymaker tournoyant).

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | -1 aura toutes les 10 ticks | identique | ✅ |
| Éclat d'ivresse | 3 coups de suite sur la même cible (fenêtre 100 ticks, 6 cibles suivies au plus) : +2 dégâts, nausée 100 ticks, lenteur 40 ticks, immunité 120 ticks | identique | ✅ |
| **Haymaker tournoyant** (X) | 14 aura, recharge 240 ; prochain coup 100 ticks : nausée 100 ticks, puis effet « Désorienté » 50 ticks (immunité 290 ticks) | identique ; « Désorienté » inverse les touches de déplacement | ✅ |
| **Coup bas** (C, rang 2) | 6 aura, recharge 60 ; élan de 0,55 vers l'avant, cible dans un cône à 3 blocs : 3 dégâts, 4,5 si elle te regardait (nausée 100 + lenteur 30, immunité 100) | identique | ✅ |
| **Tissage** (V, rang 3) | 12 aura, recharge 240 ; 120 ticks : vitesse, coups de mêlée reçus x0,75, l'attaquant a nausée 60 + lenteur 20 | identique | ✅ |
| Poses | `dz_spin`, `dz_sucker` | `cast_strike`, `cast_buff` | 🎨 |
