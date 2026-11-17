# Conjure: Vines

Runtime definition: `ability_powers.json` → `powers.conjure_vines`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Fouet de lianes | held | 0 | 4 | 0 | `cv_lash` |
| Entrave | action | 24 | 0 | 160 | `cv_entangle` |
| Thorns | action | 22 | 0 | 140 | `cv_spikes` |
| Embrace | held | 0 | 6 | 0 | `cv_grasp` |

## Moves

- **Fouet de lianes**: Hold to guide a vine. It lashes for four damage and grabs the first enemy in your sightline, carrying them ahead of you.
- **Entrave**: Throw vines over a six-block area, binding targets for five seconds and preventing sprinting or jumping. Crouching casts Thorn Prison for twenty-six aura, creating a hedge for two hundred ticks.
- **Thorns**: Five vine thorns in a fan deal four damage each. Crouching creates four stakes at your aim for seven damage and stun.
- **Embrace**: Hold to grow a vine field at your aim, slowing and damaging its occupants.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
