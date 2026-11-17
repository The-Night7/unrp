# Spectral Claw

Runtime definition: `ability_powers.json` → `powers.spectral_claw`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Prise | action | 22 | 0 | 200 | `sc_grip` |
| Lancer | action | 16 | 0 | 60 | `sc_throw` |

## Moves

- **Prise**: Grab the nearest non-boss enemy ahead within 4.5 blocks for eight seconds, dealing 1.5 damage per second. Taking a hit breaks the grab.
- **Lancer**: Throw your captive for six damage and slowness; a wall impact adds five damage.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
