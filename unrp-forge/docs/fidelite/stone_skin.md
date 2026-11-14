# Stone Skin

Sources : `powers/stone_skin.json`, `events/EarlyPowerEvents`, `kubejs/aura_system.js`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +16 armure, +3 dégâts d'attaque, +0,02 vitesse ; -1 aura toutes les 8 ticks | identique | ✅ |
| **Durcir** (X) | 12 aura, recharge 240 ; 80 ticks : dégâts subis réduits de 60 à 85 % selon le niveau, 1,5 renvoyé à l'attaquant, pas de course | identique | ✅ |
| **Immobile** (C, rang 2, interrupteur) | -1 aura / 20 ticks ; lenteur X, saut bloqué, pas de course, dégâts x0,75 ; rien ne te déplace | identique ; recul annulé par un attribut au lieu d'un test | ≈ |
| Apparence | statue de pierre (rendu et sons du modèle) | pas de peau de pierre ; éclats de pierre | 🎨 |
| Poses | `anim_harden`, `anim_immovable` | `cast_buff` | 🎨 |
