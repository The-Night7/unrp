# Aura Transference

Runtime definition: `ability_powers.json` → `powers.aura_transference`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Accorder | action | 0 | 0 | 5 | `unrp.at_attune_trigger` |
| Weave into Weapon | action | 0 | 0 | 40 | `unrp.at_imbue_item_trigger` |
| Weave into Person | action | 0 | 0 | 40 | `unrp.at_imbue_player_trigger` |

## Moves

- **Accorder**: Select the trait to weave. Crouch to dissolve your latest weave. Copy traits by punching an ability user bare-handed.
- **Weave into Weapon**: Weave the selected trait into your held weapon for ten aura. Each weave consumes one aura every forty ticks.
- **Weave into Person**: Weave the selected trait into the targeted person, or yourself while crouching, for ten aura.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
