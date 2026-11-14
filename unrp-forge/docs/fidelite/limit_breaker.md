# Fidélité — Limit Breaker (Origin II)

Sources : `palladium/powers/limit_breaker.json`, `events/LimitBreakerEvents`, `LimitBreakerForms`, `points/OriginGrowth`, `client/*Lb*`, `client/Colossus*`, `client/FormWheelScreen`, `client/GritHudOverlay`, `client/ArcaneBolt/HomingBeam/WaveOrbRenderer`, `network/LbForm/LbBecoming/GritHud/FormWheel/FormPick/BreakCine/OriginLookPacket`.
Code : package `uo/` — **code du modèle repris tel quel** (décompilé, renommé), plus `uo/ModelEnv` (pont vers notre moteur).

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (bord, cran, Ki, paliers, 14 formes, Bolt/Barrage, Wave/Nova, Break, Rush/Crash, vol, cérémonies) | ✅ | `uo/LimitBreakerEvents` = LimitBreakerEvents du modèle |
| Touches | ✅ | comme limit_breaker.json : 4 actions qui posent `unrp.lb_bolt/wave/break/rush_trigger` (recharges 10/30/30/20), tag actif `unrp.active.limit_breaker`, son d'activation `block.fire.ambient` 1,0 / 0,6 |
| Vol Palladium | ✅ | modificateurs `palladium:flight_speed` du modèle (mêmes UUID) |
| Vessel (croissance Origin) | ✅ | même formule ; stockée dans notre sauvegarde (`ability/OriginGrowth`) au lieu des scores `og_level/og_xp` |
| Roue des formes, HUD Grit, cinématique Break | ✅ | écrans/overlays du modèle (`unrp_grit`) |
| Rendu : fourrure, cheveux, aura, bord, Colossus, projectiles | ✅ | couches et rendus du modèle, dans l'ordre de PlayerLayersHandler |
| Aura | ✅ | score `unrp.aura` = notre aura ; ce que le code du modèle y retire est retiré de notre aura (`uo/ModelEnv`) |
| Suspension | ✅ | tag `unrp.power_disabled` posé tant que notre moteur suspend le pouvoir |
| Groupes / alliés | ✅ | `OriginStrike` : nos groupes (`SocialService`) à la place de GroupManager |
| Valerie, Leilah, le Principal | ❌ | PNJ du hub du modèle, absents chez nous : jamais épargnés comme « gardiens » |
