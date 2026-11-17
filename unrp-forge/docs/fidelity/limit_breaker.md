# Limit Breaker

Runtime definition: `ability_powers.json` → `powers.limit_breaker`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Flare Bolt | action | 0 | 0 | 10 | `unrp.lb_bolt_trigger` |
| Breaking Wave | action | 0 | 0 | 30 | `unrp.lb_wave_trigger` |
| Break | action | 0 | 0 | 30 | `unrp.lb_break_trigger` |
| Rush | action | 0 | 0 | 20 | `unrp.lb_rush_trigger` |

## Moves

- **Flare Bolt**: A Ki bolt costs four Ki and deals four damage times your multiplier. Crouch for Barrage: five bolts in a fan for twelve Ki, each dealing three times the multiplier. Ki regenerates by three per second.
- **Breaking Wave**: Second Limit: charge for 2.5 seconds and spend twenty Ki on a piercing beam, dealing sixteen damage or twenty-two from Azure, times the multiplier. Crouch for Nova: the same charge around you over five blocks, costing sixteen Ki and dealing seven times the multiplier.
- **Break**: Cycle unlocked forms on the fourteen-form Ladder according to Vessel level and stages. Crossing eras takes three seconds, grants invulnerability, costs 10–22+ Grit and ends in a shockwave. Within an era, the cost is four Grit. Crouch to release the form. Active forms drain Grit each second.
- **Rush**: Dash to the targeted enemy for six Ki and crush them on arrival; a wall impact creates a shockwave. Sprinting casts Crash for ten Ki, travelling faster and ending in an explosion.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
