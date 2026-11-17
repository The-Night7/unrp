# World-effect renderer

The `pass/` package renders the larger effects used by Lightning, Barrier, Energy Discharge, Time Manipulation, Predator and Aura Manipulation. `PassBridge` adapts shared state; `PassSync` distributes it through `PassStatePacket`.

The server-side bridges are `LightningPass`, `EdPass`, `TimePass`, `PredatorPass`, `BarrierPass` and `AmPassLite`. Entity layers and the explosion, tint and held-item mixins supply additional rendering hooks. Barrier also uses its own entities and reflection handlers.

A bridge being registered does not prove every effect looks correct. Test activation, release, target changes, dimension changes and logout in game. Use [VISUAL_STYLE.md](../VISUAL_STYLE.md) to locate the actual textures.
