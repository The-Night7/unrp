# Illumination

Runtime definition: `ability_powers.json` → `powers.illumination`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Carried Light | toggle | 1 | 1 | 0 | `il_light` |
| Blinding Flash | action | 5 | 0 | 80 | `il_flash` |
| Phare | action | 8 | 0 | 200 | `il_beacon` |
| Dazzling Pulse | action | 12 | 0 | 240 | `il_pulse` |

## Moves

- **Carried Light**: Toggle light around you while moving. Costs one aura every 102 ticks.
- **Blinding Flash**: A cone of blinding light, eight blocks long.
- **Phare**: For ten seconds, nearby invisible creatures within 12–24 blocks glow.
- **Dazzling Pulse**: A burst of light blinds and slows everyone nearby.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
