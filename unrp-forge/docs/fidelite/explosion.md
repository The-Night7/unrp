# Explosion

Sources : `powers/explosion.json`, `events/ExplosionEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Activation (R) | immunité aux explosions et à leur recul | identique | ✅ |
| **Détonation** (X, maintenir) | 8 aura, -1 / 10 ticks, charge 200 ticks ; explosion là où tu regardes (12 blocs, première cible ou mur) : rayon 2,5 à 10 (x1 à 1,4 selon le niveau), dégâts min(14, (7 à 11) x (1 + charge)), recul (1,2 à 2,8) x (1 + 0,5 x charge) ; recharge 30 à 120 ; un coup reçu interrompt | identique | ✅ |
| **Onde explosive** (C, rang 2) | 35 aura, recharge 140 ; autour de toi : rayon 10, 12 à 14 dégâts, recul 2,0 à 4,5 | identique | ✅ |
| **Détonation en grappe** (V, rang 3, maintenir) | une petite explosion toutes les 12 ticks pour 10 aura (rayon 1,75, 3 à 7 dégâts, recul 1,4 à 2,8) tant qu'il reste 10 aura | identique | ✅ |
| Dégâts d'explosion | source « explosion » attribuée au joueur, recul `push` | identique | ✅ |
| Rendu | particules du modèle | flammes et onde | 🎨 |
