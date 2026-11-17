# Salt Seraph

Runtime definition: `ability_powers.json` → `powers.salt_seraph`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Lance de sel | action | 0 | 0 | 6 | `unrp.ss_lance_trigger` |
| Mur de sel | action | 0 | 0 | 6 | `unrp.ss_wall_trigger` |
| Salt Arrows | action | 0 | 0 | 6 | `unrp.ss_spires_trigger` |
| Seraph Awakening | action | 0 | 0 | 6 | `unrp.ss_wake_trigger` |

## Moves

- **Lance de sel**: Throw a salt spear for ten aura with a two-second cooldown: four damage, twenty-five salt and two-target piercing. Crouch for Great Spear, costing thirty-five aura with a thirty-second cooldown: eighteen damage, sixty salt and wall-impact burst.
- **Mur de sel**: Raise a seven-by-three salt wall ahead for twenty aura with a fifteen-second cooldown. It crumbles after ten seconds.
- **Salt Arrows**: Fire salt spikes in an eighteen-block cone for thirty aura with a twenty-two-second cooldown: eight damage, thirty-five salt and knockback. Crouch for Salt Seal, costing twenty-two aura with a sixteen-second cooldown: forty-five salt and a three-second root.
- **Seraph Awakening**: Black branches unfold from your back for fifty-five aura with a two-minute cooldown. Glide for one minute and deal 25% more damage. Salt affects everyone, including allies. Press again for Last Trumpet within fifteen blocks, applying sixty salt. Ends with twenty seconds of Weakness.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
