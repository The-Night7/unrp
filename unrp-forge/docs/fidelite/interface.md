# Interface du modèle

| Élément | Modèle | Ici | Statut |
|---|---|---|---|
| Barre de capacités | barre de Palladium (AbilityBarRenderer), texture `ability_bars/unrp_powers.png`, 5 cases par liste, case 0 = interrupteur, cases partagées entre postures | la même, reproduite (`look/AbilityBar`), cases (`bar_slot`), icônes et couleurs de cadre reprises des fiches du modèle | ✅ |
| Touches | celles de Palladium : V B N M , ; X change de liste (accroupi : en arrière) ; G = roue | les mêmes (touches de Palladium, roue G) ; R et X C V B N supprimées | ✅ |
| HUD | UnrpHudOverlay (vie, aura, faim, armure, tête), vanilla masqué | le même (`look/UnrpHudOverlay`) | ✅ |
| Yeux de pouvoir | CustomEyesLayer + PowerEyes | les mêmes (`look/`), réglages lus dans l'onglet Style | ≈ (le style « classique » de l'onglet Style n'existe pas dans le modèle ; il s'affiche en « aiguisé ») |
| HUD des pactes | PactHudOverlay | le même | ✅ |
| Hub | HubScreen du modèle (écoles, groupes, territoires, duels, arbre de capacités...) | notre hub | ❌ à porter |
| Copies d'Aura Manipulation / pouvoirs accordés | chacun a ses listes, tous actifs en même temps | pareil : un état par pouvoir détenu (`AbilityEngine.OTHERS`, `withPower`), chaque liste vise son pouvoir, tous restent allumés | ✅ |
