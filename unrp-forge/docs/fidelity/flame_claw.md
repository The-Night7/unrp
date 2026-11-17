# Flame Claw

Runtime definition: `ability_powers.json` → `powers.flame_claw`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Anneau de feu | action | 18 | 0 | 240 | `fc_ring` |
| Surchauffe | toggle | 1 | 1 | 0 | `fc_surge` |
| Blazing Rush | action | 16 | 0 | 180 | `fc_rush` |

## Moves

- **Anneau de feu**: A five-block fire breath deals 6–14 damage and burns for five seconds.
- **Surchauffe**: Toggle hotter claws for +4–12 damage and longer burns.
- **Blazing Rush**: Dash through enemies, burning them for 8–16 damage. Your next strike within five seconds is empowered.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
