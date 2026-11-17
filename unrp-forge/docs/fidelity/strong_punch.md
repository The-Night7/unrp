# Strong Punch

Runtime definition: `ability_powers.json` → `powers.strong_punch`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Haymaker | action | 14 | 0 | 160 | `sp_haymaker` |
| Bris de garde | action | 18 | 0 | 240 | `sp_break` |
| Frappe au sol | action | 40 | 75 | 300 | `sp_smash` |

## Moves

- **Haymaker**: Arm your next hit for five seconds: +8–16 damage by level and strong launch.
- **Bris de garde**: Arm your next hit for five seconds: break shields and make the target take 25% more damage.
- **Frappe au sol**: Strike the ground to launch and damage nearby targets.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
