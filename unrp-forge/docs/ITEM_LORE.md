# Item artwork and lore

The pack uses native pixel materials: 16px block/item materials and 64 × 64 humanoid UV sheets. Equipment inventory models, placed geometry and worn garments now use their production assets together. [Current texture sheet](unrp-items-preview.png).

## Supported visual corrections

| Items | Visual adaptation | Reference |
| --- | --- | --- |
| Joker's mask | Black cloth balaclava, dark eye openings, no ivory face plate, golden inlay or permanently glowing eyes | [Joker's Mask](https://unordinary.fandom.com/wiki/Joker%27s_Mask) |
| Wellston clothing | Teal jackets; white shirts; navy ties/bows; grey vest beneath the jacket; black trousers; navy skirts and dark socks/shoes. Shoulder variants retain this palette. No invented gold breast badge. | [Wellston uniform](https://unordinary.fandom.com/wiki/Wellston_Private_High_School) |
| Agwin clothing | Brown jacket with dark trim; white shirt and red tie; dark brown trousers/pleated skirt and brown socks. No burgundy school blazer or gold badge. | [Agwin uniform](https://unordinary.fandom.com/wiki/Agwin_High_School) |
| X-Static clothing | Grey top with brown upper-arm bands and utility belt/pouches, black trousers, brown shoes. No cyan-trim streetwear jacket; the worn sheet and 3D inventory garment share the same grey/brown materials. | [Rei's appearance](https://unordinary.fandom.com/wiki/Rei) |
| Disabler and filled syringe | Light-blue liquid in medical glass/injection equipment, not grey serum. Exact glass shape and a few pixels of shading are adaptations. | [Ability Disabler](https://unordinary.fandom.com/wiki/Ability_Disabler) |
| Recovery machine inventory icon | Hospital-style bed with an adjacent instrument screen, replacing the compact glowing-liquid reactor in both the item and placed block. The 16px bed/monitor arrangement is adapted. | [Recovery machine](https://unordinary.fandom.com/wiki/Recovery_machine) |

These references are secondary descriptions of the webtoon. The icons simplify those descriptions into sixteen pixels; they are not exact panel reproductions. Worn Wellston/Agwin UV sheets are now rebuilt with these palettes, collars, ties, vest/pleats and separate sleeve regions. Recovery is now a compact medical couch/monitor model both placed and in inventory, with matching selection/collision geometry. It remains a single-block adaptation and does not add treatment mechanics.

## Canon objects with incomplete visual verification

- **unOrdinary book:** William's in-universe book, not the published graphic novel. Its existence/author are supported by [Unordinary](https://unordinary.fandom.com/wiki/Unordinary). The revised icon is a plain charcoal book with pale print, retaining the project's earlier charcoal/white palette and removing fantasy metal corners. A full panel-accurate cover has not been verified in this pass; the tiny print marks are an abstraction, not a claim of exact typography.
- **Amplifier:** canonically an injectable drug carried in vials ([reference](https://unordinary.fandom.com/wiki/Ability_Amplifier)). A medical vial is used without a decorative gold clasp. The orange liquid colour is an existing gameplay palette, not a verified canon colour.
- **Dampener:** the series describes a pulse-emitting tool, with its exact device appearance unspecified in the consulted [reference](https://unordinary.fandom.com/wiki/Ability_Dampener). The mod's `deamplifier` vial and syringe are RP representations; they are not evidence that the canon dampener is an injectable blue potion. No mechanics or IDs are changed to imply that.
- **Spectre clothing and EMBER officer outfit:** generic faction-inspired designs; no claim that every faction member wears these exact garments.

## Original RP items and adaptations

The supplied catalog mixes webtoon items with RP content. Contracts/soul contracts, souls and soul swords, bone charms, vital sand, carved runes, binding links, rewriter serum, named shards, the Sunsteel Axe, salt spear, Dragonbane/upgrade, armament material variants, tower cores and the six music discs retain the project's own designs. No canon panel reference was established for those particular catalog objects. Spawn eggs and cubic placeable aura/particle blocks are Minecraft representations.

The gold HUD is a UNRP interface choice, not an object shown in the story. New art must follow established descriptions where available and be labelled as an adaptation where visual evidence is missing.

## Editing

[design_equipment.py](../tools/design_equipment.py) owns actual block/item meshes, worn sheets and clothing item meshes. [design_unrp_items.py](../tools/design_unrp_items.py) owns ordinary sprite items. [preview_equipment.py](../tools/preview_equipment.py) renders the actual resulting item model graph and texture UVs, without substituting the former icon drawings. See [VISUAL_STYLE.md](VISUAL_STYLE.md) for generation and consistency checks.
