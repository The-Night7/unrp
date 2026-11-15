# New Bostin High School

[Preview the floors and rooftop](new-bostin-preview.svg).

New Bostin has a dedicated 97 × 97 campus: a three-floor brick teaching block, linked rear balconies, locker corridors, classroom **2-B**, support rooms and a fenced rooftop reached by stairs.

## References and limits

[Official Episode 183](https://www.webtoons.com/en/super-hero/unordinary/episode-183/viewer?title_no=679&episode_no=194) supplies the flashback school setting and visible brick exterior walkways. The [school reference index](https://unordinary.fandom.com/wiki/New_Bostin_High_School) identifies John's former school, its rooftop and class 2-B. The reference index is secondary; the generated building is not an official floor plan.

The three-floor arrangement, precise balcony connections, classroom position, administration, infirmary, library, cafeteria and garden are compressed Minecraft choices. The rooftop and named classroom are included without spawning historical characters or replaying John's past. No field on this campus is labelled as the exact site of Claire's confrontation; that event's geography is not established by this layout.

## Code and generation

[NewBostinBlueprint.java](../src/main/java/com/myriam/unrp/worldgen/NewBostinBlueprint.java) defines the campus; [CampusBuilder.java](../src/main/java/com/myriam/unrp/worldgen/CampusBuilder.java) provides furnished rooms, shell construction and stairs. The `new_bostin` material palette is in [SchoolPiece.java](../src/main/java/com/myriam/unrp/worldgen/SchoolPiece.java).

The unique world-generation site is unchanged:

```mcfunction
/unrp school locate new_bostin
```

New structure starts use layout version 4. Saved versions 1–3 retain the former two-floor green-accented campus. See [SCHOOLS.md](SCHOOLS.md) for generation, old-world behavior, tests and preview commands.
