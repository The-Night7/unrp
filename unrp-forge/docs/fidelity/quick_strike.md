# Quick Strike

Runtime definition: `ability_powers.json` → `powers.quick_strike`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Fente | action | 12 | 0 | 70 | `qs_blitz` |
| Posture d'assaut | action | 10 | 0 | 2200 | `qs_stance` |
| Combo | action | 20 | 0 | 140 | `qs_combo` |
| Poursuite | action | 8 | 0 | 50 | `qs_pursuit` |

## Moves

- **Fente**: Dash where you look and strike the first enemy for seven damage and slowness.
- **Posture d'assaut**: For twenty seconds, gain very fast attacks and movement with reduced knockback. Requires recovery afterwards.
- **Combo**: Combo de cinq coups sur la cible devant toi ; le dernier est fort et repousse.
- **Poursuite**: A fast dash strikes anyone ahead or provides an escape.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
