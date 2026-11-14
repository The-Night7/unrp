# Fidélité — Black Lash

Sources : `palladium/powers/black_lash.json`, `events/BlackLashEvents`, `events/LashMath`, `client/ClientBlackLash`, `client/Lash*`, `client/HandCapture`, `client/GroundScan`, `mixin/PlayerLeanMixin`, `network/Lash*Packet`, cratères de `events/NadirEvents` + `data/VoidLedger` + `client/ClientVertigo`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (tentacules, balancier, lance-pierre, Entrave, Fouet/Garde, Tempête, tension, vent mortel, à terre) | ✅ | `uo/BlackLashEvents` |
| Touches | ✅ | comme black_lash.json : Agrippement maintenu (`bl_lash_held`, `bl_lash_trigger`, relâché `bl_lash_release`), Entrave/Fouet/Tempête (`bl_*_trigger`, recharge 6) ; bloquées par `bl_locked`, `aura_depleted`, `water_form_active` ; interrupteur : `active.black_lash` + `bl_on`, extinction `bl_off_trigger`, son `block.chain.place` 0,8 / 0,7 |
| Rendu : tentacules, veines, penchée, mains, anneau, écran « à terre » | ✅ | classes du modèle + `PlayerLeanMixin` |
| Cratères | ✅ | système de cratères du modèle (règles `unrpRealCraters`, `unrpCratersHeal` : rebouchage) |
| Recharges | ✅ | réduites par la Ruse (TrickCooldownEvents), d'après nos stats |
| Réglage « noncanon » | ✅ | objectif `unrp.config` (ConfigBridgeEvents) |
