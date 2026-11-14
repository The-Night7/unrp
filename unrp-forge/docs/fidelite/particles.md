# Fidélité — Particles

Sources : `palladium/powers/particles.json`, `events/ParticlesEvents`, `block/ParticleBlock`, `block/LatticeBlockEntity`, `entity/Particle*Entity`, `client/LatticeFx`, `client/LatticeRenderer`, `client/Particle*Renderer`, `client/Shard*`, `client/Weave*`, `network/Weave*Packet`, `init/ModSounds`.
Code : package `uo/` — **code du modèle repris tel quel**.

| Élément | Statut | Détail |
|---|---|---|
| Logique serveur (motifs Guardian/Warden/Vanguard/Architect, trame de blocs, Condensation, Tissage, Construction, Poing) | ✅ | `uo/ParticlesEvents` |
| Touches | ✅ | Changer de motif (`pt_switch`, 10), Condensation (`pt_condense`, 60) ou Tissage maintenu (`pt_weave_on`) selon la posture 3, Construction (40), Poing (100) ; déblocages unlock_s2..s4 |
| Bloc de particules, entités, rendus, HUD d'essaim | ✅ | classes du modèle ; essaim synchronisé comme StatSyncPacket |
| Sons du modèle | ✅ | ModSounds (namespace unrp) |
| Icône de posture (stance_hud_* Palladium) | ✅ | `uo/StanceIconOverlay`, mêmes texture, position et échelle |
