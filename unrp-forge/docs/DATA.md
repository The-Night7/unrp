# Editing character and ability data

Runtime data lives in `src/main/resources/data/unrp/unodex/`. You can edit those JSON files directly or regenerate the character/school catalog from `tools/input/unodex-export.txt`.

Before importing, validate the source:

```bash
python tools/build_unodex.py --check
```

Run without `--check` to write generated files and `docs/generated/REPORT.md`. Manual overrides in `tools/overrides/` are merged after import. Keep long-term corrections there if the same catalog will be imported again.

## Files

| File | Contents |
| --- | --- |
| `characters/*.json` | Character IDs, English names/descriptions, levels, stats, school membership and snapshots. |
| `schools.json` | School/group IDs, members, leaders and campus metadata. |
| `abilities.json` | Ability descriptions and implementation references. |
| `ability_powers.json` | Playable moves, costs, conditions and visual settings. This is maintained separately from the sheet import. |
| `ability_effects.json` | Generic effects for abilities that use them. |
| `rarity.json` | Starting rarity weights and level ranges. |
| `ability_pool.json` | OC ability weights. |
| `stat_ranges.json` | Five starting-potential ranges per ability. |
| `pose_sounds.json` | Pose-to-sound mappings and layered sound signatures. |

The CSV source has sheets separated by `## Sheet name: <name>`. The importer currently reads `unodex` and `unodex-rp`; RP snapshots take priority where a person appears in both. Headers are English, including `School`, `Height` and `Rate`.

## Overrides and IDs

Overrides are keyed by ID. Nested dictionaries merge; lists and scalars replace the generated value. `_remove: true` removes an entry. Character snapshots retain historical information; the current character fields describe the selected latest record.

IDs already used by worlds must remain stable. The importer matches translated display names back to existing ability IDs rather than deriving a new ID solely from the English name. Some storage IDs therefore retain their original spelling. URLs, people's names and registry keys are not prose translations.

For new data, use lowercase snake_case IDs and English display text. Check references before removing an ability or school. Changing a label does not require changing its ID.

Restart after replacing packaged resources, or use a data pack and `/reload` for supported resource reloads. See [ABILITY_DEVELOPMENT.md](ABILITY_DEVELOPMENT.md) for adding gameplay rather than just a catalog entry.
