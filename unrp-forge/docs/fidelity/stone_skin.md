# Stone Skin

Runtime definition: `ability_powers.json` → `powers.stone_skin`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Durcir | action | 12 | 0 | 240 | `st_harden` |
| Immobile | toggle | 1 | 1 | 0 | `st_immovable` |

## Moves

- **Durcir**: For four seconds, reduce damage by 60–85% and reflect 1.5 damage to attackers. Sprinting is disabled.
- **Immobile**: Toggle grounding: resist displacement and reduce damage by 25%, but cannot walk.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
