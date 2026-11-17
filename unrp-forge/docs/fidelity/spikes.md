# Spikes

Runtime definition: `ability_powers.json` → `powers.spikes`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Spike Row | action | 8 | 0 | 35 | `sk2_shot` |
| Spike Volley | action | 18 | 0 | 100 | `sk2_volley` |
| Guided Spikes | action | 22 | 0 | 140 | `sk2_redirect` |
| Champ de pointes | action | 34 | 0 | 220 | `sk2_field` |

## Moves

- **Spike Row**: Eight spikes erupt ahead, each larger than the previous one, dealing 4.5 damage.
- **Spike Volley**: Five spikes erupt successively at the target for 3.5 damage each.
- **Guided Spikes**: Four detached thorns curve from your sides towards your aim, dealing 4.5 damage.
- **Champ de pointes**: Cover targeted ground in spikes for eight seconds. Crouch and spend fourteen extra aura to form a grid of nine large spikes ahead.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
