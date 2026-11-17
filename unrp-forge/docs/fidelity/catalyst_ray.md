# Catalyst ray

Runtime definition: `ability_powers.json` → `powers.catalyst_ray`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Catalyse | action | 8 | 0 | 30 | `cr_catalyze` |
| Table rase | action | 12 | 0 | 140 | `cr_clean` |
| Genetic Stasis | action | 40 | 0 | 260 | `cr_stasis` |
| Infusion | action | 20 | 0 | 320 | `cr_imbue` |

## Moves

- **Catalyse**: Gives the targeted creature, or yourself while crouching, one random mutation for thirty seconds. More than three causes overload.
- **Table rase**: Efface mutations et effets de la cible (accroupi : toi).
- **Genetic Stasis**: Suspends the targeted player's ability for ten seconds, within twenty-eight blocks.
- **Infusion**: With exactly one active mutation, strengthens it and extends its duration to sixty seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
