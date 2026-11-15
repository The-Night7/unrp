# Agwin Private High School

Agwin now has a dedicated campus based on the school shown in [official Episode 374](https://www.webtoons.com/en/super-hero/unordinary/episode-374/viewer?title_no=679&episode_no=393), replacing the generic blue-accented U-shaped school.

[View the north elevation and all floor plans](agwin-preview.svg).

## Reference basis

The episode's establishing panel shows five visible storeys, a pale projecting centre with a shallow pediment, yellow and pink side panels, framed windows, a wide entrance canopy, a metal gate and trees. The following panels show a long windowed corridor, classroom signage **2-3**, rows of desks and a green classroom board.

These observations guide the exterior and teaching spaces. They do not establish the complete campus plan, the location of every facility, or an official Minecraft-scale measurement.

## Implemented layout

The build occupies 97 × 97 blocks. Its main teaching building has five floors, framed light-grey glazing, a pale tiled centre, pastel façade panels, a flat grey roof and a small service penthouse. The entrance rises two blocks above the grounds; stairs at the front and back connect the plinth to the paths.

Inside, side corridors face the exterior windows. Classrooms have glazed partitions, birch doors, rows of desks and chairs, green boards and ceiling lights. Classroom 2-3 is labelled explicitly. A central staircase connects every floor and reaches the roof.

The ground floor also contains an infirmary, administration room, library, cafeteria, staff room and study room. These support facilities and their locations are **gameplay adaptations**, not rooms whose exact positions were confirmed by the reference. The rear garden and roof access are adaptations too.

This design does not import Wellston's Safe House, newspaper, named halls or dormitory assignments into Agwin. It does not invent a canonical beacon spire, royal arena, house crest or permanent school ranking. The metal gate has an open pedestrian passage so players can enter without a redstone mechanism. Furnishings are vanilla-block approximations; this change does not add NPCs, classes, royal seats or Turf Wars gameplay.

## Canon versus adaptation

| Element | Treatment |
| --- | --- |
| Five-storey façade, projecting centre, side panels, canopy and gate | Based on the Episode 374 establishing view; shapes and proportions are approximated with blocks. |
| Corridor and classroom 2-3 | Based on the depicted corridor/classroom. Placing 2-3 on the second floor is a layout choice, not proof that the sign denotes a floor. |
| Building depth, room count, staircase and unseen façades | Adapted for a complete, traversable Minecraft building. |
| Support rooms, garden and accessible roof | Added for playability; not asserted as mapped canonical locations. |
| Materials | Quartz, pale sandstone, white terracotta, framed glazing and grey concrete approximate the drawing. |
| School politics and cast | No fixed ranking or character timeline is encoded into the building. |

## Generation and existing worlds

The unique seed-based Agwin site is unchanged. There is still only one natural Agwin campus per Overworld. Locate the planned site with:

```mcfunction
/unrp school locate agwin
```

The revised design is used for newly generated Agwin structure starts. Existing campus pieces with layout version 1 or 2 keep their 73 × 73 two-floor geometry and blue palette, including when their remaining chunks generate later. Wellston version 2 keeps its dedicated campus unchanged. New pieces use layout version 4; existing Agwin version 3 stays unchanged.

No explored buildings are overwritten or rebuilt. Use a fresh world with structures enabled to see the new Agwin design reliably. See [SCHOOLS.md](SCHOOLS.md) for the general generation rules and terrain limitations.

## Code and previews

- [AgwinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/AgwinBlueprint.java): dedicated façade, classrooms, corridors, stairs and grounds.
- [SchoolBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/SchoolBlueprint.java): current design selection and saved-version dispatch.
- [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java): materials, saved version and chunk-clipped block placement.
- [SchoolSites.java](../src/main/java/com/myriam/unrp/worldgen/SchoolSites.java): school metadata and unchanged unique-site algorithm.
- [AgwinPreview.java](../tools/AgwinPreview.java): north elevation and six floor slices rendered from the actual blueprint.

Tests cover the façade materials, classroom 2-3, stair headroom across all five storeys, routes through the raised entrance to the main rooms and roof, rear-garden access, structure bounds and saved-version compatibility. A live Minecraft terrain-generation and visual check remains necessary.
