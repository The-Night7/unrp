# Barrier (Arlo)

Sources dans le modèle : `powers/barrier.json`, `events/BarrierEvents`, `entity/BarrierEntity`, poses `br_*`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | interrupteur, +6 d'armure | identique | ✅ |
| **Dresser une barrière** (X) | 15 aura, recharge 40 ticks | identique | ✅ |
| ↳ normal | sphère autour de soi ; une seule à la fois, la nouvelle remplace l'ancienne | identique | ✅ |
| ↳ accroupi + viser une de ses barrières (28 blocs) | rappel : elle disparaît et l'aura est rendue | identique (lancer gratuit) | ✅ |
| ↳ accroupi ailleurs | barrière externe à l'endroit visé (24 blocs), 2 au maximum | identique ; la plus ancienne disparaît s'il y en a déjà 2 | ≈ (le modèle bloque l'emplacement un court moment) |
| Taille, PV | selon le niveau et les stats | rayon 2,5 ; PV selon Récupération et Défense | ≈ |
| **Agrandir** (C) | 6 aura, recharge 8 ; +0,75 de taille | identique | ✅ |
| ↳ accroupi | contraction de 0,75 ; ceux à l'intérieur sont écrasés (dégâts + lenteur 30 ticks) | identique | ✅ (dégâts ≈) |
| ↳ sans barrière | « No active barriers. » | « Aucune barrière active. » | ✅ |
| **Bélier** (V) | 20 aura, recharge 300 ; élan 1,2 pendant 10 ticks derrière un bouclier | identique | ✅ |
| ↳ impact | dégâts ≈ 6, recul 0,9, soulève 0,35 | identique | ≈ |
| Attaque d'un côté vers l'autre | annulée, la barrière encaisse | identique | ✅ |
| Projectiles | arrêtés | renvoyés vers le tireur | ≈ (comportement du webtoon) |
| Frapper une barrière | elle perd des PV | coup de poing dans le vide qui traverse sa paroi : elle encaisse ta force d'attaque | ✅ |
| Barrière brisée | le lanceur est blessé | identique (4 dégâts) | ≈ |
| Coût d'entretien | aura retirée régulièrement par barrière | -1 aura toutes les 40 ticks par barrière | ≈ |
| Plus d'aura / pouvoir coupé / mort | les barrières disparaissent | identique | ✅ |
| Pose à l'activation | `anim_ward` (bras tendus) | `br_ward` | 🎨 |
| Poses des capacités | `br_ward`, `br_point`, `br_grow`, `br_shrink`, `br_ram` | les mêmes, redessinées | 🎨 |
| Rendu de la barrière | entité avec modèle et texture | sphère lumineuse quadrillée, fissures, éclats | 🎨 |
| Sons | sons propres au modèle | sons Minecraft (améthyste, verre) | 🎨 |
| Déblocage progressif (`unlock_s2`...) | Agrandir et Bélier se débloquent | tout est disponible tout de suite | ❌ |
