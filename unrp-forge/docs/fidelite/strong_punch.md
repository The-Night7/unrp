# Strong Punch

Sources : `powers/strong_punch.json`, `events/EarlyPowerEvents`, `events/AbilityEvents` (Ground Smash).

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +4 dégâts d'attaque, +1 recul d'attaque | identique | ✅ |
| **Haymaker** (X) | 14 aura, recharge 160 ; arme le prochain coup 100 ticks : +8 à +16 de dégâts selon le niveau, la cible est projetée (1,6 en avant, 0,5 en l'air) | identique | ✅ |
| **Bris de garde** (C, rang 2) | 18 aura, recharge 240 ; prochain coup 100 ticks : bouclier cassé, la cible reçoit ×1,25 au prochain coup 100 ticks | identique | ✅ |
| **Frappe au sol** (V, rang 3) | exige 75 aura, en retire 40, recharge 300 ; 4 ticks plus tard, onde de rayon 14, force 3,5, 8 dégâts | identique | ✅ (rayon de recherche lu tel quel dans le modèle) |
| Niveau | dégâts du Haymaker selon le niveau (1 à 10) | identique | ✅ |
| Poses | `anim_*` du modèle | `cast_strike`, `cast_blast` | 🎨 |
| Sons, éclats, secousse | sons et effets propres au modèle | sons Minecraft, nos particules | 🎨 |
