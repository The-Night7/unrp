# UNRP visual identity

The current inventory artwork is drawn directly on a **16 × 16** pixel grid. It uses integer coordinates, opaque colour clusters, hard edges and discrete highlight/shadow colours. No generated illustration is sampled or downscaled into these textures. Related items share drawing templates and colour palettes.

[Actual textures, enlarged with nearest-neighbour sampling](unrp-items-preview.png) · [HUD preview](unrp-hud-preview.svg).

See [ITEM_LORE.md](ITEM_LORE.md) for reference-backed uniform/mask/medical corrections and the distinction between canon objects and RP adaptations.

## Where to edit

- [design_unrp_items.py](../tools/design_unrp_items.py) owns the native pixel drawings, selective outline/shading and icon preview. Run `python3 unrp-forge/tools/design_unrp_items.py` from the repository root; requires Pillow. There is no image-generation step.
- The script also uses the project's existing native pixel primitives and item/outfit palette tables in [gen_items.py](../tools/gen_items.py). Its old resource-generation entry point is not run.
- Packaged icons: `src/main/resources/assets/unrp/textures/item`, all 111 PNGs at 16 × 16. Pixel alpha is either 0 or 255.
- **Original HUD renderer restored:** [UnrpHudOverlay.java](../src/main/java/com/myriam/unrp/look/UnrpHudOverlay.java), including the rotating 3D player portrait, numeric values, original fill positions and existing anchor/scale/offset behaviour. Other ability overlays continue to use the original reported panel bounds.
- **Active HUD artwork:** [rpg_hud_back.png](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_back.png) and [rpg_hud_front.png](../src/main/resources/assets/unrp/textures/gui/hud/rpg_hud_front.png). Both are 200 × 80 RGBA PNGs. They are loaded by the original renderer. Back provides dark panel plates and meter tracks; front provides restrained pixel gold corner/edge highlights, with every original live-meter window transparent.
- **HUD texture source:** [design_hud_textures.py](../tools/design_hud_textures.py). Run `python3 unrp-forge/tools/design_hud_textures.py` to regenerate those exact PNGs. All coordinates are source texture pixels; there is no AI generation or downscaling. Inventory generation does not touch these files.
- The replacement Java-only card and its static portrait are retired. `unrp_status_frame.png` is unused.
- [Native pack manifest](../art/native-pixel-manifest.json) records the icon set and resolution.

The former AI sheets under `art/pixel-pack` are retained as unused historical sources. They are not inputs to the current generator or packaged resources. Rerunning the current script cannot restore that rejected pack.

Item IDs, model predicates and gameplay remain unchanged. Worn clothing and placed block/machine textures are outside this inventory icon change. A live Minecraft visual check remains necessary for GUI scale and in-hand rendering.

The [HUD preview](unrp-hud-preview.png) composites the actual back/front textures with sample fill rectangles. The empty portrait area is intentional: the original renderer draws the live 3D portrait separately. Numeric values and the actual aura colour also come from the game, so this is a texture/slot preview, not a screenshot. `preview_status_hud.py` now calls the original-layer texture generator rather than drawing the retired replacement layout.
