# Time Manipulation

Runtime definition: `ability_powers.json` → `powers.time_manipulation`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Changer de posture | action | 0 | 0 | 10 | `stance_next` |
| Flux du temps | toggle | 0 | 1 | 0 | `tm_flow` |
| Rewind | action | 32 | 0 | 300 | `tm_rewind` |
| Time Shift | action | 16 | 0 | 140 | `tm_shift` |
| Time Freeze | action | 40 | 0 | 400 | `tm_freeze` |
| Frappe temporelle | action | 50 | 0 | 520 | `tm_strike` |
| Delayed Strike | action | 40 | 0 | 360 | `tm_delayed` |

## Moves

- **Changer de posture**: Switch between Utility and Offensive move bars.
- **Flux du temps**: Toggle acceleration in Utility mode, or slow everything within eight blocks in Offensive mode.
- **Rewind**: Rewind yourself, or the targeted ally while crouching, by three seconds: restore position and health and remove negative effects.
- **Time Shift**: Jump eight blocks forwards. Crouch to return to the starting point.
- **Time Freeze**: Freeze creatures and projectiles ahead within twenty blocks for two to four seconds.
- **Frappe temporelle**: Appear behind a target within twelve blocks and strike.
- **Delayed Strike**: Mark a target within twelve blocks. The strike lands three seconds later.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
