# Procedural schools

All six UNRP schools now have dedicated Java-generated campus layouts. No manual building, NBT template or extra scripting mod is required. Ember and Vigilante are organizations and do not receive school buildings.

| School | Footprint | Main / maximum floors | Guide |
| --- | --- | --- | --- |
| Wellston | 145 × 145 | Three teaching floors, two dormitory floors | [WELLSTON.md](WELLSTON.md) |
| Agwin | 113 × 113 | Five | [AGWIN.md](AGWIN.md) |
| New Bostin | 113 × 113 | Three | [NEW_BOSTIN.md](NEW_BOSTIN.md) |
| Rowden | 113 × 113 | Three in main block, two in pavilions | [ROWDEN.md](ROWDEN.md) |
| Westview | 193 × 193 | One-floor pavilions, three/four-floor teaching blocks | [WESTVIEW.md](WESTVIEW.md) |
| Wellston University | 177 × 177 | Three in lower halls, seven/eight in towers | [WELLSTON_UNIVERSITY.md](WELLSTON_UNIVERSITY.md) |

[Exterior geometry previews](schools-exterior-preview.png) · [Ground-floor plans](schools-preview.svg). The exterior previews use the production layouts; material colours and partial-block shapes are simplified. They are not Minecraft screenshots, and the runtime terrain apron follows the local landscape.

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
| 6 | All six retain their architecture with an eight-block terrain apron, facade depth, roof details, warm lighting, floor inlays and planted courts |

Current changes do not overwrite explored campuses. A fresh world is the reliable way to see every revised layout.

## Buildings and terrain

Each design is furnished and has stairs between its usable floors, readable English signs and connecting paths. Facilities vary by campus: classrooms, infirmaries, libraries, cafeterias, offices, science/medical teaching rooms, residence rooms and study/common areas are listed in the school guides.

This adds physical buildings, not NPC students/teachers, lesson schedules, automatic social-territory claims, assigned character beds, laptop software, school-specific combat systems or recovery-machine gameplay.

A site is not discarded because of water or uneven terrain. New starts sample an 81-point terrain grid and fit their platform to a robust grade rather than lifting the whole campus onto one high sample. An eight-block exterior apron blends that grade into the terrain column by column. Hills are cut back above the buildings; fills use dirt near the surface and stone below. Steep mountains and deep water can still produce substantial earthworks; the fixed unique site is retained. The apron is terrain-dependent, so its exact slopes are not represented by the flat source-layout preview.

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
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolPlanGrid.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolPolish.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolTerrain.java \
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
  unrp-forge/tools/WestviewPreview.java \
  unrp-forge/tools/SchoolExteriorExport.java
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

## Sign initialization during generation

School sign text is initialized by `SchoolSignText` through block-entity NBT loading. Protochunk sign entities can have no attached `Level`; calling live sign text/wax setters there causes a `SignBlockEntity.markUpdated` null-pointer crash during feature placement. Both faces and waxing are saved without broadcasting a live block update. A regression test checks both faces contain four JSON text messages and the waxed flag.

## Generation and exterior improvements (layout 6)

`SchoolPlanGrid` compiles ordered geometry into final cells once. Placement clears terrain columns, installs final support blocks, then places attached/two-part fixtures. This avoids repeatedly placing and removing temporary doors, beds and signs. Iron bars and glass panes derive their connection states from the complete plan, including across chunk boundaries, so rails no longer rely on missing generation neighbour updates. The sign NBT initialization fix remains in place.

`SchoolPolish` preserves the earlier architectural identities and room assignments. It adds projected window sills/cornices, door canopies where applicable, facade base/pier bands, floor-edge inlays, hanging lanterns, slimmer outdoor lamps, native path edging, roof service details and planted courtyards. Existing tree crowns are trimmed to a less rectangular silhouette. Source versions 1–5 are retained.

The terrain/placement changes do not rebuild already explored buildings. Use a fresh world, or a reserved site whose starting chunk has not generated, for a complete layout-6 campus. There is no automatic destructive replacement of saved buildings.

[SchoolExteriorExport.java](../tools/SchoolExteriorExport.java) exports the exact production material grid; [preview_school_exteriors.py](../tools/preview_school_exteriors.py) renders it with simplified block colours/shapes. Compile the exporter with the pure-Java source list above and `SchoolPreview.java`, then run:

```bash
java -cp /tmp/unrp-school-preview SchoolExteriorExport /tmp/unrp-school-views
python3 unrp-forge/tools/preview_school_exteriors.py /tmp/unrp-school-views
```

New checks compare the compiled grid with ordered source painting, validate cut/fill interpolation and height limits, and check access to every room sign across all six new layouts. High entrance nameplates are checked for readability from a reachable nearby position. These tests do not replace a live Minecraft terrain/visual playtest.
