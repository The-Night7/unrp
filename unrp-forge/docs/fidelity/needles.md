# Needles

Runtime definition: `ability_powers.json` → `powers.needles`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Aiguille | action | 3 | 0 | 12 | `nd_shot` |
| Fan | action | 8 | 0 | 60 | `nd_barrage` |
| Targeted Volley | held | 0 | 6 | 0 | `nd_volley` |

## Moves

- **Aiguille**: A fast needle dealing 2–5 damage.
- **Fan**: Seven needles in a fan, dealing 1.5–3.5 damage each.
- **Targeted Volley**: Hold for tight volleys of five needles, dealing 3–6 damage each. Costs six aura per volley.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
