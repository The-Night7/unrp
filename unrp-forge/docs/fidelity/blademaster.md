# Blademaster

Runtime definition: `ability_powers.json` → `powers.blademaster`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Energy Blade | action | 6 | 0 | 20 | `bm_blade` |
| Aura Blades | action | 10 | 0 | 50 | `bm_ranged` |
| Arc d'aura | action | 14 | 0 | 90 | `bm_arc` |
| Manifestation | action | 18 | 0 | 160 | `bm_manifest` |
| Domain of a Thousand Blades | action | 40 | 0 | 520 | `bm_domain` |

## Moves

- **Energy Blade**: Manifest twin katanas for +3.5 damage and fast attacks. Crouch for a slower great blade with +8 damage and +2 reach. Press again to dismiss.
- **Aura Blades**: Throw three aura blades.
- **Arc d'aura**: Un croissant d'aura traverse tout devant toi (10 blocs).
- **Manifestation**: Blades strike everything within four blocks.
- **Domain of a Thousand Blades**: Rain blades over the targeted area for five seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
