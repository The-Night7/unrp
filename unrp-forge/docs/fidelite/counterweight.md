# Fidélité — Counterweight

Sources : `palladium/powers/counterweight.json`, `events/CounterweightEvents`, `client/CwHudOverlay`, `client/CwHudState`, `network/CwHudPacket`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (parade, Banque, élan, stades I-VI, Wave/Kinetic Bound, Fist/Reknit, Point) | ✅ | `uo/CounterweightEvents` |
| Touches | ✅ | Absorb maintenu (`cw_absorb_trigger` + `cw_guard_held`), Wave (20), Fist (15), Point (60, exige `cw_stage5`) ; tag actif `active.counterweight`, son `block.anvil.land` 0,5 / 1,8 |
| HUD | ✅ | `unrp_counterweight` (CwHudOverlay du modèle) |
