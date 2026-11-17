# Armor Suit

Runtime definition: `ability_powers.json` → `powers.armor_suit`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Arc-boutant | action | 10 | 0 | 200 | `suit_brace` |
| Charge de garde | action | 12 | 0 | 120 | `suit_crash` |
| Plate Shedding | action | 20 | 0 | 400 | `suit_shed` |
| Salve de plaques | action | 16 | 0 | 160 | `suit_volley` |

## Moves

- **Arc-boutant**: For three seconds, incoming damage is halved and movement is slowed.
- **Charge de garde**: A shoulder charge stuns anyone hit with slowness and weakness.
- **Plate Shedding**: Plates burst outwards, then reform, granting speed and resistance.
- **Salve de plaques**: Five plates in a fan; each removes two armour from its target for eight seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
