# Arcane Shot

Runtime definition: `ability_powers.json` → `powers.arcane_shot`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Tir arcanique | held | 3 | 0 | 60 | `arcane_charge` |
| Salve arcanique | action | 18 | 0 | 160 | `arcane_volley` |

## Moves

- **Tir arcanique**: Hold to charge for up to 2.5 seconds, then release to fire. Taking a hit interrupts the charge.
- **Salve arcanique**: Seven bolts in a fan.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
