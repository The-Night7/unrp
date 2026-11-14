# Fidélité — Salt Seraph

Sources : `palladium/powers/salt_seraph.json`, `events/SaltSeraphEvents`, `events/SaltUnboundEvents`, `events/SaltMath`, `block/SaltBlock`, `item/SaltSpearItem`, `client/ClientSaltSeraph`, `client/Salt*`, `client/BodyCapture`, `network/Salt*Packet`, `events/StruggleEvents` + `client/Struggle*`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (sel, statues, piliers, Lance/Grande lance, Mur, Flèches/Sceau, Éveil/Trompette, Unbound, autopilote) | ✅ | `uo/SaltSeraphEvents`, `uo/SaltUnboundEvents` |
| Touches | ✅ | comme salt_seraph.json : `ss_lance/wall/spires/wake_trigger` (recharge 6), interrupteur `active.salt_seraph` + `ss_on`, extinction `ss_off_trigger`, son `block.amethyst_block.chime` 0,8 / 0,8 ; déblocages comme le modèle (unlock_s2..s4) |
| Bloc de sel, Lance de sel | ✅ | classes du modèle, mêmes propriétés |
| « Libère-toi » | ✅ | StruggleEvents + StruggleHudOverlay du modèle (remplace notre version, pour tous les pouvoirs) |
| Rendu : sel, statues, bord, fissures, HUD | ✅ | classes du modèle |
| Hollow Deep (pierres), Singularity (stase) | ❌ | pas portés : jamais vrai |
