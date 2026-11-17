# Strong Kick

Runtime definition: `ability_powers.json` → `powers.strong_kick`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Axe Kick | action | 14 | 0 | 160 | `sk_axe` |
| Balayage | action | 16 | 0 | 200 | `sk_sweep` |
| Frappe au sol | action | 40 | 75 | 400 | `sk_smash` |

## Moves

- **Axe Kick**: Drop your heel ahead for 5–11 damage by level and slowness.
- **Balayage**: A low spinning strike knocks down and slows nearby targets.
- **Frappe au sol**: Bond puis plongeon vers ton regard : l'impact projette et blesse tout autour.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
