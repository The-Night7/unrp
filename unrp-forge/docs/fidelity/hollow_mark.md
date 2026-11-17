# Hollow Mark

Runtime definition: `ability_powers.json` → `powers.hollow_mark`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Blink | action | 0 | 0 | 30 | `unrp.hm_blink_trigger` |
| Void Gaze | action | 0 | 0 | 60 | `unrp.hm_gaze_trigger` |
| Gale Slam | action | 0 | 0 | 45 | `unrp.hm_slam_trigger` |
| Bone Darts | action | 0 | 0 | 25 | `unrp.hm_darts_trigger` |
| Thrall | action | 0 | 0 | 30 | `unrp.hm_thrall_trigger` |
| Sympathy | action | 0 | 0 | 20 | `unrp.hm_sympathy_trigger` |
| Shade Step | action | 0 | 0 | 10 | `unrp.hm_shade_trigger` |
| Calcify | action | 0 | 0 | 80 | `unrp.hm_calcify_trigger` |
| Bend Time | action | 0 | 0 | 100 | `unrp.hm_bend_trigger` |

## Moves

- **Blink**: Cross the gray for twelve aura, less at higher ranks, over nine blocks, then twelve, twenty-four or thirty-plus by rank. Arrival tears and damages the area. Crouch for Grasp, holding a distant target for twenty aura.
- **Void Gaze**: Open the Eye for ten aura to see nearby living beings through stone. Crouch for Gray Veil, an eighteen-aura ash cloud that blinds and chokes.
- **Gale Slam**: An eighteen-aura cone of dead air knocks targets down and reflects arrows. Crouch for Briar, holding ground with stone thorns for twenty-two aura.
- **Bone Darts**: Fire a burst of bone shards for fourteen aura. Crouch, or hold a rune, to open the Codex and buy ranks with runes.
- **Thrall**: Bend the targeted creature for twenty-four aura: it follows and hunts for you. Beasts unlock first; humanoids and undead at rank two. Keep one to three servants. Crouch for Borrowed Eyes to see through the nearest servant for six aura.
- **Sympathy**: Link three to six enemies for sixteen aura: when one is hurt, the others take 30–75% of that damage. Crouch for Void Gift, lending a charm's strength to an ally for thirty aura.
- **Shade Step**: Toggle gray concealment while still or walking slowly: become invisible and lose monster attention at a small aura cost each second. Crouch for Doppel, sending a walking decoy ahead to draw monsters for twenty-two aura.
- **Calcify**: Petrify the targeted enemy for twenty-six aura: 2–12 seconds, halved against players.
- **Bend Time**: A bubble slows, then at rank two stops, time for enemies and their arrows. Costs forty aura and lasts 3–16 seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
