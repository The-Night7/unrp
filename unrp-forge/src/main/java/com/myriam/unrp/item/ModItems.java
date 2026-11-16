package com.myriam.unrp.item;

import com.myriam.unrp.UnrpMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Objets du mod : la liste complète des objets d'Unrp (identifiants, noms, quantités, rareté), avec
 * des images et des textes à nous. Voir docs/fidelite/objets.md pour ce qui agit vraiment en jeu.
 * Les ressources sont produites par tools/gen_items.py.
 */
public final class ModItems {
    private ModItems() {}

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, UnrpMod.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UnrpMod.MOD_ID);

    /** Se porte sur la tête. */
    public static class Mask extends Item implements Equipable {
        public Mask(Properties p) {
            super(p);
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.HEAD;
        }
    }

    private static Item.Properties one() {
        return new Item.Properties().stacksTo(1);
    }

    private static Item.Properties sixteen() {
        return new Item.Properties().stacksTo(16);
    }

    private static RegistryObject<Item> block(String id, RegistryObject<net.minecraft.world.level.block.Block> b, Item.Properties p) {
        return ITEMS.register(id, () -> new BlockItem(b.get(), p));
    }

    public static final RegistryObject<Item> JOKER_MASK = ITEMS.register("joker_mask", () -> new Mask(one()));

    // --- amplification
    public static final RegistryObject<Item> AMPLIFIER = ITEMS.register("amplifier", () -> new Item(sixteen()));
    public static final RegistryObject<Item> DEAMPLIFIER = ITEMS.register("deamplifier", () -> new Item(sixteen()));
    public static final RegistryObject<Item> DISABLER = ITEMS.register("disabler", () -> new Item(sixteen()));
    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new Item(sixteen()));
    public static final RegistryObject<Item> SYRINGE_AMPLIFIER = ITEMS.register("syringe_amplifier", () -> new Item(sixteen()));
    public static final RegistryObject<Item> SYRINGE_DEAMPLIFIER = ITEMS.register("syringe_deamplifier", () -> new Item(sixteen()));
    public static final RegistryObject<Item> SYRINGE_DISABLER = ITEMS.register("syringe_disabler", () -> new Item(sixteen()));
    public static final RegistryObject<Item> SYRINGE_BLOOD = ITEMS.register("syringe_blood", () -> new UnItems.Lore(new Item.Properties(), ChatFormatting.DARK_RED, "Du sang, prélevé pour une analyse."));
    public static final RegistryObject<Item> REWRITER_SERUM = ITEMS.register("rewriter_serum", () -> new UnItems.RewriterSerum(one()));

    // --- objets de pouvoir
    public static final RegistryObject<Item> TOWER_CORE = ITEMS.register("tower_core", () -> new UnItems.Lore(new Item.Properties(), ChatFormatting.GOLD, "Le cœur d'une tour EMBER.", "Un noyau chaud qui bourdonne doucement."));
    public static final RegistryObject<Item> VITAL_SAND = ITEMS.register("vital_sand", () -> new com.myriam.unrp.uo.VitalSandItem(sixteen()));
    public static final RegistryObject<Item> OSSIFIED_TAIL = ITEMS.register("ossified_tail", () -> new com.myriam.unrp.uo.OssifiedTailItem(one()));
    public static final RegistryObject<Item> BINDING_LINK = ITEMS.register("binding_link", () -> new UnItems.Lore(sixteen().rarity(Rarity.RARE), ChatFormatting.GOLD,
        "Usage prévu : plafond de niveau de 10,0 à 20,0 (système de l'Enchaîné pas encore porté).", "Il ne répond qu'à son propriétaire."));
    public static final RegistryObject<Item> CARVED_RUNE = ITEMS.register("carved_rune", () -> new com.myriam.unrp.uo.CarvedRuneItem(sixteen().fireResistant()));
    public static final RegistryObject<Item> BONE_CHARM = ITEMS.register("bone_charm", () -> new com.myriam.unrp.uo.BoneCharmItem(one().fireResistant()));
    public static final RegistryObject<Item> SOUL = ITEMS.register("soul", () -> new com.myriam.unrp.deal.SoulItem(one().fireResistant().food(com.myriam.unrp.deal.SoulItem.SOUL_MEAL)));
    public static final RegistryObject<Item> SOUL_SWORD = ITEMS.register("soul_sword", () -> new com.myriam.unrp.deal.SoulSwordItem(one().fireResistant()));
    public static final RegistryObject<Item> UNORDINARY_BOOK = ITEMS.register("unordinary_book", () -> new UnItems.UnordinaryBook(one().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CONTRACT = ITEMS.register("contract", () -> new com.myriam.unrp.deal.ContractItem(one().fireResistant()));
    public static final RegistryObject<Item> PHYLACTERY = ITEMS.register("phylactery", () -> new Phylactery.PhylItem(ModBlocks.PHYLACTERY.get(), one().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SUNSTEEL_AXE = ITEMS.register("sunsteel_axe", () -> new com.myriam.unrp.uo.SunsteelAxeItem(one().fireResistant().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> SALT_SPEAR = ITEMS.register("salt_spear", () -> new com.myriam.unrp.uo.SaltSpearItem(one().fireResistant()));
    public static final RegistryObject<Item> ASCALON = ITEMS.register("ascalon", () -> new UnItems.Ascalon(one().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> ARMAMENT = ITEMS.register("armament", () -> new com.myriam.unrp.uo.ArmamentItem(one().fireResistant()));

    // --- éclats
    public static final RegistryObject<Item> EDICT_SHARD = ITEMS.register("edict_shard", () -> new UnItems.Shard("Édit"));
    public static final RegistryObject<Item> LIMIT_BREAKER_SHARD = ITEMS.register("limit_breaker_shard", () -> new UnItems.Shard("Briseur de limites"));
    public static final RegistryObject<Item> DRAGON_ENGINE_SHARD = ITEMS.register("dragon_engine_shard", () -> new UnItems.Shard("Moteur-dragon"));
    public static final RegistryObject<Item> SINGULARITY_SHARD = ITEMS.register("singularity_shard", () -> new UnItems.Shard("Singularité"));
    public static final RegistryObject<Item> SARCOPHAGE_SHARD = ITEMS.register("sarcophage_shard", () -> new UnItems.Shard("Sarcophage"));
    public static final RegistryObject<Item> CRIMSON_MARK_SHARD = ITEMS.register("crimson_mark_shard", () -> new UnItems.Shard("Marque cramoisie"));
    public static final RegistryObject<Item> DEALMAKER_SHARD = ITEMS.register("dealmaker_shard", () -> new UnItems.Shard("Négociateur"));
    public static final RegistryObject<Item> HOLLOW_MARK_SHARD = ITEMS.register("hollow_mark_shard", () -> new UnItems.Shard("Marque vide"));
    public static final RegistryObject<Item> COUNTERWEIGHT_SHARD = ITEMS.register("counterweight_shard", () -> new UnItems.Shard("Contrepoids"));
    public static final RegistryObject<Item> VERTIGO_SHARD = ITEMS.register("vertigo_shard", () -> new UnItems.Shard("Vertige"));

    // --- manuscrits et tomes
    public static final RegistryObject<Item> MANUSCRIPT_AUG = ITEMS.register("manuscript_aug", () -> new UnItems.StudyBook(sixteen(), 0, 1));
    public static final RegistryObject<Item> MANUSCRIPT_MED = ITEMS.register("manuscript_med", () -> new UnItems.StudyBook(sixteen(), 1, 1));
    public static final RegistryObject<Item> MANUSCRIPT_PROJ = ITEMS.register("manuscript_proj", () -> new UnItems.StudyBook(sixteen(), 2, 1));
    public static final RegistryObject<Item> MANUSCRIPT_CON = ITEMS.register("manuscript_con", () -> new UnItems.StudyBook(sixteen(), 3, 1));
    public static final RegistryObject<Item> MANUSCRIPT_QTM = ITEMS.register("manuscript_qtm", () -> new UnItems.StudyBook(sixteen(), 4, 1));
    public static final RegistryObject<Item> TOME_AUG = ITEMS.register("tome_aug", () -> new UnItems.StudyBook(sixteen().rarity(Rarity.UNCOMMON), 0, 2));
    public static final RegistryObject<Item> TOME_MED = ITEMS.register("tome_med", () -> new UnItems.StudyBook(sixteen().rarity(Rarity.UNCOMMON), 1, 2));
    public static final RegistryObject<Item> TOME_PROJ = ITEMS.register("tome_proj", () -> new UnItems.StudyBook(sixteen().rarity(Rarity.UNCOMMON), 2, 2));
    public static final RegistryObject<Item> TOME_CON = ITEMS.register("tome_con", () -> new UnItems.StudyBook(sixteen().rarity(Rarity.UNCOMMON), 3, 2));
    public static final RegistryObject<Item> TOME_QTM = ITEMS.register("tome_qtm", () -> new UnItems.StudyBook(sixteen().rarity(Rarity.RARE), 4, 2));

    // --- disques (la piste audio n'est pas livrée)
    public static final RegistryObject<Item> MUSIC_DISC_TURF_WAR = ITEMS.register("music_disc_turf_war", () -> new net.minecraft.world.item.RecordItem(8, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.turf_war")), one().rarity(Rarity.RARE), 480 * 20));
    public static final RegistryObject<Item> MUSIC_DISC_CROWN_MATCH = ITEMS.register("music_disc_crown_match", () -> new net.minecraft.world.item.RecordItem(9, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.crown_match")), one().rarity(Rarity.RARE), 279 * 20));
    public static final RegistryObject<Item> MUSIC_DISC_GUARDIAN = ITEMS.register("music_disc_guardian", () -> new net.minecraft.world.item.RecordItem(10, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.guardian")), one().rarity(Rarity.RARE), 309 * 20));
    public static final RegistryObject<Item> MUSIC_DISC_TYRANT = ITEMS.register("music_disc_tyrant", () -> new net.minecraft.world.item.RecordItem(11, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.tyrant")), one().rarity(Rarity.RARE), 245 * 20));
    public static final RegistryObject<Item> MUSIC_DISC_SURGE = ITEMS.register("music_disc_surge", () -> new net.minecraft.world.item.RecordItem(12, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.surge")), one().rarity(Rarity.RARE), 284 * 20));
    public static final RegistryObject<Item> MUSIC_DISC_THE_HUNT = ITEMS.register("music_disc_the_hunt", () -> new net.minecraft.world.item.RecordItem(13, () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation("unrp", "music_disc.the_hunt")), one().rarity(Rarity.RARE), 160 * 20));

    // --- œufs d'apparition (les créatures ne sont pas encore portées)
    public static final RegistryObject<Item> AGENT_SPAWN_EGG = ITEMS.register("agent_spawn_egg", () -> new UnItems.Lore(new Item.Properties(), ChatFormatting.DARK_GRAY, "Fait apparaître : agent EMBER.", "La créature n'est pas encore portée."));
    public static final RegistryObject<Item> AGENT_LEADER_SPAWN_EGG = ITEMS.register("agent_leader_spawn_egg", () -> new UnItems.Lore(new Item.Properties(), ChatFormatting.DARK_GRAY, "Fait apparaître : chef EMBER.", "La créature n'est pas encore portée."));
    public static final RegistryObject<Item> SPECTRE_SPAWN_EGG = ITEMS.register("spectre_spawn_egg", () -> new UnItems.Lore(new Item.Properties(), ChatFormatting.DARK_GRAY, "Fait apparaître : opérateur Spectre.", "La créature n'est pas encore portée."));

    // --- blocs
    public static final RegistryObject<Item> SALT_BLOCK_ITEM = block("salt_block", ModBlocks.SALT_BLOCK, new Item.Properties());
    public static final RegistryObject<Item> BARRIER_BLOCK_ITEM = block("barrier_block", ModBlocks.BARRIER_BLOCK, new Item.Properties());
    public static final RegistryObject<Item> BARRIER_BLOCK_AM_ITEM = block("barrier_block_am", ModBlocks.BARRIER_BLOCK_AM, new Item.Properties());
    public static final RegistryObject<Item> PARTICLE_BLOCK_ITEM = block("particle_block", ModBlocks.PARTICLE_BLOCK, new Item.Properties());
    public static final RegistryObject<Item> CONVERSION_MACHINE_ITEM = block("conversion_machine", ModBlocks.CONVERSION_MACHINE, new Item.Properties());
    public static final RegistryObject<Item> RECOVERY_MACHINE_ITEM = block("recovery_machine", ModBlocks.RECOVERY_MACHINE, new Item.Properties());
    public static final RegistryObject<Item> RECOVERY_BED_ITEM = block("recovery_bed", ModBlocks.RECOVERY_BED, new Item.Properties().stacksTo(1));
    public static final RegistryObject<Item> SUPPRESSION_FIELD_ITEM = block("suppression_field", ModBlocks.SUPPRESSION_FIELD, new Item.Properties());

    // --- vêtements cosmétiques (aucune protection)
    public static final RegistryObject<Item> WELLSTONE_BLAZER = ITEMS.register("wellstone_blazer", () -> new UnItems.Outfit("wellstone_uniform", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> WELLSTONE_SHIRT = ITEMS.register("wellstone_shirt", () -> new UnItems.Outfit("wellstone_shirt", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> WELLSTONE_TROUSERS = ITEMS.register("wellstone_trousers", () -> new UnItems.Outfit("wellstone_uniform", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> WELLSTONE_SHOES = ITEMS.register("wellstone_shoes", () -> new UnItems.Outfit("wellstone_uniform", EquipmentSlot.FEET, one()));
    public static final RegistryObject<Item> WELLSTONE_GIRL_BLAZER = ITEMS.register("wellstone_girl_blazer", () -> new UnItems.Outfit("wellstone_girl_uniform", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> WELLSTONE_GIRL_SHIRT = ITEMS.register("wellstone_girl_shirt", () -> new UnItems.Outfit("wellstone_girl_shirt", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> WELLSTONE_GIRL_SKIRT = ITEMS.register("wellstone_girl_skirt", () -> new UnItems.Outfit("wellstone_girl_uniform", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> WELLSTONE_GIRL_SHOES = ITEMS.register("wellstone_girl_shoes", () -> new UnItems.Outfit("wellstone_girl_uniform", EquipmentSlot.FEET, one()));
    public static final RegistryObject<Item> WELLSTONE_ONE_SHOULDER_BLAZER = ITEMS.register("wellstone_one_shoulder_blazer", () -> new UnItems.Outfit("wellstone_one_shoulder", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> WELLSTONE_SERAPHINA_BLAZER = ITEMS.register("wellstone_seraphina_blazer", () -> new UnItems.Outfit("wellstone_seraphina", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_BLAZER = ITEMS.register("agwin_blazer", () -> new UnItems.Outfit("agwin_uniform", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_SHIRT = ITEMS.register("agwin_shirt", () -> new UnItems.Outfit("agwin_shirt", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_TROUSERS = ITEMS.register("agwin_trousers", () -> new UnItems.Outfit("agwin_uniform", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> AGWIN_SHOES = ITEMS.register("agwin_shoes", () -> new UnItems.Outfit("agwin_uniform", EquipmentSlot.FEET, one()));
    public static final RegistryObject<Item> AGWIN_GIRL_BLAZER = ITEMS.register("agwin_girl_blazer", () -> new UnItems.Outfit("agwin_uniform", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_GIRL_SHIRT = ITEMS.register("agwin_girl_shirt", () -> new UnItems.Outfit("agwin_shirt", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_GIRL_SKIRT = ITEMS.register("agwin_girl_skirt", () -> new UnItems.Outfit("agwin_girl_uniform", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> AGWIN_GIRL_SHOES = ITEMS.register("agwin_girl_shoes", () -> new UnItems.Outfit("agwin_girl_uniform", EquipmentSlot.FEET, one()));
    public static final RegistryObject<Item> AGWIN_OFF_SHOULDER_BLAZER = ITEMS.register("agwin_off_shoulder_blazer", () -> new UnItems.Outfit("agwin_off_shoulder", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> AGWIN_ONE_SHOULDER_BLAZER = ITEMS.register("agwin_one_shoulder_blazer", () -> new UnItems.Outfit("agwin_one_shoulder", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> XSTATIC_TOP = ITEMS.register("xstatic_top", () -> new UnItems.Outfit("xstatic", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> XSTATIC_PANTS = ITEMS.register("xstatic_pants", () -> new UnItems.Outfit("xstatic", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> XSTATIC_BOOTS = ITEMS.register("xstatic_boots", () -> new UnItems.Outfit("xstatic", EquipmentSlot.FEET, one()));
    public static final RegistryObject<Item> EMBER_LEADER_HAT = ITEMS.register("ember_leader_hat", () -> new UnItems.Outfit("ember_hat", EquipmentSlot.HEAD, one()));
    public static final RegistryObject<Item> EMBER_LEADER_COAT = ITEMS.register("ember_leader_coat", () -> new UnItems.Outfit("ember_leader", EquipmentSlot.CHEST, one()));
    public static final RegistryObject<Item> EMBER_LEADER_PANTS = ITEMS.register("ember_leader_pants", () -> new UnItems.Outfit("ember_leader", EquipmentSlot.LEGS, one()));
    public static final RegistryObject<Item> EMBER_LEADER_BOOTS = ITEMS.register("ember_leader_boots", () -> new UnItems.Outfit("ember_leader", EquipmentSlot.FEET, one()));

    /** Même contenu que l'onglet du modèle : les objets liés à un pouvoir (hache, armement, lance, Pourfendeur, éclats…) n'y sont pas. */
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("unrp", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.unrp")).icon(() -> new ItemStack(AMPLIFIER.get())).displayItems((params, out) -> {
            out.accept(UNORDINARY_BOOK.get());
            out.accept(MANUSCRIPT_AUG.get());
            out.accept(MANUSCRIPT_MED.get());
            out.accept(MANUSCRIPT_PROJ.get());
            out.accept(MANUSCRIPT_CON.get());
            out.accept(MANUSCRIPT_QTM.get());
            out.accept(TOME_AUG.get());
            out.accept(TOME_MED.get());
            out.accept(TOME_PROJ.get());
            out.accept(TOME_CON.get());
            out.accept(TOME_QTM.get());
            out.accept(AMPLIFIER.get());
            out.accept(DEAMPLIFIER.get());
            out.accept(DISABLER.get());
            out.accept(SYRINGE.get());
            out.accept(SYRINGE_AMPLIFIER.get());
            out.accept(SYRINGE_DEAMPLIFIER.get());
            out.accept(SYRINGE_DISABLER.get());
            out.accept(SYRINGE_BLOOD.get());
            out.accept(REWRITER_SERUM.get());
            out.accept(VITAL_SAND.get());
            out.accept(OSSIFIED_TAIL.get());
            out.accept(CONTRACT.get());
            out.accept(SOUL.get());
            out.accept(SOUL_SWORD.get());
            out.accept(PHYLACTERY.get());
            out.accept(CONVERSION_MACHINE_ITEM.get());
            out.accept(SUPPRESSION_FIELD_ITEM.get());
            out.accept(RECOVERY_MACHINE_ITEM.get());
            out.accept(RECOVERY_BED_ITEM.get());
            out.accept(AGENT_SPAWN_EGG.get());
            out.accept(AGENT_LEADER_SPAWN_EGG.get());
            out.accept(SPECTRE_SPAWN_EGG.get());
            out.accept(MUSIC_DISC_TURF_WAR.get());
            out.accept(MUSIC_DISC_CROWN_MATCH.get());
            out.accept(MUSIC_DISC_GUARDIAN.get());
            out.accept(MUSIC_DISC_TYRANT.get());
            out.accept(MUSIC_DISC_SURGE.get());
            out.accept(MUSIC_DISC_THE_HUNT.get());
            out.accept(WELLSTONE_BLAZER.get());
            out.accept(WELLSTONE_SHIRT.get());
            out.accept(WELLSTONE_TROUSERS.get());
            out.accept(WELLSTONE_SHOES.get());
            out.accept(WELLSTONE_GIRL_BLAZER.get());
            out.accept(WELLSTONE_GIRL_SHIRT.get());
            out.accept(WELLSTONE_GIRL_SKIRT.get());
            out.accept(WELLSTONE_GIRL_SHOES.get());
            out.accept(WELLSTONE_ONE_SHOULDER_BLAZER.get());
            out.accept(WELLSTONE_SERAPHINA_BLAZER.get());
            out.accept(AGWIN_BLAZER.get());
            out.accept(AGWIN_SHIRT.get());
            out.accept(AGWIN_TROUSERS.get());
            out.accept(AGWIN_SHOES.get());
            out.accept(AGWIN_GIRL_BLAZER.get());
            out.accept(AGWIN_GIRL_SHIRT.get());
            out.accept(AGWIN_GIRL_SKIRT.get());
            out.accept(AGWIN_GIRL_SHOES.get());
            out.accept(AGWIN_OFF_SHOULDER_BLAZER.get());
            out.accept(AGWIN_ONE_SHOULDER_BLAZER.get());
            out.accept(XSTATIC_TOP.get());
            out.accept(XSTATIC_PANTS.get());
            out.accept(XSTATIC_BOOTS.get());
            out.accept(EMBER_LEADER_HAT.get());
            out.accept(EMBER_LEADER_COAT.get());
            out.accept(EMBER_LEADER_PANTS.get());
            out.accept(EMBER_LEADER_BOOTS.get());
            out.accept(JOKER_MASK.get());
            out.accept(TOWER_CORE.get());
            out.accept(SALT_BLOCK_ITEM.get());
            out.accept(BARRIER_BLOCK_ITEM.get());
            out.accept(BARRIER_BLOCK_AM_ITEM.get());
            out.accept(PARTICLE_BLOCK_ITEM.get());
        }).build());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        TABS.register(bus);
    }
}
