# Predator

Sources : `powers/predator.json`, `events/PredatorEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +2 dégâts, +0,015 vitesse, +0,4 vitesse d'attaque, +0,15 recul résisté | identique | ✅ |
| **Verrouillage** (X) | 6 aura, recharge 40 ; cible la plus alignée (cône serré) à 40 à 60 blocs (+5 transformé) ; tes coups sur elle : x1,2 à x1,5 ; perdue au-delà de 60 blocs | identique ; la vision à travers les murs de la proie est une lueur | ≈ |
| **Sens aiguisé** (C, rang 2, maintenir) | 10 aura, -1 / 8 ticks ; les êtres vivants à 24 blocs et plus brillent, tu es ralenti II | identique | ✅ |
| **Ruée du prédateur** (V, rang 3) | 20 aura, recharge 140 ; sur la cible verrouillée à moins de 20 blocs : élan 1,7, à moins de 2,5 blocs : 8 à 14 dégâts, recul 1,2, lenteur IV et faiblesse II 40 ticks ; sans cible : bond de 1,25 | identique | ✅ |
| **Brise-sol** (B, rang 4) | 18 aura, recharge 180 ; une onde de 12 blocs suit le relief : 5 dégâts et plus, projette (0,55), lenteur II 60 ticks | identique | ✅ |
| **Transformation** (N, rang 4, interrupteur) | -1 aura / 8 ticks ; dégâts +3, vitesse +0,02, recul +0,25 | identique | ✅ |
| Rendu | moteur d'effets (ClientPassPd), transformation (PredatorTransLayer), réticule (PredatorClientHandler) | les mêmes, portés | ✅ |
