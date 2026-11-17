# Whirlwind

Runtime definition: `ability_powers.json` → `powers.whirlwind`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Barrage | action | 18 | 0 | 80 | `ww_blast` |
| Cyclone | action | 24 | 0 | 140 | `ww_cyclone` |
| Vol | toggle | 1 | 1 | 0 | `ww_flight` |
| Bouclier de vent | held | 0 | 1 | 0 | `ww_shield` |

## Moves

- **Barrage**: A 9.5-block wind cone deals seven damage, strong knockback and slowness.
- **Cyclone**: Open a vortex that pulls everything within 8.5 blocks for one second.
- **Vol**: Toggle wind flight, costing one aura every five ticks. Jump to rise; crouch to descend.
- **Bouclier de vent**: Hold to deflect arrows and thrown projectiles within four blocks.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
