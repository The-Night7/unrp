# UNRP visual identity

The current inventory artwork is drawn directly on a **16 × 16** pixel grid. It uses integer coordinates, opaque colour clusters, hard edges and discrete highlight/shadow colours. No generated illustration is sampled or downscaled into these textures. Related items share drawing templates and colour palettes.

[Actual textures, enlarged with nearest-neighbour sampling](unrp-items-preview.png) · [HUD preview](unrp-hud-preview.svg).

See [ITEM_LORE.md](ITEM_LORE.md) for reference-backed uniform/mask/medical corrections and the distinction between canon objects and RP adaptations.

## Where to edit

- [design_unrp_items.py](../tools/design_unrp_items.py) owns the native pixel drawings, selective outline/shading and icon preview. Run `python3 unrp-forge/tools/design_unrp_items.py` from the repository root; requires Pillow. There is no image-generation step.
- The script also uses the project's existing native pixel primitives and item/outfit palette tables in [gen_items.py](../tools/gen_items.py). Its old resource-generation entry point is not run.
- Packaged icons: `src/main/resources/assets/unrp/textures/item`, all 111 PNGs at 16 × 16. Pixel alpha is either 0 or 255.
- Status HUD: a 196 × 78 portrait-and-bars layout, drawn directly in code with isolated gold corner brackets, a square player-skin portrait and cut-tip meters. There is no background-frame texture or decorative grain.
- [UnrpHudOverlay.java](../src/main/java/com/myriam/unrp/look/UnrpHudOverlay.java) draws the current player skin and hat layer, health/aura values, absorption, food, armour and momentum. Shading uses discrete pixel bands. Existing anchor, offset, scale and aura colour settings remain supported, with updated dimensions for dependent overlays. Vanilla underwater bubbles remain visible.
- [Native pack manifest](../art/native-pixel-manifest.json) records the icon set and resolution.

The former AI sheets under `art/pixel-pack` are retained as unused historical sources. They are not inputs to the current generator or packaged resources. Rerunning the current script cannot restore that rejected pack.

Item IDs, model predicates and gameplay remain unchanged. Worn clothing and placed block/machine textures are outside this inventory icon change. A live Minecraft visual check remains necessary for GUI scale and in-hand rendering.

The [HUD preview tool](../tools/preview_status_hud.py) reproduces the layout with sample values, a generic sample face and approximate text. It is a geometry preview, not an in-game screenshot. Run `python3 unrp-forge/tools/preview_status_hud.py` to regenerate it. The old frame PNG is unused; inventory regeneration no longer rewrites HUD art.
