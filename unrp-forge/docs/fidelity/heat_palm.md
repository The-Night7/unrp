# Heat Palm

Runtime definition: `ability_powers.json` → `powers.heat_palm`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Frappe de paume | action | 10 | 0 | 70 | `hp_palm` |
| Paume de fonte | toggle | 1 | 1 | 0 | `hp_smelt` |

## Moves

- **Frappe de paume**: A white-hot palm deals 7–13 damage by level and ignites its target.
- **Paume de fonte**: Toggle cooking of the held item, one unit at a time.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
