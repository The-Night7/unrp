# Counterweight

Runtime definition: `ability_powers.json` → `powers.counterweight`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Absorb | held | 0 | 0 | 0 | `unrp.cw_absorb_trigger` |
| Wave | action | 0 | 0 | 20 | `unrp.cw_wave_trigger` |
| Fist | action | 0 | 0 | 15 | `unrp.cw_fist_trigger` |
| Point | action | 0 | 0 | 60 | `unrp.cw_point_trigger` |

## Moves

- **Absorb**: Tap for an eight-tick parry window, perfect for the first two ticks, with a 2.5-second cooldown. Hold Guard for a lower x4 capture rate at two aura every two ticks. Frontal hits are cancelled and banked at x10, or x15 on a perfect parry. Rear hits deal 25% extra damage and break the chain. Perfect parries chain later hits. Arrows unlock at stage II; explosions at stage IV.
- **Wave**: Spend at least twenty-four Bank charge in a 6.5-block air cone: 0.16 damage per charge and knockdown. Crouching at stage II uses Kinetic Bound: eighteen charge to leap towards your aim without landing damage.
- **Fist**: Spend at least thirty Bank charge on your next punch: +0.3 damage per charge. Crouching at stage IV uses Reknit: remain still for three seconds, then restore one HP per six charge.
- **Point**: Stage V: spend the entire Bank, at least sixty charge, on a twenty-four-block piercing spear. Deals 0.34 damage per charge, loses 15% per target and bypasses armour for 60% of its damage.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
