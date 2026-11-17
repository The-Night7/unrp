# Contact Shred

Runtime definition: `ability_powers.json` → `powers.contact_shred`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Lacerating Embrace | action | 14 | 0 | 140 | `cs_grip` |
| Toucher griffu | action | 8 | 0 | 60 | `cs_rake` |

## Moves

- **Lacerating Embrace**: Grab a nearby target. The mark grows while held, then tears them when released or when the grab ends.
- **Toucher griffu**: Dash through enemies, injuring and marking each one.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
