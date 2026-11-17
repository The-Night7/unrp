# Aura Manipulation

Runtime definition: `ability_powers.json` → `powers.aura_manipulation`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Copier | action | 25 | 0 | 10 | `am_copy` |
| Expulser | action | 0 | 0 | 40 | `am_expel` |

## Moves

- **Copier**: Copy the power of a player within twenty-four blocks and keep it in reserve indefinitely. Shift + R cycles copied powers, then returns to your own.
- **Expulser**: Release all copied powers. This also happens on death or when aura reaches zero.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
