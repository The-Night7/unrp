# Explosion

Runtime definition: `ability_powers.json` → `powers.explosion`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Detonation | held | 0 | 8 | 0 | `ex_detonate` |
| Onde explosive | action | 35 | 0 | 140 | `ex_shock` |
| Cluster Detonation | held | 0 | 1 | 0 | `ex_cluster` |

## Moves

- **Detonation**: Hold to charge for up to ten seconds, then release an explosion at your aim within twelve blocks. You are immune to explosions and their knockback.
- **Onde explosive**: Explode around yourself within ten blocks for 12–14 damage.
- **Cluster Detonation**: Hold for small explosions every 0.6 seconds, costing ten aura each.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
