# Nightmare

Runtime definition: `ability_powers.json` → `powers.nightmare`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Champ de terreur | toggle | 0 | 1 | 0 | `nm_field` |
| Hallucination | action | 22 | 0 | 220 | `nm_hallucinate` |
| Pic de panique | action | 16 | 0 | 160 | `nm_panic` |

## Moves

- **Champ de terreur**: Toggle darkness within eight blocks, slowing nearby targets amid whispers.
- **Hallucination**: Terrify a target within eighteen blocks with slowness, darkness, blindness and weakness.
- **Pic de panique**: Grab the targeted enemy and throw them in terror.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
