package com.myriam.unrp.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.myriam.unrp.ability.AbilityEngine;
import com.myriam.unrp.data.ClaimData;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

public final class UnItems {
    private UnItems() {}

    public static CompoundTag persisted(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    static boolean marked(ItemStack s, String mark) {
        CompoundTag t = s.getTag();
        return t != null && t.getBoolean(mark);
    }

    static void bar(Player p, String msg) {
        p.displayClientMessage(Component.literal(msg), true);
    }

    static void tip(List<Component> out, String text, ChatFormatting c) {
        out.add(Component.literal(text).withStyle(c));
    }

    public static class Marked extends Item {
        private final String mark;

        public Marked(Properties p, String mark) {
            super(p);
            this.mark = mark;
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
            if (!level.isClientSide && !marked(stack, mark)) stack.setCount(0);
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return false;
        }

        @Override
        public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
            return false;
        }

        @Override
        public boolean isRepairable(ItemStack stack) {
            return false;
        }
    }

    public static class Ascalon extends SwordItem {
        public static final String NBT_TIER = "unrp_ascalon_tier";
        private static Multimap<Attribute, AttributeModifier> mk2;

        public Ascalon(Properties p) {
            super(Tiers.NETHERITE, 6, -2.4f, p);
        }

        public static int tierOf(ItemStack s) {
            return s.hasTag() && s.getTag().getInt(NBT_TIER) >= 2 ? 2 : 1;
        }

        @Override
        public Component getName(ItemStack stack) {
            return tierOf(stack) >= 2 ? Component.translatable("item.unrp.ascalon_mk2") : super.getName(stack);
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
            if (slot == EquipmentSlot.MAINHAND && tierOf(stack) >= 2) {
                if (mk2 == null) {
                    ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
                    b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 12.0, AttributeModifier.Operation.ADDITION));
                    b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", -2.4, AttributeModifier.Operation.ADDITION));
                    mk2 = b.build();
                }
                return mk2;
            }
            return super.getAttributeModifiers(slot, stack);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!target.level().isClientSide) {
                float pierce = tierOf(stack) >= 2 ? 3.5f : 2f;
                int keep = target.invulnerableTime;
                target.invulnerableTime = 0;
                target.hurt(target.level().damageSources().indirectMagic(attacker, attacker), pierce);
                if (keep > target.invulnerableTime) target.invulnerableTime = keep;
            }
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }

        @Override
        public boolean onDroppedByPlayer(ItemStack item, Player player) {
            if (!player.level().isClientSide) bar(player, "§7Dragonslayer cannot leave the Engine's hand.");
            return false;
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return false;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            boolean m = tierOf(s) >= 2;
            tip(t, m ? "Saint George's blade, reforged in the Tyrant's fire." : "The dragon-slaying blade, bound to the Engine.", ChatFormatting.GREEN);
            tip(t, "Each hit also deals " + (m ? "3.5" : "2") + ", bypassing armour.", ChatFormatting.DARK_GREEN);
            tip(t, "It never breaks, accepts no enchantments and cannot be dropped.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class SoulSword extends SwordItem {
        public SoulSword(Properties p) {
            super(Tiers.NETHERITE, 3, -2.2f, p);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!target.level().isClientSide) {
                int keep = target.invulnerableTime;
                target.invulnerableTime = 0;
                target.hurt(target.level().damageSources().magic(), 1.75f);
                if (keep > target.invulnerableTime) target.invulnerableTime = keep;
            }
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean onDroppedByPlayer(ItemStack item, Player player) {
            if (!player.level().isClientSide) bar(player, "§bThe soul sword belongs to the Pact. It stays.");
            return false;
        }

        @Override
        public boolean canBeDepleted() {
            return false;
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return false;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Drawn from the Dealmaker's own tier.", ChatFormatting.AQUA);
            tip(t, "A quarter of its edge (1.75) deals raw damage.", ChatFormatting.DARK_AQUA);
            tip(t, "Cannot be dropped.", ChatFormatting.DARK_GRAY);
        }
    }

    // ------------------------------------------------------------------ objets d'usage

    public static class UnordinaryBook extends Item {
        public UnordinaryBook(Properties p) {
            super(p);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer sp) {
                CompoundTag d = persisted(sp);
                if (d.getBoolean("unrp_book_read")) {
                    bar(sp, "§7The pages are worn. You know them by heart.");
                } else {
                    d.putBoolean("unrp_book_read", true);
                    sp.sendSystemMessage(Component.literal("§f§l== unOrdinary =="));
                    sp.sendSystemMessage(Component.literal("§7A banned book from a world where almost everyone has an ability"));
                    sp.sendSystemMessage(Component.literal("§7and the strong rule the weak simply because they can."));
                    sp.sendSystemMessage(Component.literal("§7Against that order, it preaches one heresy:"));
                    sp.sendSystemMessage(Component.literal("§7the strong exist to protect the weak."));
                    sp.sendSystemMessage(Component.literal("§bSomething inside you settles. The weak deserve protection."));
                    com.myriam.unrp.social.Reputation.addRaw(sp, 15);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Right-click to read.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class RewriterSerum extends Item {
        public RewriterSerum(Properties p) {
            super(p);
        }

        @Override
        public int getUseDuration(ItemStack stack) {
            return 32;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!(entity instanceof ServerPlayer sp) || level.isClientSide) return stack;
            ClaimData claims = ClaimData.get(sp.server);
            var pr = claims.profileOf(sp.getUUID()).orElse(null);
            if (pr == null || !ClaimData.Profile.OC.equals(pr.kind) || pr.lifelong) {
                bar(sp, "§7The serum has no effect. Only an OC with an ability can be rewritten.");
                return stack;
            }
            String old = pr.ocAbility;
            String fresh = claims.rollFreeAbility(sp.getUUID(), new java.util.Random());
            claims.setOcAbility(sp.getUUID(), fresh);
            ServerLevel sl = sp.serverLevel();
            sl.playSound(null, sp.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 0.6f);
            sl.playSound(null, sp.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 0.6f, 1.4f);
            com.myriam.unrp.ability.Vfx.emit(sl, net.minecraft.core.particles.ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1, sp.getZ(), 30, 0.5, 0.8, 0.5, 0.4);
            sp.sendSystemMessage(Component.literal("§dYour ability is torn away" + (old != null ? " (" + old + ")" : "") + ". A new one takes root: §f" + fresh));
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "An unstable NXGen distillate: an amplifier and a disabler", ChatFormatting.GRAY);
            tip(t, "forced into the same syringe.", ChatFormatting.GRAY);
            tip(t, "Drink it to tear out your ability and grow a new one.", ChatFormatting.DARK_PURPLE);
            tip(t, "Stat potentials are rerolled too.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class StudyBook extends Item {
        public static final String[] SCHOOLS = {"aug", "med", "proj", "con", "qtm"};
        public static final int[] MAXES = {4, 5, 6, 6, 8};
        public static final String[] NAMES = {"Augmentation", "Medicine", "Projectile Generation", "Construct Generation", "Quantum Manipulation"};
        private final int school, points;

        public StudyBook(Properties p, int school, int points) {
            super(p);
            this.school = school;
            this.points = points;
        }

        public static int study(Player p, int school) {
            return persisted(p).getInt("unrp_study_" + SCHOOLS[school]);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            if (!"aura_manipulation".equals(AbilityEngine.basePowerId(sp))) {
                bar(sp, "§7You turn a few pages, but none of it makes sense to you.");
                return InteractionResultHolder.fail(stack);
            }
            int cur = study(sp, school), max = MAXES[school];
            if (cur >= max) {
                bar(sp, "§bThere is nothing left to learn in " + NAMES[school] + ".");
                return InteractionResultHolder.fail(stack);
            }
            int next = Math.min(max, cur + points);
            persisted(sp).putInt("unrp_study_" + SCHOOLS[school], next);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, sp.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9f, 1f);
            double mult = 1.0 + 0.25 * next / max;
            bar(sp, "§d+" + (next - cur) + " theory " + NAMES[school] + " §7(" + next + "/" + max + ", copy strength x" + String.format(java.util.Locale.FRANCE, "%.2f", mult) + ")");
            return InteractionResultHolder.success(stack);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "+" + points + " theory in " + NAMES[school] + " (max " + MAXES[school] + ").", ChatFormatting.GRAY);
            tip(t, "For Aura Manipulation. The copy bonus is not implemented yet.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class Contract extends Item {
        public Contract(Properties p) {
            super(p);
        }

        private static int state(ItemStack s) {
            return s.hasTag() ? s.getTag().getInt("CustomModelData") : 0;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            switch (state(s)) {
                case 2, 3 -> tip(t, "A Pact with a soul at stake.", ChatFormatting.BLUE);
                case 4 -> tip(t, "Half blank, full of hope. The applicant writes first.", ChatFormatting.GOLD);
                case 5 -> tip(t, "Terms in red ink. They do not smudge.", ChatFormatting.DARK_RED);
                case 1 -> tip(t, "Only the Dealmaker can countersign it.", ChatFormatting.DARK_GRAY);
                default -> tip(t, "The Dealmaker's terms, awaiting a signature.", ChatFormatting.DARK_GRAY);
            }
            tip(t, "These clauses are not implemented yet.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class Soul extends Item {
        public Soul(Properties p) {
            super(p);
        }

        @Override
        public Component getName(ItemStack stack) {
            String n = stack.hasTag() ? stack.getTag().getString("unrp_soul_name") : "";
            return Component.literal(n.isEmpty() ? "Soul" : "Soul of " + n).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Whatever happens to it happens to its owner.", ChatFormatting.DARK_AQUA);
            tip(t, "It cannot break. Once spent, it is gone.", ChatFormatting.DARK_GRAY);
        }
    }

    public static class Shard extends Item {
        private final String label;

        public Shard(String label) {
            super(new Properties().stacksTo(1).fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC));
            this.label = label;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Broken from the Chained One at Godfall. It answers only to: " + label + ".", ChatFormatting.LIGHT_PURPLE);
            tip(t, "A mortal who uses it becomes a Titan, child of " + label + " (system not implemented yet).", ChatFormatting.GRAY);
            int g = s.hasTag() ? s.getTag().getInt("unrp_gen") : 0;
            if (g > 0) tip(t, "Generation " + g, ChatFormatting.DARK_GRAY);
        }
    }

    /** Objet d'ambiance : porte juste ses lignes de texte. */
    public static class Lore extends Item {
        private final String[] lines;
        private final ChatFormatting color;

        public Lore(Properties p, ChatFormatting color, String... lines) {
            super(p);
            this.lines = lines;
            this.color = color;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            for (String line : lines) tip(t, line, color);
        }
    }

    public static class Outfit extends Item {
        public final String set;
        public final EquipmentSlot slot;

        public Outfit(String set, EquipmentSlot slot, Properties p) {
            super(p);
            this.set = set;
            this.slot = slot;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && player instanceof ServerPlayer sp) Cosmetics.toggle(sp, this);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Cosmetic clothing: no armour protection or armour slot.", ChatFormatting.GRAY);
            tip(t, "Right-click to show or remove it.", ChatFormatting.DARK_GRAY);
        }
    }
}
