# Missiles

Sources : `powers/missiles.json`, `events/MissilesEvents`, `entity/MissileEntity`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Missile** (X, maintenir) | 14 aura au départ, -1 / 10 ticks ; charge jusqu'à 60 ticks ; explosion 1,0 à 2,2, vitesse 1,0 à 1,5 ; recharge 30 à 50 ; un coup reçu interrompt | identique | ✅ |
| Vol du missile | durée 90 ticks, trajectoire qui revient vers le regard (v x 0,96 + regard x 0,1), coup direct 4 + souffle x 2,5, explosion sans casser les blocs | identique | ✅ |
| **Jumeaux** (C, rang 2) | 28 aura, recharge 120 ; deux missiles (souffle 2,0, vitesse 1,15) ; accroupi : +10 aura, quatre missiles en gerbe (-28, -10, 10, 28 degrés, souffle 1,8) qui reviennent vers le regard | identique | ✅ |
| **Rafale** (V, rang 3) | 10 aura, un missile (souffle 1,6, vitesse 1,2) toutes les 5 ticks pour 2 aura, tir légèrement dispersé, s'arrête sans aura | identique | ✅ |
| **Ogive** (B, rang 4) | 60 aura, charge 80 ticks ; souffle 2,2 à 3,5, vitesse 0,9 ; recharge 280 à 360 | identique | ✅ |
| Rendu | entité missile avec modèle | particules de braise et traînée | 🎨 |
