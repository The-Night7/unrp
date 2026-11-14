# Conjure Vines

Sources : `powers/conjure_vines.json`, `events/VinesEvents`, `entity/VineEntity`, `entity/SpikeEntity`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Fouet de lianes** (X, maintenir) | 4 aura ; une liane suit ton regard (14 blocs) ; au bout : 4 dégâts toutes les 12 ticks ; elle saisit le premier ennemi sur la ligne de mire et le porte à 4,2 blocs devant toi (saut bloqué, 1,5 dégât toutes les 30 ticks) | identique | ✅ |
| **Entrave** (C, rang 2) | 24 aura, recharge 160 ; zone visée (18 blocs, rayon 3) : ralentissement IV, saut bloqué, lueur 100 ticks | identique | ✅ |
| **Prison d'épines** (C accroupi) | 26 aura, recharge 400 ; 10 pieux en cercle (rayon 4,5) pendant 200 ticks : ce qui est dans l'anneau est repoussé vers l'intérieur (1,5 dégât toutes les 10 ticks), ce qui est au centre est ligoté toutes les 40 ticks | identique | ✅ |
| **Épines** (V, rang 3) | 22 aura, recharge 140 ; cinq épines en éventail (4 dégâts, courbent vers le regard) ; accroupi : quatre pieux au sol (7 dégâts, étourdit 30 ticks) | identique | ✅ |
| **Étreinte** (B, rang 4, maintenir) | 6 aura ; champ de lianes qui grandit (rayon 2 à 6) : toutes les 8 ticks ralentit IV, bloque le saut, 1 dégât, 45 % de ligoter | identique | ✅ |
| Rendu | entités liane et pieu avec modèle | traits et feuilles | 🎨 |
