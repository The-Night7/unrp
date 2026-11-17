# Particles

Runtime definition: `ability_powers.json` → `powers.particles`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Changer de motif | action | 0 | 0 | 10 | `unrp.pt_switch` |
| Condensation | action | 0 | 0 | 60 | `unrp.pt_condense` |
| Tissage | held | 0 | 0 | 0 | `passive` |
| Construction | action | 0 | 0 | 40 | `unrp.pt_construct` |
| Poing | action | 0 | 0 | 100 | `unrp.pt_fist` |

## Moves

- **Changer de motif**: Cycle patterns, or cycle backwards while crouching: Warden, Jailer, Vanguard, Architect. Active-pattern constructs awaken.
- **Condensation**: Pattern action: Warden protects an ally, Jailer cuffs a target, Vanguard fires a spike volley. Costs twelve aura and six motes.
- **Tissage**: Architect only: place a weave plate at your aim for six motes, lasting ten seconds.
- **Construction**: Raise your pattern's construct at your aim for twenty aura and fourteen motes. Blueprints recover after thirty seconds.
- **Poing**: A conjured fist strikes ahead for ten aura and eight motes. Crouch to crush a Marked target for fourteen aura and twelve motes.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
