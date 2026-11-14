# Fidélité — Armament Dominion

Sources : `palladium/powers/armament_dominion.json`, `events/ArmamentEvents`, `item/ArmamentItem`, `entity/ArsenalBoltEntity`, `entity/ArsenalGateEntity`, `client/Arsenal*`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (forge, saisie, balle d'armement, arsenal complet, portes) | ✅ | `uo/ArmamentEvents` |
| Touches | ✅ | Forger (20), Saisir (5), Balle maintenue (8 d'aura à l'appui, recharge 60, `ad_shot_charging` / `ad_shot_release`), Arsenal complet (40 d'aura, 600) ; interrupteur `active.armament_dominion` + `ad_on`, extinction `ad_off_trigger`, son `block.beacon.activate` 0,6 / 0,8 |
| Objets, entités, rendus | ✅ | classes du modèle |
| Valeur d'un pouvoir (appraise) | ✅ | tags `unrp.has.*` / `unrp.power.*` au lieu des pouvoirs Palladium |
