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

/**
 * Comportements des objets. Les identifiants, noms, quantités et règles de base suivent le modèle (Unrp) ;
 * les textes et les images sont les nôtres. Quand un objet dépend d'un système que le mod n'a pas encore
 * (Sarcophage, Marque vide, Négociateur, Titans…), il existe, porte ses données, mais n'agit pas : voir
 * docs/fidelite/objets.md.
 */
public final class UnItems {
    private UnItems() {}

    /** Données de joueur qui survivent à la mort. */
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

    // ------------------------------------------------------------------ objets sans comportement propre

    /** Objet qui n'a de sens que porté avec ses étiquettes : sans elles il se dissout dans l'inventaire. */
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

    /** Pourfendeur de dragon : chaque coup perce l'armure (2, ou 3,5 en Mk. II). Ni cassable, ni lâchable. */
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
                target.invulnerableTime = 0; // un second coup frais, qui ignore l'armure
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
            if (!player.level().isClientSide) bar(player, "§7Pourfendeur ne quitte pas la main du Moteur.");
            return false;
        }

        @Override
        public boolean isEnchantable(ItemStack stack) {
            return false;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            boolean m = tierOf(s) >= 2;
            tip(t, m ? "La lame de saint Georges, reforgée dans le feu du Tyran." : "La lame qui terrasse les dragons, liée au Moteur.", ChatFormatting.GREEN);
            tip(t, "Chaque coup perce aussi de " + (m ? "3,5" : "2") + ", à travers l'armure.", ChatFormatting.DARK_GREEN);
            tip(t, "Elle ne casse jamais, ne s'enchante pas, ne se lâche pas.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Épée d'âme : un quart de son tranchant frappe en dégâts bruts. Ne se lâche pas. */
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
            if (!player.level().isClientSide) bar(player, "§bL'épée d'âme appartient au pacte. Elle reste.");
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
            tip(t, "Tirée du rang même du Négociateur.", ChatFormatting.AQUA);
            tip(t, "Un quart de son tranchant (1,75) frappe en dégâts bruts.", ChatFormatting.DARK_AQUA);
            tip(t, "Ne se lâche pas.", ChatFormatting.DARK_GRAY);
        }
    }

    // ------------------------------------------------------------------ objets d'usage

    /** Livre unOrdinary : se lit une fois, laisse une marque durable. */
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
                    bar(sp, "§7Les pages sont usées. Tu les connais par cœur.");
                } else {
                    d.putBoolean("unrp_book_read", true);
                    sp.sendSystemMessage(Component.literal("§f§l== unOrdinary =="));
                    sp.sendSystemMessage(Component.literal("§7Un livre interdit, d'un monde où presque tous ont une capacité"));
                    sp.sendSystemMessage(Component.literal("§7et où les forts dominent les faibles parce qu'ils le peuvent."));
                    sp.sendSystemMessage(Component.literal("§7Contre cet ordre, il prêche une seule hérésie :"));
                    sp.sendSystemMessage(Component.literal("§7les forts existent pour protéger les faibles."));
                    sp.sendSystemMessage(Component.literal("§bQuelque chose s'apaise en toi. Les faibles valent d'être protégés."));
                    com.myriam.unrp.social.Reputation.addRaw(sp, 15); // UnOrdinaryBookItem : +15 à la réputation
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Clic droit pour lire.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Sérum de réécriture : on le boit, l'OC perd sa capacité et en tire une nouvelle, libre, avec de nouveaux potentiels. */
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
                bar(sp, "§7Le sérum glisse sans effet : seul un OC avec capacité peut être réécrit.");
                return stack; // pas consommé
            }
            String old = pr.ocAbility;
            String fresh = claims.rollFreeAbility(sp.getUUID(), new java.util.Random());
            claims.setOcAbility(sp.getUUID(), fresh);
            ServerLevel sl = sp.serverLevel();
            sl.playSound(null, sp.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 0.6f);
            sl.playSound(null, sp.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 0.6f, 1.4f);
            com.myriam.unrp.ability.Vfx.emit(sl, net.minecraft.core.particles.ParticleTypes.ENCHANT, sp.getX(), sp.getY() + 1, sp.getZ(), 30, 0.5, 0.8, 0.5, 0.4);
            sp.sendSystemMessage(Component.literal("§dTa capacité est arrachée" + (old != null ? " (" + old + ")" : "") + ". Une nouvelle prend racine : §f" + fresh));
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            return stack;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Un distillat NXGen instable : amplificateur et désactiveur", ChatFormatting.GRAY);
            tip(t, "forcés dans une même seringue.", ChatFormatting.GRAY);
            tip(t, "À boire : arrache ta capacité, une nouvelle prend racine.", ChatFormatting.DARK_PURPLE);
            tip(t, "Les potentiels sont retirés aussi.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Manuscrits (1 point) et tomes (2 points) d'une école d'Aura Manipulation. */
    public static class StudyBook extends Item {
        public static final String[] SCHOOLS = {"aug", "med", "proj", "con", "qtm"};
        public static final int[] MAXES = {4, 5, 6, 6, 8};
        public static final String[] NAMES = {"Augmentation", "Médecine", "Génération de projectiles", "Génération de constructions", "Manipulation quantique"};
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
                bar(sp, "§7Tu feuillettes quelques pages, mais rien n'a de sens pour toi.");
                return InteractionResultHolder.fail(stack);
            }
            int cur = study(sp, school), max = MAXES[school];
            if (cur >= max) {
                bar(sp, "§bIl ne reste rien à apprendre en " + NAMES[school] + ".");
                return InteractionResultHolder.fail(stack);
            }
            int next = Math.min(max, cur + points);
            persisted(sp).putInt("unrp_study_" + SCHOOLS[school], next);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            level.playSound(null, sp.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9f, 1f);
            double mult = 1.0 + 0.25 * next / max;
            bar(sp, "§d+" + (next - cur) + " théorie " + NAMES[school] + " §7(" + next + "/" + max + ", force de copie x" + String.format(java.util.Locale.FRANCE, "%.2f", mult) + ")");
            return InteractionResultHolder.success(stack);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "+" + points + " de théorie en " + NAMES[school] + " (max " + MAXES[school] + ").", ChatFormatting.GRAY);
            tip(t, "Réservé à Aura Manipulation. Le bonus de copie n'est pas encore appliqué en jeu.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Contrat : une seule pièce, cinq états (CustomModelData). Les clauses vivent ailleurs : pas de pacte en jeu. */
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
                case 2, 3 -> tip(t, "Un pacte avec une âme sur la table.", ChatFormatting.BLUE);
                case 4 -> tip(t, "À moitié vierge, plein d'espoir. Le demandeur écrit d'abord.", ChatFormatting.GOLD);
                case 5 -> tip(t, "Termes à l'encre rouge. Ils ne se brouillent pas.", ChatFormatting.DARK_RED);
                case 1 -> tip(t, "Seul le Négociateur peut le contresigner.", ChatFormatting.DARK_GRAY);
                default -> tip(t, "Les termes du Négociateur, en attente d'encre.", ChatFormatting.DARK_GRAY);
            }
            tip(t, "Les clauses ne sont pas encore appliquées en jeu.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Âme : porte le nom de son propriétaire. */
    public static class Soul extends Item {
        public Soul(Properties p) {
            super(p);
        }

        @Override
        public Component getName(ItemStack stack) {
            String n = stack.hasTag() ? stack.getTag().getString("unrp_soul_name") : "";
            return Component.literal(n.isEmpty() ? "Âme" : "Âme de " + n).withStyle(ChatFormatting.AQUA);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Ce qu'on lui fait est fait à son propriétaire.", ChatFormatting.DARK_AQUA);
            tip(t, "Elle ne se brise pas. Dépensée, elle est perdue.", ChatFormatting.DARK_GRAY);
        }
    }

    /** Éclat : brisé de l'Enchaîné, porte une génération et une mémoire d'ichor. Le système des Titans n'est pas porté. */
    public static class Shard extends Item {
        private final String label;

        public Shard(String label) {
            super(new Properties().stacksTo(1).fireResistant().rarity(net.minecraft.world.item.Rarity.EPIC));
            this.label = label;
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            tip(t, "Brisé de l'Enchaîné à Godfall. Il ne répond qu'à : " + label + ".", ChatFormatting.LIGHT_PURPLE);
            tip(t, "Un mortel qui l'utilise devient Titan, enfant de " + label + " (système pas encore porté).", ChatFormatting.GRAY);
            int g = s.hasTag() ? s.getTag().getInt("unrp_gen") : 0;
            if (g > 0) tip(t, "Génération " + g, ChatFormatting.DARK_GRAY);
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

    // ------------------------------------------------------------------ vêtements

    /**
     * Vêtement cosmétique : ce n'est PAS une armure. Il ne prend aucune case d'armure et ne protège pas ;
     * clic droit pour l'afficher ou le retirer, il reste dans l'inventaire. Les autres joueurs le voient.
     */
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
            tip(t, "Vêtement cosmétique : aucune protection, aucune case d'armure.", ChatFormatting.GRAY);
            tip(t, "Clic droit : l'afficher ou le retirer.", ChatFormatting.DARK_GRAY);
        }
    }
}
