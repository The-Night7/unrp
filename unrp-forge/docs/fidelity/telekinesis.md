# Telekinesis

Runtime definition: `ability_powers.json` → `powers.telekinesis`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Pression | held | 2 | 0 | 0 | `tk_pressure` |
| Manipulation | action | 10 | 0 | 120 | `tk_force` |
| Orbite | action | 15 | 0 | 120 | `tk_orbit` |
| Domination absolue | action | 35 | 0 | 520 | `tk_dominion` |

## Moves

- **Pression**: Hold to crush a target within fourteen blocks. Crouch to place a crushing field at your aim.
- **Manipulation**: Lift the targeted creature or block. Press again to throw it; crouch to put it down.
- **Orbite**: Orbiting debris repels anything it touches.
- **Domination absolue**: Launch everything within twelve blocks skywards. Crouch to slam it into the ground.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
