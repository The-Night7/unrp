# Regeneration

Runtime definition: `ability_powers.json` → `powers.regeneration`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Adrenaline | action | 20 | 0 | 400 | `regen_adrenaline` |
| Martyr | toggle | 0 | 1 | 0 | `martyr` |

## Moves

- **Adrenaline**: Immediately restores four HP and grants Speed II for five seconds.
- **Martyr**: Toggle damage sharing: allies within eight blocks take 70% of incoming damage; you take the remainder.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
