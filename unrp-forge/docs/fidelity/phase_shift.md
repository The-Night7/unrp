# Phase Shift

Runtime definition: `ability_powers.json` → `powers.phase_shift`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Defensive Form | toggle | 0 | 1 | 0 | `passive` |
| Offensive Form | toggle | 0 | 1 | 0 | `passive` |

## Moves

- **Defensive Form**: +16 armour and two HP regenerated every two seconds, with reduced speed and strength.
- **Offensive Form**: +5 damage and much greater speed, at a cost of twelve armour.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
