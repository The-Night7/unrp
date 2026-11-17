# Items and equipment

Item registration is in `item/ModItems.java`; behaviour is in `UnItems`, the dedicated item classes and `deal/`. Block registration is in `ModBlocks`. A registered item can have a model and name before its full design mechanic exists.

Runtime block and held models live under `assets/unrp/models/`. Clothing uses the worn UV textures consumed by `CosmeticLayer`; changing only an inventory icon does not change the worn object. `tools/design_equipment.py` builds the placed/held/worn assets, and its previews render those production models. See [ITEM_LORE.md](../ITEM_LORE.md) and [VISUAL_STYLE.md](../VISUAL_STYLE.md).

Contracts and soul items use the Pact ledger in `deal/`. Hollow Mark phylacteries use their own ownership and survival rules. Recovery Beds have saved owner, speed and restoration-ceiling settings; see [RECOVERY_BED.md](../RECOVERY_BED.md).

For an individual item, inspect its registered class before describing it as functional. Tooltips that explicitly say a system is not implemented identify a real limitation, not a missing texture.
