# Missiles

Runtime definition: `ability_powers.json` → `powers.missiles`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Missile | held | 0 | 14 | 0 | `ms_missile` |
| Jumeaux | action | 28 | 0 | 120 | `ms_twin` |
| Barrage | held | 0 | 10 | 0 | `ms_barrage` |
| Ogive | held | 0 | 60 | 0 | `ms_heavy` |

## Moves

- **Missile**: Hold to prime for up to three seconds, then release to throw. Longer charges create larger explosions. Taking a hit interrupts charging.
- **Jumeaux**: Un missile de chaque main. Accroupi (+10 aura) : quatre missiles en gerbe qui reviennent vers ton regard.
- **Barrage**: Hold to stream small missiles, costing two aura each.
- **Ogive**: Hold to prime a huge warhead for up to four seconds, then release. Slow but devastating.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
