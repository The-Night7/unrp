# Azure Storm

Runtime definition: `ability_powers.json` → `powers.azure_storm`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Azure Storm | toggle | 0 | 1 | 0 | `storm_field` |
| Garde du vent | action | 12 | 0 | 140 | `storm_guard` |
| Storm Flight | toggle | 0 | 1 | 0 | `storm_flight` |
| Lance cyclone | action | 20 | 0 | 150 | `storm_lance` |

## Moves

- **Azure Storm**: Toggle a five-block storm that lifts and damages targets.
- **Garde du vent**: For six seconds, approaching projectiles are deflected.
- **Storm Flight**: Interrupteur : tu voles.
- **Lance cyclone**: A wind spear pierces a twenty-block line and lifts targets.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
