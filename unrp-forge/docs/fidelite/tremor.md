# Tremor

Sources : `powers/tremor.json`, `events/TremorEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | dégâts de chute x0,7 | identique | ✅ |
| **Séisme** (X, maintenir) | 4 aura, -1 / 9 ticks ; exige le sol, tu es immobilisé ; toutes les 8 ticks : rayon 11 (18 en magnitude), 1,5 dégât (2,5), recul 0,25, lenteur 12 ticks | identique | ✅ |
| **Faille** (C, rang 2) | 24 aura (rendue sans sol), recharge 160 ; après 4 ticks, une onde avance de 1,2 par tick sur 14 blocs (20 en magnitude), suit le relief ; 6 dégâts (8), projette (0,45), lenteur III 20 ticks ; une cible touchée au plus toutes les 15 ticks | identique | ✅ |
| **Magnitude** (V, rang 3) | 6 aura, recharge 20 ; bascule : tout frappe plus fort, -1 aura / 18 ticks ; s'effondre sans aura | identique | ✅ |
| **Éclat sismique** (B, rang 4) | 34 aura, recharge 240 ; exige le sol ; 7 ticks immobile puis anneau de 16 blocs (22), 7 dégâts (9), recul 1,3, lenteur II 20 ticks | identique | ✅ |
| **Cataclysme** (B accroupi) | 34 + 18 aura, recharge 420 ; 20 ticks de montée puis 5 ondes toutes les 12 ticks (rayon 5, 10, 15, 20, 25 ; 13, 10, 8, 6,5, 5 dégâts), lenteur V 60 ticks | identique | ✅ |
| Secousse d'écran des autres joueurs | oui | non reproduite (paquet client) | ❌ |
| Rendu | poussière de bloc | éclats de roche | 🎨 |
