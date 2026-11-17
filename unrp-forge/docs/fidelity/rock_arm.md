# Rock Arm

Runtime definition: `ability_powers.json` → `powers.rock_arm`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Frappe de pierre | action | 12 | 0 | 100 | `ra_smash` |
| Garde de pierre | toggle | 1 | 1 | 0 | `ra_guard` |

## Moves

- **Frappe de pierre**: A heavy stone punch deals 6–10 damage by level and knocks targets back.
- **Garde de pierre**: Toggle raised stone arms: incoming damage reduced by 15%, increased knockback resistance and slower movement.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
