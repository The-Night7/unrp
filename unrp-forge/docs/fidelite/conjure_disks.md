# Conjure Disks

Sources : `powers/conjure_disks.json`, `events/ConjureDisksEvents`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| Réserve de disques | 3 disques (niveau < 3,0), 4 (< 6,0), 5 (< 8,5), 6 ensuite | identique | ✅ |
| **Disques en orbite** (X, interrupteur) | réserve 2 disques ; armure +6, vitesse -5 % ; -1 aura / 6 ticks ; dégâts subis -16 à -28 % selon le niveau (sauf dégâts qui ignorent l'armure) ; projectiles qui arrivent de face renvoyés pour 3 aura | identique (étiquette de dégâts « ignore l'armure » supposée) | ≈ |
| **Lancer un disque** (C, rang 2) | 12 aura, recharge 40 ; ligne de 28 blocs, première cible : 7 à 11 dégâts, recul 0,9 ; le disque se reforme en 280 à 180 ticks selon le niveau ; sans disque libre : refusé | identique | ✅ |
| **Aéroglisseur** (V, rang 3, interrupteur) | réserve 1 disque ; -1 aura / 5 ticks ; vol de survie, vitesse +30 % ; chute amortie à la fin | identique ; vol Palladium, `palladium:flight_speed` +0,45 (UUID du modèle) | ✅ |
| Rendu | disques dorés (entité du modèle) | éclats et traits dorés | 🎨 |
