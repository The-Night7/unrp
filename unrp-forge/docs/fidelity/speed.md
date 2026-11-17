# Speed

Runtime definition: `ability_powers.json` → `powers.speed`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Rush | toggle | 0 | 1 | 0 | `buff` |
| Flash Dash | action | 8 | 0 | 50 | `dash` |

## Moves

- **Rush**: Toggle +55% movement speed and a speed trail.
- **Flash Dash**: Bond fulgurant droit devant.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
