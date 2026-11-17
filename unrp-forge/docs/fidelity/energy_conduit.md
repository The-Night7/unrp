# Energy Conduit

Runtime definition: `ability_powers.json` → `powers.energy_conduit`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Lasso d'aura | action | 8 | 0 | 30 | `ec_lasso` |
| Transfert | held | 0 | 4 | 0 | `ec_transfer` |
| Restauration | held | 0 | 4 | 0 | `ec_restore` |
| Control | held | 0 | 6 | 0 | `ec_control` |

## Moves

- **Lasso d'aura**: Throw an aura loop to bind a target, with up to two links. Crouch to sever the latest link. Deactivating the power severs all links.
- **Transfert**: Hold to give aura to linked allies. Crouch to draw aura from linked targets instead.
- **Restauration**: Hold to heal linked allies with aura. Crouch to drain their health instead.
- **Control**: Hold to pull linked targets towards you. Crouch to restrain them with increasing force.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
