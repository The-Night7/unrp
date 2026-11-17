# Sarcophage

Runtime definition: `ability_powers.json` → `powers.sarcophage`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Tail Grab | action | 0 | 0 | 20 | `unrp.sc_grab_trigger` |
| Duneride | action | 0 | 0 | 20 | `unrp.sc_lunge_trigger` |
| Grainshot | action | 0 | 0 | 20 | `unrp.sc_spray_trigger` |
| Sandshroud | toggle | 0 | 0 | 0 | `passive` |

## Moves

- **Tail Grab**: A tendril grabs within five blocks, or seven with four Vital Sand, for six aura with a four-second cooldown. Hold for three seconds; press again to throw. Held monsters die and drop Vital Sand, capped at eight per ten minutes. Players can escape by jumping. Crouch for Sand Coffin, costing fifteen aura with a sixteen-second cooldown: crush and suppress a cocooned target, then crouch-press again to finish. Phage: Devour / Spit.
- **Duneride**: Two tails: surf a trampling sand wave for one reserve per second; press again to crash seven blocks forwards. Crouch for Sever Tail, creating a hardened tail club anyone can carry. Phage: Burrow / Erupt, Sink.
- **Grainshot**: Three tails: fire seven plus tail-count hardened-grain jets over twenty-four blocks for eight aura with a five-second cooldown. Crouch with four tails for Tempest: a four-tailed avatar in a storm, draining two reserve per second. Phage: Roar, Obelisk.
- **Sandshroud**: Three tails: sand armour reduces damage by 40% and scrapes approaching targets, draining one reserve per second. Crouch during Tempest with sixty reserve to Unseal into Phage. Phage: Lash; crouch to seal again.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
