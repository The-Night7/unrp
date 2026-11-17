# Armament Dominion

Runtime definition: `ability_powers.json` → `powers.armament_dominion`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Forger | action | 0 | 0 | 20 | `unrp.ad_forge_trigger` |
| Saisir | action | 0 | 0 | 5 | `unrp.ad_recall_trigger` |
| Balle d'armement | held | 0 | 8 | 60 | `passive` |
| Arsenal complet | action | 40 | 0 | 600 | `unrp.ad_full_trigger` |

## Moves

- **Forger**: Claim your held item for four aura plus value, or a weakened creature with an empty hand for six aura plus value. Store up to twelve armaments. Crouch to judge value or break the held armament.
- **Saisir**: With an empty hand, cycle stored armaments. Crouch to store the held one.
- **Balle d'armement**: Hold to focus aura at your fingertip for eight aura, then release a piercing shot dealing five, eight or twelve damage by charge.
- **Arsenal complet**: Tous tes armements s'envolent vers ta cible.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
