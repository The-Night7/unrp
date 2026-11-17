# Predator

Runtime definition: `ability_powers.json` → `powers.predator`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Verrouillage | action | 6 | 0 | 40 | `pd_lock` |
| Heightened Senses | held | 0 | 10 | 0 | `pd_focus` |
| Predator Rush | action | 20 | 0 | 140 | `pd_rush` |
| Brise-sol | action | 18 | 0 | 180 | `pd_breaker` |
| Transformation | toggle | 1 | 1 | 10 | `pd_trans` |

## Moves

- **Verrouillage**: Lock a target within 40–55 blocks, see them through walls and deal 1.2–1.5 times damage to them.
- **Heightened Senses**: Hold to see living beings through walls at twenty-four blocks or more. Movement is slowed.
- **Predator Rush**: Dash to the locked target for 8–14 arrival damage, slowness and weakness. Without a target, perform a simple leap.
- **Brise-sol**: Strike the ground to send a twelve-block wave that launches targets for five or more damage.
- **Transformation**: Toggle the beast: +3 damage, increased speed, +0.25 knockback and +5 lock-on range.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
