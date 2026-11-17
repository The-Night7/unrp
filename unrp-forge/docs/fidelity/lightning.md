# Lightning

Runtime definition: `ability_powers.json` → `powers.lightning`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Lightning | action | 12 | 0 | 36 | `bolt` |
| Electric Touch | action | 10 | 0 | 50 | `strike` |
| Course foudroyante | held | 0 | 0 | 0 | `buff` |
| Rayon foudroyant | held | 0 | 0 | 0 | `beam` |

## Moves

- **Lightning**: Fire a lightning bolt that slows its target.
- **Electric Touch**: Electrocute the nearest target ahead for heavy damage, slowness and weakness.
- **Course foudroyante**: Hold to rush straight ahead at high speed.
- **Rayon foudroyant**: Hold to fire a continuous lightning beam.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
