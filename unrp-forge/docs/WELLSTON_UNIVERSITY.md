# Wellston University

[Preview the mixed-height campus](wellston-university-preview.svg).

The university now has its own 161 × 161 campus rather than a recoloured high-school template. It combines three-floor brick medical/lecture buildings with eight-floor student residences, a seven-floor academic/library tower, landscaped grounds and connecting paths.

## Reference basis

[Official Episode 369](https://www.webtoons.com/en/super-hero/unordinary/episode-369/viewer?title_no=679&episode_no=388) shows a landscaped campus with brick buildings, pale modern multi-storey blocks, and a Medical Department teaching laboratory with benches, sinks, boards and computers. Those visual elements guide this design. The university dormitory is also mentioned in the [Chapter 280 synopsis](https://unordinary.fandom.com/wiki/List_of_Chapters), a secondary reference.

The Medical Department includes furnished teaching labs with counters, water cauldrons, brewing stands, boards and computers as vanilla-block approximations. The separate lecture building has admissions and lecture rooms. The academic tower includes a library, seminar rooms and study spaces. Student residences contain common rooms and paired beds.

## Adaptations

The footprint, relative building positions, eight/seven-floor tower counts, room numbers, residence allocation and complete interior plans are **compressed Minecraft choices**, not canonical measurements or a mapped university plan. No bedroom is assigned to a named character. The computers are decorative block representations; the labs do not implement ability-restoration medicine, a secret Spectre facility, recovery machines or playable experiments. There is no high-school royal hierarchy, Safe House copy or invented clock tower.

## Code and generation

[UniversityBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/UniversityBlueprint.java) defines the four buildings, tower window bands, pitched lower roofs and landscaping. [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) builds rooms, laboratories, residences and stairs. The `university` palette and tower materials are in [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java).

Its one seed-based site per Overworld remains unchanged:

```mcfunction
/unrp school locate wellston_university
```

New starts use layout version 4. Existing versions 1–3 retain the former 73 × 73 three-floor purple-accented campus, rather than being partly replaced by towers when adjacent chunks load. See [SCHOOLS.md](SCHOOLS.md) for the full generation and verification workflow.

## Layout 6 update

The current generator retains this school’s architecture and adds facade depth, landscaping, lighting and an eight-block terrain apron around the previous footprint. New starts use version 6; the dimensions and version notes above describe the original dedicated design. See [SCHOOLS.md](SCHOOLS.md) for current total footprints, compatibility and [exterior views](schools-exterior-preview.png). Already generated schools are not automatically replaced.
