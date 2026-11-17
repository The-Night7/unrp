# Dizzy Punch

Runtime definition: `ability_powers.json` → `powers.dizzy_punch`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Haymaker tournoyant | action | 14 | 0 | 240 | `dz_spin` |
| Low Blow | action | 6 | 0 | 60 | `dz_sucker` |
| Tissage | action | 12 | 0 | 240 | `dz_weave` |

## Moves

- **Haymaker tournoyant**: Arm your next hit for five seconds: nausea followed by reversed controls.
- **Low Blow**: Feinte puis crochet : plus fort si la cible te regardait (3 puis 4,5).
- **Tissage**: For six seconds, incoming melee damage is reduced by 25% and attackers are stunned.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
