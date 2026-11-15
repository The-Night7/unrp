# Procedural schools

All six UNRP schools now have dedicated Java-generated campus layouts. No manual building, NBT template or extra scripting mod is required. Ember and Vigilante are organizations and do not receive school buildings.

| School | Footprint | Main / maximum floors | Guide |
| --- | --- | --- | --- |
| Wellston | 129 × 129 | Three teaching floors, two dormitory floors | [WELLSTON.md](WELLSTON.md) |
| Agwin | 97 × 97 | Five | [AGWIN.md](AGWIN.md) |
| New Bostin | 97 × 97 | Three | [NEW_BOSTIN.md](NEW_BOSTIN.md) |
| Rowden | 97 × 97 | Three in main block, two in pavilions | [ROWDEN.md](ROWDEN.md) |
| Westview | 177 × 177 | One-floor pavilions, three/four-floor teaching blocks | [WESTVIEW.md](WESTVIEW.md) |
| Wellston University | 161 × 161 | Three in lower halls, seven/eight in towers | [WELLSTON_UNIVERSITY.md](WELLSTON_UNIVERSITY.md) |

[View all six ground-floor plans](schools-preview.svg).

Canon references guide the depicted features of Wellston, Agwin, New Bostin and the university. Exact dimensions and unseen room arrangements remain adaptations. Rowden's architecture is original because the consulted references do not map its campus. Westview is an original UNRP school with architecture adapted from the author’s three campus photographs. The individual guides identify references and limits.

## Finding the campuses

There is **one natural generation site for each school per Overworld**. Sites are fixed by the world seed, independent of exploration order, chunk-generation threads, saves and restarts. Exploring more regions does not create extra copies.

Use a fresh Overworld with **Generate Structures enabled** for a complete new set. As with villages, distant campuses are built when their chunks generate, not by forcing all their terrain to load immediately at world creation.

Administrators can report a reserved site's centre without loading or modifying terrain:

```mcfunction
/unrp school locate wellston
/unrp school locate agwin
/unrp school locate new_bostin
/unrp school locate rowden
/unrp school locate westview
/unrp school locate wellston_university
```

The output gives Overworld X/Z coordinates for the planned site; it does not assert that the chunks have already generated. The six sites occupy separate 2,048-block regions near the world origin, with seed-dependent offsets. These distances are gameplay choices, not canonical travel distances, and are not relative to the player or spawn point.

Disabling structures, removing their structure sets or overriding biome eligibility prevents generation. Custom generators/biomes and other worldgen mods can alter the terrain result. Campus blocks are placed only in the Overworld.

## Existing worlds

There is no automatic retroactive placement, demolition recovery or rebuilding on login. An existing world can generate a revised campus only if its reserved starting chunk has not already generated.

Saved piece layouts and palettes are retained, including when a building's remaining chunks generate later:

| Saved layout | Design retained |
| --- | --- |
| 1 | Original 73 × 73 templates for all schools |
| 2 | Dedicated Wellston; original templates elsewhere |
| 3 | Dedicated Wellston and Agwin; original templates for the remaining four |
| 4 | All six dedicated layouts, including the former H-shaped Westview |
| 5 | Photo-inspired Westview; the other five designs retained |

Current changes do not overwrite explored campuses. A fresh world is the reliable way to see every revised layout.

## Buildings and terrain

Each design is furnished and has stairs between its usable floors, readable English signs and connecting paths. Facilities vary by campus: classrooms, infirmaries, libraries, cafeterias, offices, science/medical teaching rooms, residence rooms and study/common areas are listed in the school guides.

This adds physical buildings, not NPC students/teachers, lesson schedules, automatic social-territory claims, assigned character beds, laptop software, school-specific combat systems or recovery-machine gameplay.

A site is not discarded merely because it lies over water or uneven terrain. The campus platform sits above sampled terrain, with foundations extending through air/water to solid blocks. This preserves the single reserved site, but can produce a raised campus on slopes or an island-like platform over water. The university is larger and contains substantially more blocks than the secondary schools.

## Code and data

| Purpose | Location |
| --- | --- |
| Unique sites, six IDs and current palette/floor metadata | [SchoolSites.java](../src/main/java/com/myriam/unrp/worldgen/SchoolSites.java) |
| Current design dispatch, plan model and saved-version compatibility | [SchoolBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java) |
| Shared shell, furnished-room and staircase primitives | [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) |
| Wellston | [WellstonBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WellstonBlueprint.java) |
| Agwin | [AgwinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/AgwinBlueprint.java) |
| New Bostin | [NewBostinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/NewBostinBlueprint.java) |
| Rowden | [RowdenBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/RowdenBlueprint.java) |
| Westview | [WestviewBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WestviewBlueprint.java) |
| University | [UniversityBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/UniversityBlueprint.java) |
| Terrain sampling and generation entry point | [SchoolStructure.java](../src/main/java/com/myriam/unrp/worldgen/SchoolStructure.java) |
| Chunk-clipped placement, materials, signs and saved piece versions | [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java) |
| Structure/piece registration | [SchoolWorldgen.java](../src/main/java/com/myriam/unrp/worldgen/SchoolWorldgen.java) |
| Structure definitions and Minecraft placement rules | [worldgen/structure](../src/main/resources/data/unrp/worldgen/structure), [worldgen/structure_set](../src/main/resources/data/unrp/worldgen/structure_set) |
| Locate command | [UnrpCommands.java](../src/main/java/com/myriam/unrp/command/UnrpCommands.java) |
| Vector previews | [SchoolPreview.java](../tools/SchoolPreview.java), [CampusPreview.java](../tools/CampusPreview.java) |

All six structure sets use vanilla `random_spread` placement with spacing 128, separation 64 and salt 91827463. `SchoolStructure.findGenerationPoint` accepts only the school's one seed-derived candidate in its assigned region. Every other candidate is rejected. Tests compare the site calculation with Minecraft's `WorldgenRandom`, including negative regions and extreme seeds.

Geometry is an ordered list of boxes and labels. Each piece paints only inside the current chunk's clipping bounds. Minecraft stores the normal structure start/piece data, with the school ID, origin and layout version; there is no shared mutable "already spawned" flag or cross-chunk placement queue.

Keep the spacing/separation/salt constants and placement JSON synchronized. When modifying a serialized layout again, introduce a new layout version and preserve older `forVersion`/`schoolForVersion` paths. Adding a school requires a design/site, worldgen definitions and matching data coverage.

## Regenerating previews

From the repository root:

```bash
mkdir -p /tmp/unrp-school-preview
javac -encoding UTF-8 -d /tmp/unrp-school-preview \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolSites.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/WellstonBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/AgwinBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/NewBostinBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/RowdenBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/WestviewV4Blueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/WestviewBlueprint.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/UniversityBlueprint.java \
  unrp-forge/tools/SchoolPreview.java \
  unrp-forge/tools/WellstonPreview.java \
  unrp-forge/tools/AgwinPreview.java \
  unrp-forge/tools/CampusPreview.java \
  unrp-forge/tools/WestviewPreview.java
java -cp /tmp/unrp-school-preview SchoolPreview unrp-forge/docs/schools-preview.svg
java -cp /tmp/unrp-school-preview WellstonPreview unrp-forge/docs/wellston-preview.svg
java -cp /tmp/unrp-school-preview AgwinPreview unrp-forge/docs/agwin-preview.svg
java -cp /tmp/unrp-school-preview CampusPreview new_bostin unrp-forge/docs/new-bostin-preview.svg
java -cp /tmp/unrp-school-preview CampusPreview rowden unrp-forge/docs/rowden-preview.svg
java -cp /tmp/unrp-school-preview CampusPreview westview unrp-forge/docs/westview-preview.svg
java -cp /tmp/unrp-school-preview WestviewPreview unrp-forge/docs/westview-exterior.svg
java -cp /tmp/unrp-school-preview CampusPreview wellston_university unrp-forge/docs/wellston-university-preview.svg
```

## Verification

Tests cover unique eligible sites, vanilla placement agreement, source-data coverage, geometry bounds, signs, stair headroom, usable entrances, furniture, all new room signs/building floors reachable from the front gate, and compatibility with earlier saved layouts. Wellston and Agwin's established dedicated layouts are checked for changes.

A live Minecraft world-generation and visual playtest remains necessary, especially for terrain joins, extreme landscapes, actual block-model behavior and interactions with other world-generation mods.
