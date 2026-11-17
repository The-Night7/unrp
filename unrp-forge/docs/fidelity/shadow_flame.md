# Shadow Flame

Runtime definition: `ability_powers.json` → `powers.shadow_flame`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Boule de flamme | held | 0 | 14 | 0 | `sf_bolt` |
| Triple Volley | action | 24 | 0 | 40 | `sf_volley` |

## Moves

- **Boule de flamme**: Hold to charge for up to ten seconds, then release. Longer charges create larger explosions.
- **Triple Volley**: Throw three fireballs at once.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
