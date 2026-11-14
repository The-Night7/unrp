# Quick Strike

Sources : `powers/quick_strike.json`, `events/QuickStrikeEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +0,25 vitesse d'attaque, +0,008 vitesse ; +0,15 de résistance au recul en courant | identique | ✅ |
| **Fente** (X) | 12 aura, recharge 70 ; fente de 8 ticks (1,5 en avant), frappe la première cible dans un cône (2,4 blocs) : 7 dégâts, lenteur III 20 ticks, tu t'arrêtes | identique | ✅ |
| **Posture d'assaut** (C, rang 2) | 10 aura, recharge 2200 ; 400 ticks : +1,2 vitesse d'attaque, +0,03 vitesse, +0,3 recul résisté ; s'arrête si l'aura tombe à 0 | identique | ✅ |
| **Combo** (V, rang 3) | 20 aura, recharge 140 ; cible dans un cône (3,5 blocs), 5 coups : 4 x 1,6 dégâts (lenteur IV 6 ticks) puis 5 dégâts et recul 1,1 ; les coups passent à travers l'invulnérabilité | rythme (un coup tous les 2 ticks) et reprise de cible reconstitués | ≈ |
| **Poursuite** (B, rang 4) | 8 aura, recharge 50 ; cible dans un cône (12 blocs) : élan vers elle puis vraie attaque de mêlée à moins de 2,6 blocs ; sinon élan de fuite (1,3) | élan reconstitué, attaque identique | ≈ |
| Poses | `anim_stance`, `qs_*` | `spd_dash`, `cast_buff`, `cast_strike` | 🎨 |
