# Fidélité — Daystar

Sources : `palladium/powers/daystar.json`, `events/DaystarEvents`, `events/DaystarMath`, `entity/Sun*Entity`, `item/SunsteelAxeItem`, `client/ClientDaystar`, `client/Daystar*`, `client/Sun*Renderer`, `client/BladeTrail`, `network/DaystarHudPacket`, cratères de `events/NadirEvents`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (jauge de soleil, chaleur, hache, soleil chargé, faille, High Noon, Burnout) | ✅ | `uo/DaystarEvents` |
| Entités soleil (orbe, faille, croissant, hache lancée) | ✅ | classes et rendus du modèle |
| Touches | ✅ | comme daystar.json : Hache `ds_axe_trigger` (10), Soleil maintenu (15 d'aura à l'appui, `ds_sun_charging` puis `ds_sun_release`, recharge 320), Sunder (30 d'aura, 500), High Noon (55 d'aura min., 100) ; interrupteur `active.daystar` + `ds_on`, extinction `ds_off_trigger`, son `block.beacon.power_select` 0,7 / 1,4 |
| Hache d'acier solaire | ✅ | SunsteelAxeItem du modèle |
| Rendu : cadran, doigt, traînées | ✅ | classes du modèle |
