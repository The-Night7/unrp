# Fidélité — Sarcophage (Origin)

Sources : `palladium/powers/sarcophage.json`, `events/SarcophageEvents`, `events/PhageEvents`, `entity/BindVisualEntity`, `item/VitalSandItem`, `item/OssifiedTailItem`, `client/Sarcophage*`, `client/Sarc*`, `client/Phage*`, `client/Sand*`, `client/SegmentedTailModel`, `client/BindVisualRenderer`, `network/SandHudPacket`.
Code : package `uo/` — **code du modèle repris tel quel**, avec `uo/ModelEnv` (aura, suspension, objectifs de ScoreboardBootstrap).

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (queues, réserve, Sable vital, Grab/Coffin, Duneride/Sever, Grainshot/Tempest, Sandshroud, Phage) | ✅ | `uo/SarcophageEvents`, `uo/PhageEvents` |
| Touches | ✅ | comme sarcophage.json : 3 actions (`sc_grab/lunge/spray_trigger`, recharge 20) et l'interrupteur Sandshroud (`sc_shroud`, exige `sc_tails3`) ; tag actif `unrp.active.sarcophage`, son `block.sand.break` 1,0 / 0,6 |
| Entraves | ✅ | « Libère-toi » : notre `Struggle` (même API que StruggleEvents) |
| Objets | ✅ | Sable vital, Queue ossifiée : classes du modèle |
| Rendu : queues, tempête, accessoires de sable, Phage, cocon, vue de la saisie | ✅ | couches et rendus du modèle |
| HUD | ✅ | `unrp_sand` (SandHudOverlay du modèle) |
| Singularity (« everpresent », hors d'atteinte) | ❌ | pouvoir absent chez nous : jamais vrai |
