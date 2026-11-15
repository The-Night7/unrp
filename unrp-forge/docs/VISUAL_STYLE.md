# UNRP visual identity

Equipment now uses production models and textures across inventory, held, placed and worn representations. [Preview of the actual item models](unrp-items-preview.png). These are labelled orthographic software renders of the shipped JSON and PNGs, **not live Minecraft screenshots**. Their camera, lighting and pose differ from Minecraft; their meshes, UVs and texture pixels come from the production assets.

See [ITEM_LORE.md](ITEM_LORE.md) for canon references and RP adaptations.

## Equipment source of truth

- [design_equipment.py](../tools/design_equipment.py) builds the actual machine/block geometry and 16px materials, humanoid 64 × 64 garment UV sheets, and matching equipment item models. It regenerates machine collision cuboids from the same block geometry. Run `python3 unrp-forge/tools/design_equipment.py` from the repository root.
- All eight block item models inherit their actual placed block model. There are no separate illustrated icons for those blocks. Lit/unlit suppression models use matching geometry with separate material states.
- Clothing inventory models reference the **same** `textures/models/armor/<set>_layer_*.png` files as [CosmeticLayer.java](../src/main/java/com/myriam/unrp/client/CosmeticLayer.java). Left/right arms and legs have separate UVs; shoulder variants expose the appropriate sleeve. Skirts use an actual continuous skirt mesh. Boots show the corresponding ankle textures. The worn Joker mask uses the same head UV sheet as its item mesh.
- Shards use one cuboid crystal mesh in GUI and hand, replacing `forge:separate_transforms` with its unrelated GUI illustration. Dragonbane and its upgrade now also use textured geometry, retaining the upgrade predicate.
- [design_unrp_items.py](../tools/design_unrp_items.py) only draws the remaining ordinary sprite items (medical vials, books and similar handheld sprites). It skips textured mesh items, preventing their material atlases from being overwritten with thumbnails. Those sprites are the actual in-hand and inventory textures.
- Forty-three unreferenced clothing/block thumbnails were removed. Edit the production worn or block textures rather than an obsolete item PNG.
- [preview_equipment.py](../tools/preview_equipment.py) resolves actual item inheritance, model elements, face UVs and PNGs. It produces the preview and [texture-hash manifest](../art/equipment-preview-manifest.json). Run it after changes, or run the sprite generator, which calls it. It does not invent a replacement icon for a model.
- [test_equipment_assets.py](../tools/test_equipment_assets.py) checks block/item inheritance, clothing texture identity, shard consistency, UV references, preview texture hashes, mask rendering and collision geometry. Run `python3 unrp-forge/tools/test_equipment_assets.py`.

## HUD

- **Original HUD renderer restored:** [UnrpHudOverlay.java](../src/main/java/com/myriam/unrp/look/UnrpHudOverlay.java), including the rotating 3D player portrait, numeric values, original fill positions and existing anchor/scale/offset behaviour. Other ability overlays continue to use the original reported panel bounds.
- **Active HUD artwork:** [rpg_hud_back.png](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_back.png) and [rpg_hud_front.png](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_front.png). Both are 200 × 80 RGBA PNGs. They are loaded by the original renderer. Back provides dark panel plates and meter tracks; front provides restrained pixel gold corner/edge highlights, with every original live-meter window transparent.
- **HUD texture source:** [design_hud_textures.py](../tools/design_hud_textures.py). Run `python3 unrp-forge/tools/design_hud_textures.py` to regenerate those exact PNGs. All coordinates are source texture pixels; there is no AI generation or downscaling. Inventory generation does not touch these files.
- The replacement Java-only card and its static portrait are retired. `unrp_status_frame.png` is unused.

The former AI sheets under `art/pixel-pack` are retained as unused historical sources. They are not inputs to the current generator or packaged resources. Rerunning the current script cannot restore that rejected pack.

Item IDs, contract state predicates and weapon-upgrade predicates are retained. Machine collision/selection shapes now follow the remodeled geometry, including facing. New machine treatments or power mechanics are not added. A live Minecraft visual check remains necessary for item hand poses, clothing motion and interactions with other rendering mods.

The [HUD preview](unrp-hud-preview.png) composites the actual back/front textures with sample fill rectangles. The empty portrait area is intentional: the original renderer draws the live 3D portrait separately. Numeric values and the actual aura colour also come from the game, so this is a texture/slot preview, not a screenshot. `preview_status_hud.py` now calls the original-layer texture generator rather than drawing the retired replacement layout.
