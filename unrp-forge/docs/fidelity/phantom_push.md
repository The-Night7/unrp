# Phantom Push

Runtime definition: `ability_powers.json` → `powers.phantom_push`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Push | held | 0 | 8 | 0 | `pp_push` |
| Volley | action | 20 | 0 | 130 | `pp_volley` |
| Paume | action | 26 | 0 | 170 | `pp_palm` |
| Onde | action | 22 | 0 | 140 | `pp_wave` |

## Moves

- **Push**: Hold to gather strength for up to 2.5 seconds, then release a giant palm for 5–9 damage and strong knockback.
- **Volley**: Five small hands in a quick fan.
- **Paume**: A huge hand strikes the targeted area for eleven damage, stunning targets and breaking glass.
- **Onde**: A force cone pushes back everything, including arrows.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
