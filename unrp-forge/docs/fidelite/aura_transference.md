# Fidélité — Aura Transference

Sources : `palladium/powers/aura_transference.json`, `events/AuraTransferEvents`, `entity/AuraThreadEntity`, `client/ImbuedItemLayer`, `client/ImbuedItemTooltip`, `client/WeaveFx`, `client/WeaveDraw`, `client/WeaveQuads`, `client/ArsenalDraw`, `mixin/ItemInHandMoveMixin`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (copie d'un trait à mains nues, tissage dans une arme / une personne, fils d'aura) | ✅ | `uo/AuraTransferEvents` |
| Pouvoir de la cible | ✅ | tags `unrp.power.*` (créatures) puis `unrp.has.*` (nos joueurs, à la place des pouvoirs Palladium) |
| Touches | ✅ | comme aura_transference.json : `at_attune_trigger` (5), `at_imbue_item_trigger` / `at_imbue_player_trigger` (10 d'aura min., 40) ; interrupteur `active.aura_transference` + `at_on`, extinction `at_off_trigger`, son `block.beacon.activate` 0,6 / 1,5 |
| Rendu : objet tissé (3e et 1re personne), info-bulle | ✅ | classes du modèle |
