# Energy Discharge

Sources : `powers/energy_discharge.json` (rayon et sphère en données), `events/EnergyWaveEvents`, `events/AbilityEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Rayon** (X, maintenir) | -1 aura / 3 ticks ; rayon long de 32 blocs, 1 dégât à la première cible touchée | identique (dégâts limités par l'invulnérabilité, comme Palladium) | ≈ |
| **Onde d'énergie** (X, accroupi à l'appui) | après 10 ticks, le rayon s'allonge de 25 % par tick ; 2 dégâts à tout ce qui est sur la ligne (rayon 1,5) ; -2 aura / 3 ticks | identique | ✅ |
| **Sphère d'énergie** (C, rang 2) | exige 150 aura, en retire 50, recharge 300 ; vitesse 1,5, gravité 0,01, 100 ticks, taille 1,6, 20 dégâts, explosion de 2 sans casser | identique | ✅ |
| **Onde de choc** (V, rang 3) | exige 100 aura, en retire 40, recharge 400 ; 6 ticks plus tard, tout à 12 blocs est repoussé (force 28), sans dégâts | identique | ✅ |
| **Propulsion** (B, rang 4) | exige 75 aura, en retire 30, recharge 100 ; élan de 2,5 vers le regard, pas de dégâts de chute (jusqu'à l'atterrissage ou 100 ticks) | identique | ✅ |
| Rendu | moteur d'effets (ClientPassEd) : rayon, onde, sphère, explosion sans rendu vanilla, propulsion | le même moteur, porté | ✅ |
