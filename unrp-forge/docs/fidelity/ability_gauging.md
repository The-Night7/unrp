# Ability Gauging

Runtime definition: `ability_powers.json` → `powers.ability_gauging`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Analyse au toucher | action | 1 | 0 | 10 | `gauge_touch` |
| Lunette d'aura | toggle | 0 | 1 | 0 | `gauge_scope` |

## Moves

- **Analyse au toucher**: Analyses the hit creature within 4.5 blocks: power, rank, level, highest stat and aura.
- **Lunette d'aura**: Interrupteur : analyse en continu ce que tu regardes (30 blocs).

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
