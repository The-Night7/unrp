# Invisibility

Runtime definition: `ability_powers.json` → `powers.invisibility`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Cape optique | toggle | 0 | 1 | 0 | `passive` |
| Shared Cloak | action | 20 | 0 | 120 | `share` |

## Moves

- **Cape optique**: Remain invisible while active, cushion falls and become difficult for monsters to detect. Attacking reveals you for one second.
- **Shared Cloak**: Cloak the ally you aim at, from your group or school and within twelve blocks, for ten seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
