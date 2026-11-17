# Healing

Runtime definition: `ability_powers.json` → `powers.healing`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Self-Heal | held | 0 | 0 | 0 | `passive` |
| Marquer | action | 0 | 0 | 40 | `heal_mark` |
| Ranged Heal | held | 0 | 0 | 0 | `heal_external` |
| Emergency Heal | action | 30 | 0 | 600 | `heal_triage` |

## Moves

- **Self-Heal**: Hold to restore two HP per second.
- **Marquer**: Mark the targeted creature with a glow for ranged healing.
- **Ranged Heal**: Hold to apply Regeneration II to your marked target within ten blocks.
- **Emergency Heal**: Heal allies within eight blocks at once; you are weakened for four seconds.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
