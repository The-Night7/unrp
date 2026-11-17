# Phantom Fist

Runtime definition: `ability_powers.json` → `powers.phantom_fist`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Giant Fist | held | 0 | 9 | 0 | `pf_fist` |
| Un-deux | action | 16 | 0 | 90 | `pf_double` |
| Meteor | action | 30 | 0 | 200 | `pf_meteor` |
| Rafale de coups | action | 12 | 0 | 600 | `pf_barrage` |

## Moves

- **Giant Fist**: Hold to charge for up to 2.75 seconds, then release a giant spectral fist dealing 7–13 damage.
- **Un-deux**: One fist from each hand, four ticks apart.
- **Meteor**: Rise as a giant fist forms over the target, then crashes down for thirteen area damage.
- **Rafale de coups**: For three seconds, unleash a storm of punches in front of you, within 3.8 blocks.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
