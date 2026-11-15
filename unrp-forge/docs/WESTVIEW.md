# Westview

[Floor plans](westview-preview.svg) · [Exterior and roof plan](westview-exterior.svg).

Westview is an original UNRP school. Its revised campus adapts the three reference photographs supplied by the author: long modern glazed teaching blocks with pale concrete ends, muted blue-grey shallow roofs, low pavilions, covered paths and generous lawns. This is a Minecraft adaptation, not a measured replica or a claim about a canonical unOrdinary campus.

The **177 × 177** campus has eight buildings:

| Building | Floors | Use |
| --- | --- | --- |
| West and East Teaching Blocks | Four each | Classrooms and study rooms |
| Reception and Administration | Three | Admissions, offices, infirmary and student hubs |
| Science Block | Three | Laboratories |
| Arts and Humanities | Three | Classrooms and study spaces |
| North Academic Block | Four | Teaching rooms |
| Library Pavilion | One | Books and study |
| Dining Pavilion | One | Cafeteria |

Glass links connect reception to the front teaching blocks on three levels. Roofed pedestrian paths connect the other buildings around green courtyards. The front entrance combines tall concrete gateway piers and a lintel, metal gates, a glazed reception facade and a green leaf mural made from blocks. Three neutral school flags, parking bays and a perimeter drive complete the approach. Furnished interiors, stairs, dimensions and room assignments are gameplay adaptations.

## Code and generation

[WestviewBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/WestviewBlueprint.java) defines the buildings, curtain walls, roofs, links, gateway, leaf mural and grounds. [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) supplies furnished rooms and stairs. The `westview_modern` palette and additional road, gate, mural and flag materials live in [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java).

The seed-based single campus site is unchanged:

```mcfunction
/unrp school locate westview
```

New structure starts use layout version **5**. Saved version 4 keeps the original 97 × 97 H-shaped campus through [WestviewV4Blueprint.java](../src/main/java/com/myriam/unrp/worldgen/WestviewV4Blueprint.java), including its former palette. Versions 1–3 retain the generic cyan template. Explored buildings are not replaced; use a fresh world for the complete revised campus. See [SCHOOLS.md](SCHOOLS.md) for generation rules and preview commands.
