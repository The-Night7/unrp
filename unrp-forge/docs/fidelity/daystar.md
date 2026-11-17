# Daystar

Runtime definition: `ability_powers.json` → `powers.daystar`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Hache solaire | action | 0 | 0 | 10 | `unrp.ds_axe_trigger` |
| Soleil | held | 0 | 15 | 320 | `passive` |
| Sunder | action | 30 | 0 | 500 | `unrp.ds_sunder_trigger` |
| High Noon | action | 0 | 0 | 100 | `unrp.ds_noon_trigger` |

## Moves

- **Hache solaire**: Manifest a light axe whose strikes ignite and sweep around the target. Crouch while holding it to throw it for six aura with a four-second cooldown; press again to recall. Press normally to dismiss.
- **Soleil**: Hold to grow a sun on your fingertip for fifteen aura, then release to throw. Crouch to burst it around yourself. Airborne and looking down casts Shooting Star. At night, only an ember remains.
- **Sunder**: Split the ground with a long fire fissure. Crouch for a fan; look up for a crescent; airborne and looking down creates a crushing dive.
- **High Noon**: Only at noon, 11:00–13:00, once per day and under open sky. For one minute, take only 5% damage with no knockback or negative effects and deal 40% more damage. Afterwards, Burnout removes four hearts for five minutes.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
