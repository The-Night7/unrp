# Hand Blade

Runtime definition: `ability_powers.json` → `powers.hand_blade`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Fente | action | 20 | 0 | 80 | `hb_thrust` |
| Croissant | action | 25 | 0 | 120 | `hb_release` |

## Moves

- **Fente**: Dash and stab the first enemy for nine damage, bleeding and slowness.
- **Croissant**: Throw an energy crescent that travels twenty-two blocks, pierces three targets and deals seven damage.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
