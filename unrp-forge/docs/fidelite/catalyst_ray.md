# Catalyst Ray

Sources : `powers/catalyst_ray.json`, `events/CatalystRayEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Catalyse** (X) | recharge 30 ; mutation au hasard sur la cible (accroupi : soi, les bonnes pèsent x3 et les mauvaises /2) ; 20 mutations (9 bonnes, 3 neutres, 8 mauvaises) avec poids ; durée 400 ticks + 60 par point de Ruse | identique (mêmes 20 mutations, mêmes poids, même durée) | ✅ |
| Surcharge | à la capacité (3, ou le niveau pour un joueur avec pouvoir ; illimitée pour un Catalyst Ray) : dégâts 6, lenteur II, cécité 100 ticks, pouvoir coupé 200 ticks | identique | ✅ |
| Mutations spéciales | os de verre (+20 % de dégâts reçus), vol de vie (25 % des dégâts rendus), toucher brûlant (feu 3 s), second souffle (une mort évitée, à moitié de vie), pas déphasé (saut de 4 blocs, 1 fois sur 5 toutes les 40 ticks), instable (1 fois sur 4 toutes les 40 ticks : une nouvelle mutation) | identique | ✅ (fréquences de pas déphasé et d'instable ≈) |
| **Table rase** (C) | recharge 140 | efface mutations et effets | ✅ |
| **Stase génétique** (V) | recharge 260 ; capacité suspendue | pouvoir du joueur suspendu 10 s | ✅ |
| **Imprégnation** (B) | recharge 320 ; exige exactement une mutation | identique | ✅ |
