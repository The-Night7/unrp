# Tremor

Runtime definition: `ability_powers.json` → `powers.tremor`. See [POWERS.md](../POWERS.md) for shared fields.

| Move | Input | Cost | Required aura | Cooldown (ticks) | Handler |
| --- | --- | ---: | ---: | ---: | --- |
| Earthquake | held | 0 | 4 | 0 | `tm_quake` |
| Faille | action | 24 | 0 | 160 | `tm_fault` |
| Magnitude | action | 6 | 0 | 20 | `tm_mag` |
| Seismic Shard | action | 34 | 0 | 240 | `tm_burst` |

## Moves

- **Earthquake**: Hold while grounded to shake the ground within eleven blocks. You remain stationary.
- **Faille**: A ground wave launches anything standing on it and deals six damage.
- **Magnitude**: Toggle magnitude to strengthen seismic hits. Costs one aura every eighteen ticks.
- **Seismic Shard**: Frappe le sol : un anneau de force repousse tout (16 blocs). Accroupi : Cataclysme, cinq ondes successives.

Special handlers can impose additional costs and timing beyond the common JSON fields. Check the handler before interpreting a zero cost as free use. These definitions describe configured behaviour; multiplayer and rendering still need in-game checks.
