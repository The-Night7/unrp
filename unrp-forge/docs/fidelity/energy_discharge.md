# Energy Discharge

Runtime definition: `ability_powers.json` → `powers.energy_discharge`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Rayon | held | 0 | 1 | 0 | `ed_beam` |
| Energy Sphere | action | 30 | 150 | 300 | `ed_sphere` |
| Shockwave | action | 40 | 100 | 400 | `ed_burst` |
| Propulsion | action | 30 | 75 | 100 | `ed_propel` |

## Moves

- **Rayon**: Hold a long beam with thirty-two-block range. Crouch on activation for an energy wave that extends after half a second.
- **Energy Sphere**: Throw a sphere that explodes for twenty damage. Requires 150 aura and consumes fifty.
- **Shockwave**: Violently repel everything within twelve blocks.
- **Propulsion**: Launch yourself in the direction you face without fall damage.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
