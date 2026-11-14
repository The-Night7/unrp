# Energy Conduit

Sources : `powers/energy_conduit.json`, `events/EnergyConduitEvents`, `entity/LassoEntity`, `entity/LinkEntity`.

| Élément | Modèle | Notre version | Statut |
|---|---|---|---|
| **Lasso d'aura** (X) | 8 aura, recharge 30 ; une boucle (vitesse 1,4) lie ce qu'elle touche à toi ; 2 liens au plus ; accroupi : coupe le plus récent ; éteindre le pouvoir coupe tout | identique ; le lien est une table (pas une entité), cassé au-delà de 38 blocs | ≈ |
| **Transfert** (C, rang 2, maintenir) | 4 aura, -1 / 10 ticks ; sans accroupi : toutes les 10 ticks, -2 chez toi et +2 chez chaque allié lié (si tu as plus de 15 aura) ; accroupi : tu prends min(2, aura - 20) à un lié | identique | ✅ |
| **Restauration** (V, rang 3, maintenir) | 4 aura, -1 / 10 ticks ; toutes les 10 ticks : 4 aura pour soigner un lié de 2 PV ; accroupi : tu te soignes de 2 PV en lui prenant 3 aura (ou 1,5 PV et 1,5 dégât à une créature) | identique | ✅ |
| **Contrôle** (B, rang 4, maintenir) | 6 aura ; attire les liés (0,12 x la distance, 0,6 au plus) ; accroupi : entrave (lenteur croissante jusqu'à VI, saut bloqué après 2 s), tu es ralenti III, -1 aura / 8 ticks | identique | ✅ |
| Rendu | liens et lasso (entités) | traits lumineux entre les liés | 🎨 |
