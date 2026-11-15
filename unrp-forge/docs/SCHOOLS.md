# Procedural schools

UNRP generates six furnished campuses directly from Java: **Wellston, Agwin, New Bostin, Rowden, Westview and Wellston University**. Ember and Vigilante are organizations, so they do not receive school buildings.

There is **one natural generation site for each school per Overworld**, not a repeating school every few regions. The sites are fixed by the world seed, independently of exploration order, chunk-generation threads, saves and restarts. No manual building, NBT template or additional scripting mod is required.

These are original campus layouts, not exact reproductions of canonical buildings. The five secondary schools have two floors; Wellston University has three. Palettes vary by school; Wellston and its university have clock towers.

[View the ground-floor plans](schools-preview.svg).

## Finding them

Install the matching mod build and create an Overworld with **Generate Structures enabled**. Explore to generate its school sites. Like villages, campuses are constructed when their chunks generate, rather than loading distant terrain immediately when the world is created.

Administrators can find a reserved site without generating or modifying terrain:

```mcfunction
/unrp school locate wellston
/unrp school locate agwin
/unrp school locate new_bostin
/unrp school locate rowden
/unrp school locate westview
/unrp school locate wellston_university
```

The result gives the campus centre's X/Z coordinates in the Overworld. It reports the planned site, not a claim that its chunks have already generated. Sites are in six separate 2,048-block regions near the world origin, with seed-dependent offsets; they are not relative to the player's current position or spawn point.

Existing worlds can generate a school only if its reserved starting chunk has not already generated. There is no retroactive placement, automatic rebuilding after demolition, or repeated placement on login. For all six campuses in a predictable initial state, use a fresh world. Disabling structures or excluding the school structure sets prevents generation, as with other Minecraft structures.

## Buildings

Each 73 × 73 campus has:

- A fenced perimeter, entrance plaza, paths, trees and lighting.
- A main teaching block and two connected wings.
- Classrooms with desks, chairs and boards, staff offices and an infirmary with beds.
- Stairs linking all floors, corridors and courtyard exits.
- A furnished cafeteria and kitchen, an upstairs library, study/lab rooms, a gymnasium and an outdoor sports court.
- A courtyard fountain, school sign and room signs in English.

This adds buildings; it does not spawn students or teachers, assign characters to beds, or claim social territories.

The site is not discarded because it falls in an ocean or on a slope: the campus uses a level platform above sampled terrain, with foundations extending down to solid ground. This preserves the single reserved site, but can produce a raised campus on steep terrain or an island platform over water. Standard Overworld biomes are eligible. Custom world generators/biomes can change eligibility and terrain results; no campus blocks are placed in the Nether, End or other dimensions.

## Code and data

| Purpose | Location |
| --- | --- |
| Six school IDs, fixed regions and seed-derived unique sites | [SchoolSites.java](../src/main/java/com/myriam/unrp/worldgen/SchoolSites.java) |
| Building geometry and furniture | [SchoolBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java) |
| World-generation entry point and terrain height | [SchoolStructure.java](../src/main/java/com/myriam/unrp/worldgen/SchoolStructure.java) |
| Chunk-clipped block placement, palettes, signs and NBT persistence | [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java) |
| Structure type and structure piece registration | [SchoolWorldgen.java](../src/main/java/com/myriam/unrp/worldgen/SchoolWorldgen.java) |
| Six structure definitions | [worldgen/structure](../src/main/resources/data/unrp/worldgen/structure) |
| Minecraft placement rules | [worldgen/structure_set](../src/main/resources/data/unrp/worldgen/structure_set) |
| Locate command | [UnrpCommands.java](../src/main/java/com/myriam/unrp/command/UnrpCommands.java) |
| Preview renderer | [SchoolPreview.java](../tools/SchoolPreview.java) |

All six structure sets use vanilla `random_spread` placement with spacing 128, separation 64 and salt 91827463. `SchoolStructure.findGenerationPoint` accepts only that school's one seed-derived chunk in its assigned region. Every other placement candidate is rejected. The random source is checked against Minecraft's own `WorldgenRandom` implementation, including negative regions and extreme seeds.

Geometry is a list of ordered boxes and labels. Worldgen paints each box only inside the current chunk's clipping bounds. Minecraft stores the structure start and piece data in the normal chunk NBT, including the school ID and origin; an explored campus is not rebuilt on restart. No shared mutable "already spawned" flag or cross-chunk placement queue is needed.

Keep `SchoolSites`' spacing/separation/salt in sync with the structure-set JSON. Tests check this correspondence and that all school-kind entries in `schools.json` are covered. Adding a new data-only school also requires a registered procedural design/site and its worldgen JSON.

## Preview and verification

Regenerate the preview from the repository root:

```bash
mkdir -p /tmp/unrp-school-preview
javac -encoding UTF-8 -d /tmp/unrp-school-preview \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolSites.java \
  unrp-forge/src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java \
  unrp-forge/tools/SchoolPreview.java
java -cp /tmp/unrp-school-preview SchoolPreview unrp-forge/docs/schools-preview.svg
```

Automated tests cover vanilla placement agreement, a single eligible location in a grid of candidate regions, unique school sites, data coverage, geometry bounds, unobstructed entrances and stair headroom, signs and essential furnishings. A live Minecraft world-generation and visual playtest is still required, especially for terrain joins, extreme landscapes and interactions with other world-generation mods.
