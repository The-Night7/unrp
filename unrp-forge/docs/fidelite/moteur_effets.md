# Moteur d'effets « Pass » du modèle

Le modèle dessine Lightning, Barrier, Energy Discharge, Time Manipulation, Predator et Aura Manipulation avec son propre
moteur client (`client/ClientPass*`, `Pass*`, shaders `end_grey` / `null_grey`). Il est porté tel quel dans `pass/`
(code décompilé, noms remis en clair par la table de ForgeGradle), avec :

- `PassBridge` : ce que le moteur lisait ailleurs (Palladium, caches client) ; l'état des pouvoirs arrive par `PassStatePacket` ;
- `PassSync` : diffuse cet état (clés du modèle : `lightning_activation`, `surge_run`, `beam`, `power_toggle`, `long_range_beam`,
  `wave`, `wave_run`, `timemanip_activation`, `anim_clock`, `anim_counter`, `predator_activation`, `sense_focus`, `am_shown`) ;
- `LightningPass`, `EdPass`, `TimePass`, `PredatorPass`, `BarrierPass`, `AmPassLite` : les envois côté serveur, aux mêmes moments que le modèle ;
- mixins `ClientExplosionMixin`, `EntityTintMixin`, `LivingHeldMixin` ; calques `TimeGears`, `PredatorTrans` ; `PredatorClient`.

Barrier tourne désormais sur le code du modèle (`BarrierEntity`, `BarrierEvents`, `BarrierReflect`) : nos capacités posent
les mêmes tags déclencheurs que ses commandes Palladium (`trigger_tag`, `active_tag` dans `ability_powers.json`).

| Pouvoir | Envois branchés | Reste |
|---|---|---|
| Lightning | LT_ON, LT_BOLT, LT_HIT, GONE, LT_TOUCH, LT_RETORT, clés | — |
| Energy Discharge | ED_ORB, ED_BLAST, ED_GATHER, ED_BURST, ED_LAUNCH, ED_LAND, clés | — |
| Time Manipulation | TM_ON, TM_FLOW, TM_REWIND, TM_BLINK, TM_MARK, TM_RECALL, TM_FREEZE, TM_HOLD, TM_FREE, TM_STRIKE, TM_DELAY, TM_DELAY_HIT | — |
| Predator | PD_LOCK, PD_FOCUS, PD_BEAT, PD_RUSH, PD_RUSH_HIT, PD_BREAKER, PD_ROAR, cible et transformation | — |
| Barrier | tous (code du modèle) | — |
| Aura Manipulation | AM_ON, AM_TAKE, AM_EXPEL, copies sombres | AM_SENSE (roue de copie du modèle à porter) |

Non vérifié en jeu au moment du portage : à tester avec `./gradlew runClient`.
