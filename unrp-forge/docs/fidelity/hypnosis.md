# Hypnosis

Runtime definition: `ability_powers.json` → `powers.hypnosis`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Pulsion de sommeil | action | 16 | 0 | 180 | `hy_sleep` |
| Imperious Gaze | action | 18 | 0 | 240 | `hy_gaze` |
| Hypnose profonde | held | 0 | 1 | 0 | `hy_deep` |
| Memory Erasure | action | 24 | 0 | 400 | `hy_wipe` |

## Moves

- **Pulsion de sommeil**: Put everyone within 6.5–10 blocks to sleep. A hit of six or more wakes them.
- **Imperious Gaze**: Command a target within 16–26 blocks to remain still. A hit of eight or more shortens the effect.
- **Hypnose profonde**: Hold to immobilise a target after concentrating briefly. You cannot act otherwise. A hit of twelve or more frees them.
- **Memory Erasure**: Embrume l'esprit de la cible : elle devient aveugle puis titube.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
