# Crescent Slash

Runtime definition: `ability_powers.json` → `powers.crescent_slash`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Croissant | held | 0 | 8 | 0 | `cs_slash` |
| Jumeaux | action | 16 | 0 | 80 | `cs_twin` |
| Fan | action | 24 | 0 | 140 | `cs_volley` |
| Grand croissant | held | 0 | 42 | 0 | `cs_grand` |

## Moves

- **Croissant**: Hold to charge for up to three seconds, then release an aura crescent dealing 6–12 damage.
- **Jumeaux**: Deux croissants devant toi. Accroupi : deux croissants plus forts qui transpercent.
- **Fan**: Five small crescents in a quick fan.
- **Grand croissant**: Hold for up to four seconds to release a huge twin crescent that cuts through its path.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
