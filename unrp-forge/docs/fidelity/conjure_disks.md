# Conjure: Disks

Runtime definition: `ability_powers.json` → `powers.conjure_disks`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Disques en orbite | toggle | 1 | 1 | 0 | `cd_shield` |
| Lancer un disque | action | 12 | 0 | 40 | `cd_launch` |
| Hover Disc | toggle | 1 | 1 | 0 | `cd_hover` |

## Moves

- **Disques en orbite**: Toggle two orbiting discs: +6 armour, 16–28% damage reduction, frontal projectiles reflected for three aura, and slightly slower movement.
- **Lancer un disque**: Throw a disc twenty-eight blocks for 7–11 damage. It reforms in 9–14 seconds. Level determines whether you hold three to six discs.
- **Hover Disc**: Toggle riding a disc: flight and +30% speed. Jump to rise; crouch to descend.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
