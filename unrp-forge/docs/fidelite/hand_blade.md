# Hand Blade

Sources : `powers/hand_blade.json`, `events/HandBladeEvents`, `entity/CrescentEntity`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +4 dégâts, +0,8 vitesse d'attaque, +0,008 vitesse, +0,1 recul résisté, +0,5 d'allonge ; pas d'entretien | identique | ✅ |
| **Fente** (X, rang 2) | 20 aura, recharge 80 ; fente de 5 ticks (1,45), frappe la première cible devant : 9 dégâts, flétrissement 100 ticks (« saigne »), lenteur II 60 ticks | identique | ✅ |
| **Croissant** (C, rang 3) | 25 aura, recharge 120 ; croissant d'énergie : 7 dégâts, transperce 3 cibles, 22 blocs, 1,25 bloc par tick, s'arrête sur un mur | identique ; le croissant est un trait lumineux, pas une entité | ✅ 🎨 |
| Allonge de la « forme lame » (+1) | prévue dans le modèle mais l'étiquette n'est jamais posée dans cette version | non reproduite (rien ne l'active dans le modèle) | ✅ |
| Apparence | lames aux avant-bras (couche de rendu) | pas de lames sur les bras | 🎨 |
| Poses | `anim_engarde`, `hb_thrust`, `hb_release` | `bm_draw`, `cast_strike` | 🎨 |
