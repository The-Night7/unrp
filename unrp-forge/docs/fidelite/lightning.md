# Lightning (Remi)

Sources dans le modèle : `powers/lightning.json`, `events/LightningEvents`, poses `lt_*`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +12 % de vitesse, -1 aura / 7 ticks | identique | ✅ |
| ↳ à l'activation | la foudre tombe sur toi (LT_ON ; sous un toit : version abritée) | identique, moteur d'effets du modèle | ✅ |
| ↳ sens électrique | tant qu'il est actif, les créatures à 14 blocs brillent | identique | ✅ |
| ↳ riposte | qui te frappe au corps à corps prend des dégâts et ralentit, une fois par seconde et par attaquant | identique (3 dégâts) | ≈ |
| **Éclair** (X) | 12 aura, recharge 36 ; projectile vitesse 2, durée 15 | identique | ✅ |
| ↳ impact | dégâts 6 à 11 selon le niveau ; lenteur et fatigue 30 ticks ; recul 0,3 | dégâts 6 × Puissance ; lenteur et fatigue 1,5 s ; recul 0,3 | ≈ |
| **Contact électrique** (C) | 10 aura, recharge 50 ; la cible la plus proche devant (3 blocs) | identique | ✅ |
| ↳ effet | dégâts 7 à 12 ; lenteur + faiblesse 40 ticks ; recul 0,4 ; soulève 0,25 | dégâts 7 × Puissance ; le reste identique | ≈ |
| **Course foudroyante** (V, maintenir) | +60 % de vitesse, course forcée droit devant à 1,2, -1 aura / 5 ticks | identique | ✅ |
| **Rayon foudroyant** (B, maintenir) | rayon d'énergie, portée 30, -1 aura / 3 ticks | identique | ✅ (dégâts ≈) |
| Poses | `lt_call`, `lt_cast`, `lt_touch`, `lt_surge`, `lt_beam` | les mêmes, redessinées | 🎨 |
| Rendu | moteur d'effets « Pass » (ClientPass) : éclair, impacts, contact, riposte, course, rayon, sons | le même moteur, porté (`pass/`), alimenté par nos capacités | ✅ |
| Déblocage progressif | capacités 2 à 4 à débloquer | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
