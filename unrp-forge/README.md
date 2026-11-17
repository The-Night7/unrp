# Unordinary RP — Forge 1.20.1

UNRP adds playable characters and OCs, aura-based abilities, school roles, groups, territories and duels. Characters and move definitions come from JSON; server-side Java handles the effects and validates actions.

Use Java 17, Python 3 and the checked-in Gradle wrapper. Put the development dependencies listed in [SETUP.md](docs/SETUP.md) in `libs/`, then run:

```bash
./gradlew build
./gradlew runClient
```

The mod JAR is written to `build/libs/unrp-0.1.0.jar`. The first build needs network access; later builds can use `--offline` once dependencies are cached.

In game, **H** opens the hub and **R** toggles your power. Open Controls to check your move bindings; other mods may use the same keys. `/unrp menu` opens character selection and `/unrp whoami` shows your current profile.

## Find the right guide

| Task | Guide |
| --- | --- |
| Build, dependencies and launch problems | [SETUP.md](docs/SETUP.md) |
| Locate code, textures and generated files | [Where is what](<docs/where is what.md>) |
| Add or change an ability | [ABILITY_DEVELOPMENT.md](docs/ABILITY_DEVELOPMENT.md) |
| Move fields, effects and controls | [POWERS.md](docs/POWERS.md) |
| Import characters or edit data | [DATA.md](docs/DATA.md) |
| OC rolls, mastery and training | [ROLL.md](docs/ROLL.md) |
| Hub, schools, groups and duels | [INTERFACE.md](docs/INTERFACE.md) |
| Procedural school campuses | [SCHOOLS.md](docs/SCHOOLS.md) |
| Covenant | [COVENANT.md](docs/COVENANT.md) |
| Recovery Bed and owner controls | [RECOVERY_BED.md](docs/RECOVERY_BED.md) |
| Runtime move reference | [fidelity](docs/fidelity/README.md) |
| Code structure | [ARCHITECTURE.md](docs/ARCHITECTURE.md) |

## Repository layout

- `src/main/java/com/myriam/unrp/`: gameplay, server data, networking and client rendering.
- `src/main/resources/data/unrp/unodex/`: characters, schools, ability definitions and roll tables.
- `src/main/resources/assets/unrp/`: models, textures, animations, sounds and language entries.
- `tools/`: data import and asset generators. Read a generator before using `--force`.
- `tools/overrides/`: manual data corrections applied after import.
- `src/test/`: regression tests. Rendering and multiplayer interactions also need in-game checks.

School generation is implemented, with one natural site per campus. Individual school guides document their references and adaptations. Origin ability status varies; check [ORIGIN.md](docs/ORIGIN.md) and the current move definitions rather than old project phases.

## Maintenance checks

```bash
python tools/check_english.py
python tools/test_english_import.py
python tools/test_equipment_assets.py
```

The asset checks require Pillow. Vanilla texture previews use the Minecraft 1.20.1 client JAR from the Forge cache, or `MINECRAFT_CLIENT_JAR` if supplied. `tools/update_power_reference.py` refreshes the move-reference tables from runtime JSON.
