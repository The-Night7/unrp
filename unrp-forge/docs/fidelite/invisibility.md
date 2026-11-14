# Invisibility (Terrence)

Sources dans le modèle : `powers/invisibility.json`, `events/InvisibilityEvents`, pose `anim_wrap`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | +6 % de vitesse, meilleur saut | identique (`palladium:jump_power` +0,15) | ✅ |
| **Cape optique** (X, interrupteur) | invisible, -1 aura / 8 ticks | identique | ✅ |
| ↳ chutes | dégâts de chute divisés par 2 | identique | ✅ |
| ↳ monstres | te repèrent à 10 % de la distance normale | identique | ✅ |
| ↳ attaquer | te rend visible 20 ticks (« attacking only blurs you ») | identique | ✅ |
| ↳ aura vide | « Your cloak fades as your aura empties... » | la cape s'éteint avec le pouvoir | ✅ |
| **Cape partagée** (C) | 20 aura, recharge 120 ; l'allié visé (même groupe ou faction, 12 blocs) | même groupe ou même école, 12 blocs | ✅ |
| ↳ durée, nombre | durée et nombre d'alliés selon le niveau | 10 s (allongée par la Ruse), 4 alliés au maximum | ≈ |
| ↳ messages | « You've been cloaked », « Your borrowed cloak fades. » | traduits | ✅ |
| Pose | `anim_wrap` | `inv_cloak`, `inv_share` | 🎨 |
| Rendu | distorsion du personnage | léger miroitement | 🎨 |
| Déblocage progressif | Cape partagée à débloquer | rang 2, 3, 4 : 1, 2, 3 points d'aptitude (1 par niveau), commun à tous les pouvoirs ; chaque capacité exige sa parente | ✅ |
