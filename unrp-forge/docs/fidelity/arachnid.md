# Arachnid

Runtime definition: `ability_powers.json` → `powers.arachnid`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Assaut arachnide | action | 10 | 0 | 45 | `ar_assault` |
| Pattes gardiennes | action | 8 | 0 | 20 | `ar_guard` |
| Bond arachnide | action | 8 | 0 | 80 | `ar_leap` |
| Charge empalante | action | 18 | 0 | 120 | `ar_rush` |

## Moves

- **Assaut arachnide**: Coups de pattes sur tout ce qui est devant toi (3,8 blocs).
- **Pattes gardiennes**: For six seconds, incoming damage is multiplied by 0.65.
- **Bond arachnide**: Grand bond.
- **Charge empalante**: Charge en avant et empale ce qui est sur la route.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
