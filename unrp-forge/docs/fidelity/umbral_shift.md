# Umbral Shift

Runtime definition: `ability_powers.json` → `powers.umbral_shift`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Dive | action | 8 | 0 | 20 | `us_dive` |
| Assaut d'ombre | action | 20 | 0 | 160 | `us_assault` |

## Moves

- **Dive**: Dive into the ground or a wall and slide up to forty blocks while invulnerable and invisible. Costs one aura every seven ticks. Press again to emerge.
- **Assaut d'ombre**: Erupt from shadow, repelling and blinding nearby enemies for nine damage, or thirteen just after emerging.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
