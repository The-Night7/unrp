# Westview

[Preview the connected academic wings](westview-preview.svg).

Westview is an **original UNRP campus**, explicitly requested by the author. Its inclusion comes from the project's school data, and no claim is made that its building is a canonical unOrdinary location.

The 97 × 97 campus has two two-floor academic wings joined by an enclosed bridge on both levels. The bridge forms a student hub, with front and rear exits into landscaped courtyards. The west wing contains a library, administration, an infirmary and classrooms; the east wing has cafeteria/design spaces and upstairs science labs. The palette uses pale walls and cyan accents with copper-coloured roofs.

These architecture, room functions, names, colours and dimensions are original design choices. No canonical school hierarchy, royal roster or founder is invented.

## Code and generation

[WestviewBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WestviewBlueprint.java) defines the H-shaped arrangement and both bridge connections. [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) provides furnished rooms and stairways. Materials are selected by the `westview` palette in [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java).

The seed-based single campus site is unchanged:

```mcfunction
/unrp school locate westview
```

Version 4 is used for new starts. Saved versions 1–3 retain the former cyan-accented template. Existing buildings are not replaced. See [SCHOOLS.md](SCHOOLS.md) for the common generation rules and preview workflow.
