# Rowden High School

[Preview the pavilion campus](rowden-preview.svg).

Rowden now has a dedicated 97 × 97 layout: a three-floor teaching block, a two-floor library/arts pavilion and a two-floor student-commons pavilion, connected by paths around a landscaped quad. It includes classrooms, offices, an infirmary, library rooms, study/arts rooms, a cafeteria and club rooms.

## References and limits

The [Rowden Town reference](https://unordinary.fandom.com/wiki/Rowden_Town) identifies Rowden High School but provides no mapped campus architecture. The resort and amusement park in that reference are separate town locations. They have not been transplanted into the school.

The entire architectural arrangement, stone/red palette, room functions and dimensions are **original Minecraft adaptations** for the canon school. They are not asserted to be a reconstruction of a depicted campus. There is no invented canonical royal arena, residence assignment, school ranking or permanent ruler encoded in the building.

## Code and generation

[RowdenBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/RowdenBlueprint.java) defines the three pavilions and quad. [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) provides their common construction primitives; the `rowden` palette is selected in [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java).

Its one reserved site per Overworld is unchanged:

```mcfunction
/unrp school locate rowden
```

Layout version 4 affects new starts. Existing versions 1–3 keep their former two-floor design and red palette. See [SCHOOLS.md](SCHOOLS.md) for shared generation and verification details.

## Layout 6 update

The current generator retains this school’s architecture and adds facade depth, landscaping, lighting and an eight-block terrain apron around the previous footprint. New starts use version 6; the dimensions and version notes above describe the original dedicated design. See [SCHOOLS.md](SCHOOLS.md) for current total footprints, compatibility and [exterior views](schools-exterior-preview.png). Already generated schools are not automatically replaced.
