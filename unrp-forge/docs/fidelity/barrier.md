# Barrier

Runtime definition: `ability_powers.json` → `powers.barrier`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Raise Barrier | action | 15 | 0 | 40 | `unrp.barrier_deploy_trigger` |
| Expand Barriers | action | 6 | 0 | 8 | `unrp.barrier_growth_trigger` |
| Ram | action | 20 | 0 | 300 | `unrp.barrier_ram_trigger` |

## Moves

- **Raise Barrier**: Raise one sphere around yourself. Crouch to place up to two external barriers at your aim, or recall the barrier you look at.
- **Expand Barriers**: Expand all your barriers. Crouch to contract them and crush their occupants.
- **Ram**: Charge behind a shield, launching anything ahead.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
