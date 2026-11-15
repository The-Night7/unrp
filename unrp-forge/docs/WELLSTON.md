# Wellston Private High School

Wellston has a dedicated, canon-based campus rather than the generic school plan. See the [floor-by-floor preview](wellston-preview.svg).

The build represents an intact school during the Safe House era. It is a compressed Minecraft interpretation, not a claim that the webtoon establishes a complete architectural floor plan.

## Reference basis

The [official prologue](https://www.webtoons.com/en/super-hero/unordinary/prologue/viewer?title_no=679&episode_no=1) shows the warm brown/pink façade, pale trim, tall repeated windows and cafeteria. Its cafeteria scene supplies the purple seating, white tables, green serving counter and monthly Triple Chocolate Cake detail used here.

The [school reference index](https://unordinary.fandom.com/wiki/Wellston_Private_High_School) identifies Wallik, Whesley and Manner halls, separate boys' and girls' dormitories, lockers, the library, infirmary and press team. The [Safe House reference](https://unordinary.fandom.com/wiki/Safe_House_%28club%29) identifies Room 224 and the club's peaceful, inclusive purpose. These fan-maintained pages are secondary references, not architectural plans. The [Room 224 opening summary](https://unordinary.fandom.com/wiki/Chapter_201) describes study space, laptops, games and reading seats.

## What changed

- A terracotta façade with pale sandstone trim and tall blue-glass windows replaces the generic white/gold exterior.
- Three named teaching blocks replace the single U-shaped building.
- The main block has locker-lined corridors, an infirmary, headmaster's office, Safe House Room 224 and a Wellston Weekly press room.
- The library includes shelves, desks and computer representations. The cafeteria has white tables, purple seats and a green serving counter.
- Separate boys' and girls' dormitory buildings have bedrooms with paired beds, furniture, corridors and stairs.
- A fenced flat rooftop is reachable from the main stairs through a service hut. The invented clock tower is removed.
- A landscaped courtyard replaces the prototype's central basketball court and fountain.

The monthly cake special is advertised on a sign. It is not an unlimited permanent cake supply or a simulated monthly food event. Computers, lockers and club furniture are block representations; this change does not add laptop software, student schedules, NPC staff, a minigame or Safe House combat enforcement.

## Canon versus adaptation

| Feature | Implementation decision |
| --- | --- |
| Three named halls | Their names are sourced; assigning the main teaching block to Wallik, the library block to Whesley and cafeteria block to Manner is an adaptation. |
| Room 224 | The room number is sourced. Its second-floor location and adjacency to the press room are Minecraft layout choices. |
| Building storeys | Three teaching floors and two dormitory floors fit the known multi-storey setting; these counts are not presented as verified counts for every canonical building. |
| Footprint | 129 × 129 blocks, selected for a playable campus. No canonical measurements are claimed. |
| Materials and furniture | Vanilla-block approximations of the references, not exact textures or models from the comic. |
| Courtyard, paths and bedroom allocation | Connections are designed to keep all major rooms reachable. No complete canonical map or character-to-room assignment is assumed. |
| One campus per world | The existing seed-based single-site generation rule is preserved. This is a mod rule, not a lore fact. |

## Code

- [WellstonBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WellstonBlueprint.java): the dedicated buildings, rooms, stairs and furnishings.
- [SchoolBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java): dispatches Wellston to its dedicated plan; Agwin also has a dedicated plan; all other schools now have dedicated layouts too.
- [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java): material mapping, dynamic footprint, saved layout version and chunk clipping.
- [SchoolStructure.java](../src/main/java/com/myriam/unrp/worldgen/SchoolStructure.java): terrain sampling and structure height use the selected plan's dimensions.
- [SchoolSites.java](../src/main/java/com/myriam/unrp/worldgen/SchoolSites.java): unchanged unique site algorithm; Wellston selects its own palette and three teaching floors.
- [WellstonPreview.java](../tools/WellstonPreview.java): renders ground, second, third and rooftop plans from the same geometry.

## Existing worlds and validation

This updates future generation. It does not overwrite an explored campus. Existing saved pieces without a layout version retain the old 73 × 73 plan and palette, including when their remaining chunks generate later, so an old building is not partly replaced by the larger plan. Use a fresh world for the revised Wellston. Locate its reserved site using `/unrp school locate wellston`.

Tests check required locations and labels, furniture, stairs all the way to the roof, two-block headroom, footprint bounds and routes from the front gate to the key facilities and both dormitories. A live Minecraft visual and terrain-generation check is still needed.
