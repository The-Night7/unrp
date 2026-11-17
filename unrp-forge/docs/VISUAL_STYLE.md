# Models, textures and previews

UNRP uses Minecraft-style pixel art with restrained gold accents. Inventory, held, placed and worn views share their production models and textures. Edit those assets first, then regenerate the previews.

[Equipment model preview](unrp-items-preview.svg) · [HUD texture preview](unrp-hud-preview.svg) · [Item lore](ITEM_LORE.md)

## Equipment

`tools/design_equipment.py` builds machine geometry, 16px materials, clothing texture sheets and matching item models. It also derives machine collision cuboids from the block geometry.

Block items inherit their placed model. Clothing items use the same 64 × 64 armor sheets as `CosmeticLayer`; their sleeves, skirts and boots use the corresponding worn UVs. The Joker mask shares its head texture with the worn mesh. Shards and Dragonbane use textured geometry in both the inventory and the hand.

`tools/design_unrp_items.py` draws ordinary sprite items such as vials and books. It skips the textured models so it cannot overwrite their material sheets with thumbnails.

Run from the repository root:

```bash
python3 unrp-forge/tools/design_equipment.py
python3 unrp-forge/tools/preview_equipment.py
python3 unrp-forge/tools/test_equipment_assets.py
```

The preview renderer reads the shipped JSON, UVs and PNGs. Its camera and lighting differ from Minecraft. Use it to inspect the assets; check hand poses and clothing movement in game. `art/equipment-preview-manifest.json` records the textures used by the preview.

## HUD

`look/UnrpHudOverlay.java` draws the live portrait, values and meters. Anchor, scale and offset settings apply there. Other ability overlays use its reported panel bounds.

The renderer loads two 200 × 80 RGBA textures from `assets/unrp/textures/gui/hud/`:

| Texture | Contents |
| --- | --- |
| `rpg_hud_back.png` | Panel plates and meter tracks |
| `rpg_hud_front.png` | Gold edges and corners; transparent openings for the live meters |

`tools/design_hud_textures.py` generates both layers at their native pixel coordinates. Run it from the repository root:

```bash
python3 unrp-forge/tools/design_hud_textures.py
```

The HUD preview combines those layers with sample fills. The portrait is left empty because Minecraft renders the player there separately. Values and aura colour also come from runtime state.

## Old assets

`unrp_status_frame.png` is unused. The sheets under `art/pixel-pack/` are historical references and are not inputs to the current generators. Runtime assets belong under `src/main/resources/assets/unrp/`.

## Ability visual coverage

The [non-Origin visual audit](generated/ABILITY_VISUALS.md) covers animation lookup, icons, particle sprites and literal renderer resources. Its [per-move checklist](generated/ability-visual-checklist.csv) separates source checks from in-game review. Shared poses and effect glyphs are identified explicitly; their presence does not mean each ability has bespoke artwork.

Run `python3 tools/check_ability_visuals.py` from `unrp-forge/`. The same check runs during `./gradlew build`. Use `--write-report` to refresh the report after changes. `python3 tools/test_ability_visuals.py` checks that missing animations, missing icons and damaged PNGs cause failures.

Legacy poses are in `assets/unrp/poses.json`; indexed clips and network IDs are in `anim/MoveClips.java` and `anim/MoveClipIds.java`. Empty action poses now use existing gestures selected for their movement. Missing legacy names resolve to compatible shared clips. Generic power and move icons reuse existing effect glyphs, listed in `tools/overrides/shared_visual_icons.json`. No dedicated textures or animations are claimed for those shared entries.
