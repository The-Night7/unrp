# Dealmaker

Runtime definition: `ability_powers.json` → `powers.dealmaker`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Contrat | action | 0 | 1 | 10 | `unrp.dm_a1_trigger` |
| Selector | action | 0 | 0 | 6 | `unrp.dm_a2_trigger` |
| Convocation | action | 0 | 1 | 10 | `unrp.dm_a3_trigger` |
| Soul Extraction | action | 0 | 1 | 10 | `unrp.dm_a4_trigger` |
| The Dealmaker | action | 0 | 0 | 8 | `unrp.dm_power_press` |
| Bind | action | 0 | 1 | 10 | `unrp.dm_a6_trigger` |
| Sous ma protection | action | 0 | 0 | 6 | `unrp.dm_a7_trigger` |
| Bound Infusion | action | 0 | 1 | 10 | `unrp.dm_a8_trigger` |
| Self-Infusion | action | 0 | 1 | 10 | `unrp.dm_a9_trigger` |

## Moves

- **Contrat**: Draft a blank contract, or cancel an unsigned contract you are holding.
- **Selector**: Select the held soul affected by your seals. Crouch to select all held souls.
- **Convocation**: Summon selected souls across the world. Crouch to travel to the first instead.
- **Soul Extraction**: Pull a soul into your hand. Crouch while holding it to shred it.
- **The Dealmaker**: The same power toggle as the first bar. Crouch while active to open the Ledger.
- **Bind**: Bind the soul ahead to a seat. Crouch to select the seat.
- **Sous ma protection**: Take a bound ally's wounds onto yourself. Press again to release; crouch to select the seat.
- **Bound Infusion**: Temporarily raise a bound ally's tier. Crouch to select the seat.
- **Self-Infusion**: Sacrifice your lowest bound seat for your own power.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
