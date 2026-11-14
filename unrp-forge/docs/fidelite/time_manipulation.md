# Time Manipulation

Sources : `powers/time_manipulation.json`, `events/TimeManipulationEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | pose `tm_call` ; recul subi à 65 % | identique | ✅ |
| **Changer de posture** | utilitaire ↔ offensive | identique | ✅ |
| **Flux du temps** (interrupteur) | utilitaire : vitesse, hâte, saut ; offensive : lenteur, fatigue, faiblesse autour (8 blocs) | identique | ✅ (coût ≈) |
| **Retour en arrière** (utilitaire) | recharge 300 ; 3 s en arrière ; un allié (32 aura) ; effets négatifs effacés | identique | ✅ |
| **Décalage temporel** (utilitaire) | recharge 140 ; bond en avant ; rappel au point retenu | identique (8 blocs) | ✅ |
| **Temps figé** (offensive) | recharge 400 ; créatures et projectiles figés 40 à 80 ticks | identique ; les projectiles restent figés | ≈ |
| **Frappe temporelle** (offensive) | 50 aura, recharge 520 ; élan cumulé (momentum) | tu surgis derrière la cible et frappes ; sans élan cumulé | ≈ |
| **Coup différé** (offensive) | 40 aura, recharge 360 ; coup 60 ticks plus tard | identique | ✅ |
| Poses | `anim_clock`, `anim_counter` | 6 poses `tm_*` | 🎨 |
| Rendu | moteur d'effets (ClientPassTm) : flux, retour, saut, marque, temps figé en gris, frappe, coup différé, engrenages | le même moteur, porté | ✅ |
| Déblocages | capacités à débloquer | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
