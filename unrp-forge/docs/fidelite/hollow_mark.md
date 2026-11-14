# Fidélité — Hollow Mark (Origin VIII)

Sources : `palladium/powers/hollow_mark.json`, `events/HollowMarkEvents`, `events/HollowDeepFavors`, `entity/HollowDoppelEntity`, `item/CarvedRuneItem`, `item/BoneCharmItem`, `loot/HollowRelicsModifier`, `client/RuneCodexScreen`, `client/ClientHollowDeep`, `client/HollowDoppelRenderer`, `network/Codex*Packet`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (runes, rangs hm_r0..r13, stades, charmes, Blink, Gaze, Slam, Darts, faveurs profondes) | ✅ | `uo/HollowMarkEvents`, `uo/HollowDeepFavors` |
| Touches | ✅ | 9 actions `hm_*_trigger` aux recharges du modèle ; débloquées comme le modèle (rangs `hm_r*` ou `hm_owns.*`) ; tag actif `active.hollow_mark`, son `block.sculk_sensor.clicking` 0,7 / 0,6 |
| Codex des runes | ✅ | RuneCodexScreen du modèle (rune taillée, `/unrp codex`) |
| Double, charmes, runes | ✅ | classes du modèle |
| Reliques dans les coffres | ✅ | modificateur global `unrp:hollow_relics` |
| Marionnettes de Singularity | ❌ | pas portées |
