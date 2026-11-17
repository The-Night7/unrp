# Hydrofreeze

Runtime definition: `ability_powers.json` → `powers.hydrofreeze`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Changer de posture | action | 0 | 0 | 10 | `stance_next` |
| Forme liquide | toggle | 0 | 1 | 0 | `water_form` |
| Enveloppement | held | 2 | 0 | 0 | `hydro_envelop` |
| Vague | action | 12 | 0 | 55 | `beam` |
| Ice Blades | toggle | 0 | 1 | 0 | `passive` |
| Lance de glace | action | 14 | 0 | 35 | `bolt` |
| Prison de glace | action | 100 | 0 | 120 | `hydro_freeze` |

## Moves

- **Changer de posture**: Switch between Water and Ice move bars.
- **Forme liquide**: Toggle a water form: +20% speed and incoming melee damage halved.
- **Enveloppement**: Hold to trap a target within eight blocks in water, drowning them.
- **Vague**: A wave pushes back everything ahead.
- **Ice Blades**: Toggle +6 melee damage and slowing strikes.
- **Lance de glace**: Projectile de glace qui ralentit.
- **Prison de glace**: Freeze a target within eighteen blocks for ten seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
