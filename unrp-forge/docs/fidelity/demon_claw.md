# Demon Claw

Runtime definition: `ability_powers.json` → `powers.demon_claw`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Demonic Leap | action | 12 | 0 | 140 | `dc_lunge` |
| Eviscerating Slash | action | 15 | 0 | 200 | `dc_ripper` |
| Feral Focus | toggle | 1 | 1 | 0 | `dc_feral` |

## Moves

- **Demonic Leap**: Leap forward and slash the first enemy for 7–15 damage by level.
- **Eviscerating Slash**: A heavy crossed slash hits everything ahead within 4.5 blocks for 9–18 damage.
- **Feral Focus**: Toggle larger claws for +4–11 damage and +12% movement speed.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
