# Black Lash

Runtime definition: `ability_powers.json` → `powers.black_lash`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Agrippement | held | 0 | 0 | 0 | `unrp.bl_lash_trigger` |
| Entrave | action | 0 | 0 | 6 | `unrp.bl_bind_trigger` |
| Fouet | action | 0 | 0 | 6 | `unrp.bl_whip_trigger` |
| Storm | action | 0 | 0 | 6 | `unrp.bl_storm_trigger` |

## Moves

- **Agrippement**: Throw a tendril for eight aura with a 1.5-second cooldown. Light targets are pulled to you; heavy targets and blocks pull you towards them. Airborne and aiming down adds a crushing dive for twelve extra aura. Each tendril builds tension.
- **Entrave**: Grab a target within ten blocks for twenty aura with a twelve-second cooldown. Press on another target to carry up to three, costing ten aura each. Press elsewhere to throw for six damage, or crouch to crush for eight.
- **Fouet**: Three lashes hit the nearest enemy ahead for four damage each, costing sixteen aura with a five-second cooldown. Crouch for Tendril Guard: eighteen aura, fourteen-second cooldown, four seconds of spinning strikes and 60% damage reduction.
- **Storm**: Requires half your tension. Eight tendrils lash nearby targets for three seconds, then strike together. Costs forty-eight aura with a ninety-second cooldown.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
