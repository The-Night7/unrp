# Strong Kick

Sources : `powers/strong_kick.json`, `events/EarlyPowerEvents`, `events/AbilityEvents` (Ground Smash).

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +0,05 vitesse de déplacement, +0,5 puissance de saut | +0,05 de vitesse, `palladium:jump_power` +0,5 | ✅ |
| **Coup de hache** (X) | 14 aura, recharge 160 ; 5 à 11 de dégâts selon le niveau dans une zone devant soi, ralentit 60 ticks (II) | identique | ✅ |
| **Balayage** (C, rang 2) | 16 aura, recharge 200 ; tout dans 3 x 1,2 x 3 est poussé (0,42) et décollé (0,35), ralentissement III 50 ticks, pas de dégâts | identique | ✅ |
| **Frappe au sol** (V, rang 3) | exige 75 aura, en retire 40, recharge 400 ; bond (1,85), plongeon vers le regard, onde de rayon 8, force 3, 10 dégâts ; pas de dégâts de chute | identique ; la vitesse de plongeon est reconstituée | ≈ |
| Poses | `sk_kick`, `sk_dive`, `sk_land` | `spd_dash` | 🎨 |
