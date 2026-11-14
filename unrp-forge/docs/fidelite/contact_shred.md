# Contact Shred

Sources : `powers/contact_shred.json`, `events/ContactShredEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +9 % de vitesse ; -1 aura / 20 ticks | identique | ✅ |
| Marque mineure | un coup de poing sur une cible marque (20 ticks) ; une marque par cible tous les 40 ticks ; elle se déchire : 2,5 à 4 dégâts selon le niveau | identique | ✅ |
| **Étreinte lacérante** (X) | 14 aura, recharge 140 ; cible à 3 blocs : 2 à 4 dégâts, marque forte (30 à 50 ticks selon le niveau) qui grossit de 1 toutes les 10 ticks ; elle se déchire à la fin ou quand tu lâches (pouvoir coupé, mort) : 4 à 7 + charge x 1,25 à 1,75, lenteur 40 ticks | identique | ✅ |
| **Toucher griffu** (C, rang 2) | 8 aura, recharge 60 ; 8 ticks de ruée (1,15) : chacun touché une fois, 3 à 5,5 dégâts, marque de 30 ticks (déchirure 3,5 à 5,5 + charge) | identique | ✅ |
| Priorité des marques | une marque plus forte n'est pas écrasée par une plus faible | identique | ✅ |
| Apparence | mains griffues (couche de rendu), anneaux de particules | braises rouges sur la cible | 🎨 |
| Poses | `anim_grip`, `anim_rakeup`, `anim_rakedown` | `cast_strike`, `spd_dash` | 🎨 |
