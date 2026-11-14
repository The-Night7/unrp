package com.myriam.unrp.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Phylactère (Hollow Mark), repris de PhylacteryEvents + PhylacteryItem + PhylacteryLedger du modèle.
 * Posé par un porteur de la Marque, il se lie à lui (5 au plus). Tant qu'un phylactère se dresse, un coup mortel le laisse à 1 PV
 * et révèle un emplacement au tueur. Chaque phylactère lié qui n'est ni dressé ni sur lui coûte 1 PV max (jamais sous 6).
 * Cassé par un autre, il tombe chez lui : tenu en main, il montre la direction de son maître, et qui le porte le frappe 30 % plus fort.
 * Un phylactère que personne ne porte pendant une heure s'effrite. Le registre est sauvegardé avec le monde.
 */
public final class Phylactery {
    private Phylactery() {}

    public static final String NBT_SERIAL = "unrp_phyl_serial", NBT_OWNER = "unrp_phyl_owner", NBT_NAME = "unrp_phyl_name";
    static final int MAX_BOUND = 5;
    static final float HEALTH_EACH = 1f, LEAST_MAX = 6f, CARRIER_BONUS = 1.3f;
    static final long FORGET = 72000L;
    private static final UUID BURDEN_ID = UUID.fromString("b6e1f7a2-3c58-4d09-9e4b-5a72c18d03f6");
    static final int STANDING = 0, CARRIED = 1, DROPPED = 2, LOST = 3;

    // ------------------------------------------------------------------ registre (sauvegardé)
    public static final class Ledger extends SavedData {
        public static final class Entry {
            public long serial;
            public UUID owner;
            public String ownerName = "";
            public int state = CARRIED;
            public String dim = "";
            public int x, y, z;
            public long seen;
        }

        final Map<Long, Entry> entries = new LinkedHashMap<>();
        long next = 1;

        static Ledger get(MinecraftServer s) {
            return s.overworld().getDataStorage().computeIfAbsent(Ledger::load, Ledger::new, "unrp_phylacteries");
        }

        static Ledger load(CompoundTag t) {
            Ledger l = new Ledger();
            l.next = t.getLong("next");
            for (Tag x : t.getList("e", Tag.TAG_COMPOUND)) {
                CompoundTag c = (CompoundTag) x;
                Entry e = new Entry();
                e.serial = c.getLong("s");
                e.owner = c.getUUID("o");
                e.ownerName = c.getString("n");
                e.state = c.getInt("st");
                e.dim = c.getString("d");
                e.x = c.getInt("x");
                e.y = c.getInt("y");
                e.z = c.getInt("z");
                e.seen = c.getLong("seen");
                l.entries.put(e.serial, e);
            }
            return l;
        }

        @Override
        public CompoundTag save(CompoundTag t) {
            t.putLong("next", next);
            ListTag list = new ListTag();
            for (Entry e : entries.values()) {
                CompoundTag c = new CompoundTag();
                c.putLong("s", e.serial);
                c.putUUID("o", e.owner);
                c.putString("n", e.ownerName);
                c.putInt("st", e.state);
                c.putString("d", e.dim);
                c.putInt("x", e.x);
                c.putInt("y", e.y);
                c.putInt("z", e.z);
                c.putLong("seen", e.seen);
                list.add(c);
            }
            t.put("e", list);
            return t;
        }

        Entry entry(long serial) {
            return entries.get(serial);
        }

        List<Entry> mine(UUID owner) {
            List<Entry> out = new ArrayList<>();
            for (Entry e : entries.values()) if (e.owner.equals(owner) && e.state != LOST) out.add(e);
            return out;
        }

        List<Entry> standing(UUID owner) {
            List<Entry> out = new ArrayList<>();
            for (Entry e : mine(owner)) if (e.state == STANDING) out.add(e);
            return out;
        }

        long bind(UUID owner, String name, long now) {
            if (mine(owner).size() >= MAX_BOUND) return -1;
            Entry e = new Entry();
            e.serial = next++;
            e.owner = owner;
            e.ownerName = name;
            e.seen = now;
            entries.put(e.serial, e);
            setDirty();
            return e.serial;
        }

        void placed(long serial, String dim, BlockPos p, long now) {
            Entry e = entries.get(serial);
            if (e == null) return;
            e.state = STANDING;
            e.dim = dim;
            e.x = p.getX();
            e.y = p.getY();
            e.z = p.getZ();
            e.seen = now;
            setDirty();
        }

        Entry at(String dim, BlockPos p) {
            for (Entry e : entries.values()) if (e.state == STANDING && e.dim.equals(dim) && e.x == p.getX() && e.y == p.getY() && e.z == p.getZ()) return e;
            return null;
        }

        void loose(long serial, long now) {
            Entry e = entries.get(serial);
            if (e == null) return;
            e.state = CARRIED;
            e.seen = now;
            setDirty();
        }

        void seen(long serial, long now) {
            Entry e = entries.get(serial);
            if (e == null || e.state == LOST) return;
            if (e.state != STANDING) e.state = CARRIED;
            e.seen = now;
            setDirty();
        }

        void dropped(long serial) {
            Entry e = entries.get(serial);
            if (e != null && e.state == CARRIED) {
                e.state = DROPPED;
                setDirty();
            }
        }

        void lost(long serial) {
            Entry e = entries.get(serial);
            if (e != null) {
                e.state = LOST;
                setDirty();
            }
        }
    }

    // ------------------------------------------------------------------ outils
    static long now(MinecraftServer s) {
        return s.overworld().getGameTime();
    }

    static String dimOf(Level l) {
        return l.dimension().location().toString();
    }

    static long serialOf(ItemStack s) {
        CompoundTag t = s.getTag();
        return t != null && t.contains(NBT_SERIAL) ? t.getLong(NBT_SERIAL) : -1;
    }

    static UUID ownerOf(ItemStack s) {
        CompoundTag t = s.getTag();
        return t != null && t.hasUUID(NBT_OWNER) ? t.getUUID(NBT_OWNER) : null;
    }

    static String nameOf(ItemStack s) {
        CompoundTag t = s.getTag();
        return t == null ? "" : t.getString(NBT_NAME);
    }

    static void stamp(ItemStack s, Ledger.Entry e) {
        CompoundTag t = s.getOrCreateTag();
        t.putLong(NBT_SERIAL, e.serial);
        t.putUUID(NBT_OWNER, e.owner);
        t.putString(NBT_NAME, e.ownerName);
    }

    static boolean holds(ServerPlayer p) {
        return com.myriam.unrp.uo.HollowMarkEvents.holdsMark(p);
    }

    static void bar(ServerPlayer p, String s) {
        p.displayClientMessage(Component.literal(s), true);
    }

    static String where(Ledger.Entry e) {
        String d = switch (e.dim) {
            case "minecraft:overworld" -> "l'Overworld";
            case "minecraft:the_nether" -> "le Nether";
            case "minecraft:the_end" -> "l'End";
            default -> e.dim;
        };
        return e.x + ", " + e.y + ", " + e.z + " §7dans " + d;
    }

    static ServerLevel levelOf(MinecraftServer s, String dim) {
        try {
            return s.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(dim)));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    static boolean crumbled(ItemStack s, MinecraftServer server) {
        long serial = serialOf(s);
        if (serial < 0) return false;
        Ledger.Entry e = Ledger.get(server).entry(serial);
        return e == null || e.state == LOST || !e.owner.equals(ownerOf(s));
    }

    /** Les phylactères dressés qui existent encore (un bloc disparu est perdu). */
    static List<Ledger.Entry> standing(ServerPlayer p) {
        Ledger l = Ledger.get(p.server);
        List<Ledger.Entry> out = new ArrayList<>();
        for (Ledger.Entry e : l.standing(p.getUUID())) {
            ServerLevel there = levelOf(p.server, e.dim);
            if (there == null) continue;
            BlockPos pos = new BlockPos(e.x, e.y, e.z);
            if (there.isLoaded(pos) && !(there.getBlockState(pos).getBlock() instanceof PhylBlock)) l.lost(e.serial);
            else out.add(e);
        }
        return out;
    }

    static boolean onPerson(ServerPlayer p, long serial) {
        if (serialOf(p.containerMenu.getCarried()) == serial) return true;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (serialOf(p.getInventory().getItem(i)) == serial) return true;
        return false;
    }

    static int awayFrom(ServerPlayer p) {
        int n = 0;
        for (Ledger.Entry e : Ledger.get(p.server).mine(p.getUUID())) if (e.state != STANDING && !onPerson(p, e.serial)) n++;
        return n;
    }

    static boolean carries(ServerPlayer carrier, UUID owner) {
        for (int i = 0; i < carrier.getInventory().getContainerSize(); i++) {
            ItemStack st = carrier.getInventory().getItem(i);
            if (st.is(ModItems.PHYLACTERY.get()) && owner.equals(ownerOf(st))) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ bloc et objet
    /** PhylacteryBlock du modèle : orienté (FACING) vers le joueur à la pose. */
    public static final class PhylBlock extends ModBlocks.Facing {
        public PhylBlock(Properties p) {
            super(p);
        }

        @Override
        public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
            if (!level.isClientSide && !state.is(next.getBlock()) && level instanceof ServerLevel sl) removed(sl, pos);
            super.onRemove(state, level, pos, next, moved);
        }
    }

    private static UUID breaking;
    private static long breakingAt = -1;

    /** Qui casse le bloc (pour lui donner le phylactère). */
    public static void onBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent e) {
        if (e.getState().getBlock() instanceof PhylBlock && e.getPlayer() instanceof ServerPlayer sp) {
            breaking = sp.getUUID();
            breakingAt = now(sp.server);
        }
    }

    static void removed(ServerLevel lv, BlockPos pos) {
        MinecraftServer server = lv.getServer();
        Ledger l = Ledger.get(server);
        Ledger.Entry e = l.at(dimOf(lv), pos);
        ServerPlayer by = breaking != null && breakingAt == now(server) ? server.getPlayerList().getPlayer(breaking) : null;
        breaking = null;
        if (e == null) return;
        ItemStack drop = new ItemStack(ModItems.PHYLACTERY.get());
        stamp(drop, e);
        l.loose(e.serial, now(server));
        ServerPlayer owner = server.getPlayerList().getPlayer(e.owner);
        if (owner != null && owner != by) owner.sendSystemMessage(Component.literal(by == null ? "§bTon phylactère à " + where(e) + " §7a été arraché."
                : "§b" + by.getGameProfile().getName() + " §7a cassé ton phylactère à §f" + where(e) + "§7. Il l'a maintenant."));
        if (by != null && !by.getUUID().equals(e.owner)) bar(by, "§bTu as pris le phylactère de " + e.ownerName + ". §7Tiens-le et il montre son maître.");
        if (by == null || !by.getInventory().add(drop)) {
            Vec3 c = Vec3.atCenterOf(pos);
            ItemEntity it = new ItemEntity(lv, c.x, c.y, c.z, drop);
            it.setDefaultPickUpDelay();
            it.setUnlimitedLifetime();
            lv.addFreshEntity(it);
        }
    }

    public static final class PhylItem extends BlockItem {
        public PhylItem(Block b, Properties p) {
            super(b, p);
        }

        @Override
        public InteractionResult place(BlockPlaceContext ctx) {
            Level level = ctx.getLevel();
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(ctx.getPlayer() instanceof ServerPlayer p) || !(level instanceof ServerLevel sl)) return InteractionResult.FAIL;
            BlockPlaceContext real = updatePlacementContext(ctx);
            if (real == null || !real.canPlace()) return InteractionResult.FAIL;
            BlockPos pos = real.getClickedPos();
            ItemStack hand = ctx.getItemInHand();
            long serial = claim(p, hand);
            if (serial < 0) return InteractionResult.FAIL;
            InteractionResult r = super.place(ctx);
            if (r.consumesAction() && sl.getBlockState(pos).getBlock() == getBlock()) {
                Ledger.get(p.server).placed(serial, dimOf(sl), pos, now(p.server));
                int stand = Ledger.get(p.server).standing(p.getUUID()).size();
                bar(p, "§bLié. §7Tant qu'il se dresse, un coup mortel te laisse à 1 PV. §8" + stand + " debout.");
                sl.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1f, 0.5f);
            }
            return r;
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean selected) {
            if (level.isClientSide || !(holder instanceof ServerPlayer p) || p.tickCount % 10 != 0) return;
            long serial = serialOf(stack);
            if (serial < 0) return;
            if (crumbled(stack, p.server)) {
                stack.setCount(0);
                bar(p, "§8Un phylactère tombe en poussière dans ton sac.");
                return;
            }
            Ledger.get(p.server).seen(serial, now(p.server));
            boolean inHand = selected || p.getOffhandItem() == stack;
            UUID owner = ownerOf(stack);
            if (!inHand || owner == null) return;
            if (owner.equals(p.getUUID())) {
                if (p.tickCount % 20 == 0) bar(p, status(p));
            } else bar(p, track(p, owner, nameOf(stack)));
        }

        @Override
        public boolean onEntityItemUpdate(ItemStack stack, ItemEntity e) {
            Level level = e.level();
            if (level.isClientSide) return false;
            boolean falling = e.getY() < level.getMinBuildHeight() - 48;
            if (!falling && e.tickCount % 10 != 0) return false;
            long serial = serialOf(stack);
            if (serial < 0 || !(level instanceof ServerLevel sl)) return false;
            if (crumbled(stack, sl.getServer())) {
                e.discard();
                return true;
            }
            e.setUnlimitedLifetime();
            if (falling) {
                Ledger.get(sl.getServer()).lost(serial);
                UUID o = ownerOf(stack);
                ServerPlayer who = o == null ? null : sl.getServer().getPlayerList().getPlayer(o);
                if (who != null) who.sendSystemMessage(Component.literal("§8Un de tes phylactères est tombé hors du monde. §7Il ne te coûte plus rien."));
                e.discard();
                return true;
            }
            Ledger.get(sl.getServer()).dropped(serial);
            return false;
        }

        @Override
        public boolean canBeHurtBy(DamageSource src) {
            return false;
        }

        @Override
        public boolean isFoil(ItemStack s) {
            return serialOf(s) >= 0;
        }

        @Override
        public Component getName(ItemStack s) {
            String o = nameOf(s);
            return o.isEmpty() ? super.getName(s) : Component.literal("Phylactère de " + o);
        }

        @Override
        public void appendHoverText(ItemStack s, Level l, List<Component> t, TooltipFlag f) {
            if (serialOf(s) < 0) {
                t.add(Component.literal("Non lié. Pose-le en portant la Marque vide et il se lie à toi.").withStyle(ChatFormatting.AQUA));
                t.add(Component.literal("Tant qu'un phylactère se dresse, un coup mortel te laisse à 1 PV.").withStyle(ChatFormatting.GRAY));
                t.add(Component.literal("Chacun loin de toi coûte 1 PV max. Cinq au plus.").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                t.add(Component.literal("Lié à " + nameOf(s) + ".").withStyle(ChatFormatting.AQUA));
                t.add(Component.literal("Tiens-le : il montre son maître. Qui le porte le frappe plus fort.").withStyle(ChatFormatting.GRAY));
                t.add(Component.literal("Rien ne le brûle ni ne le fait sauter. Une heure sans porteur, il s'effrite.").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    static long claim(ServerPlayer p, ItemStack stack) {
        if (!holds(p)) {
            bar(p, "§8Il ne prend pas racine pour toi. Seule la Marque vide lie un phylactère.");
            return -1;
        }
        Ledger l = Ledger.get(p.server);
        long serial = serialOf(stack);
        if (serial >= 0) {
            Ledger.Entry e = l.entry(serial);
            if (e == null || e.state == LOST) {
                stack.setCount(0);
                bar(p, "§8Il s'effrite. Ce qu'il tenait a disparu.");
                return -1;
            }
            if (!p.getUUID().equals(e.owner)) {
                bar(p, "§8Il est lié à " + e.ownerName + ". Il ne prend pas racine pour toi.");
                return -1;
            }
            return serial;
        }
        long fresh = l.bind(p.getUUID(), p.getGameProfile().getName(), now(p.server));
        if (fresh < 0) {
            bar(p, "§8Cinq, c'est tout ce que tu peux lier. Il faut en perdre un avant d'en lier un autre.");
            return -1;
        }
        stamp(stack, l.entry(fresh));
        return fresh;
    }

    static String status(ServerPlayer o) {
        int stand = 0, away = 0, with = 0;
        for (Ledger.Entry e : Ledger.get(o.server).mine(o.getUUID())) {
            if (e.state == STANDING) stand++;
            else if (onPerson(o, e.serial)) with++;
            else away++;
        }
        return "§bTes phylactères §7: " + stand + " debout" + (away > 0 ? ", " + away + " hors de portée" : "") + (with > 0 ? "§8 (" + with + " sur toi)" : "") + "§7.";
    }

    static String track(ServerPlayer h, UUID owner, String name) {
        String who = name.isEmpty() ? "Son maître" : name;
        ServerPlayer t = h.server.getPlayerList().getPlayer(owner);
        if (t == null) return "§b" + who + " §8n'est pas là.";
        if (t.level() != h.level()) return "§b" + who + " §7est dans un autre monde.";
        double dx = t.getX() - h.getX(), dz = t.getZ() - h.getZ();
        int dist = (int) Math.round(Math.sqrt(dx * dx + dz * dz)), dy = (int) Math.round(t.getY() - h.getY());
        String[] dirs = {"sud", "sud-ouest", "ouest", "nord-ouest", "nord", "nord-est", "est", "sud-est"};
        int idx = Math.floorMod((int) Math.round(Math.toDegrees(Math.atan2(-dx, dz)) / 45.0), 8);
        String height = Math.abs(dy) < 4 ? "" : dy > 0 ? ", " + dy + " plus haut" : ", " + -dy + " plus bas";
        return "§b" + who + " §7est à " + dist + " blocs, " + dirs[idx] + height + ".";
    }

    // ------------------------------------------------------------------ événements
    private static final Map<UUID, Long> REVEALED = new HashMap<>(), LAST_SAVE = new HashMap<>();
    private static final Map<UUID, Integer> TURN = new HashMap<>();

    static boolean throughAnyway(DamageSource src) {
        return src.is(DamageTypes.FELL_OUT_OF_WORLD) || src.is(DamageTypes.GENERIC_KILL);
    }

    /** Un coup mortel : si un phylactère se dresse, 1 PV et le tueur apprend où en trouver un. Renvoie true si la mort est annulée. */
    public static boolean onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || !holds(p) || throughAnyway(e.getSource())) return false;
        List<Ledger.Entry> stand = standing(p);
        if (stand.isEmpty()) return false;
        e.setCanceled(true);
        p.setHealth(1f);
        p.invulnerableTime = Math.max(p.invulnerableTime, 20);
        if (e.getSource().getEntity() instanceof ServerPlayer killer && killer != p) {
            long t = now(p.server);
            Long last = REVEALED.get(killer.getUUID());
            if (last == null || t - last >= 40) {
                REVEALED.put(killer.getUUID(), t);
                Ledger.Entry en = stand.get(Math.floorMod(TURN.merge(p.getUUID(), 1, Integer::sum), stand.size()));
                killer.sendSystemMessage(Component.literal("§b" + p.getGameProfile().getName() + " §7devrait être mort. Un phylactère le retient à §f" + where(en) + "§7."));
            }
        }
        long t = now(p.server);
        Long last = LAST_SAVE.put(p.getUUID(), t);
        if (last == null || t - last >= 40) {
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6f, 0.6f);
            bar(p, "§bUn phylactère t'a retenu. §8" + stand.size() + " encore debout.");
        }
        return true;
    }

    public static void onHurt(LivingHurtEvent e) {
        if (e.getEntity() instanceof ServerPlayer v && holds(v) && e.getSource().getEntity() instanceof ServerPlayer c && c != v && carries(c, v.getUUID()))
            e.setAmount(e.getAmount() * CARRIER_BONUS);
    }

    static float burdenFor(int away, float maxWithout) {
        return Math.min(away * HEALTH_EACH, Math.max(0f, maxWithout - LEAST_MAX));
    }

    public static void tick(MinecraftServer server) {
        long now = now(server);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.tickCount % 20 != 0) continue;
            int away = holds(p) ? awayFrom(p) : 0;
            AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
            if (inst == null) continue;
            AttributeModifier had = inst.getModifier(BURDEN_ID);
            double was = had == null ? 0 : had.getAmount();
            float without = (float) (p.getMaxHealth() - was);
            float want = -burdenFor(away, without);
            if (Math.abs(want - was) < 0.001) continue;
            if (had != null) inst.removeModifier(BURDEN_ID);
            if (want < -0.001f) inst.addTransientModifier(new AttributeModifier(BURDEN_ID, "unrp_phylactery_burden", want, AttributeModifier.Operation.ADDITION));
            if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
        }
        if (now % 1200 != 0) return;
        Ledger l = Ledger.get(server);
        for (Ledger.Entry e : new ArrayList<>(l.entries.values())) {
            if (e.state == STANDING || e.state == LOST || now - e.seen < FORGET) continue;
            ServerPlayer owner = server.getPlayerList().getPlayer(e.owner);
            if (owner != null && onPerson(owner, e.serial)) {
                l.seen(e.serial, now);
                continue;
            }
            l.lost(e.serial);
            if (owner != null) owner.sendSystemMessage(Component.literal("§8Un phylactère que personne n'a porté depuis une heure s'est effrité. §7Il ne te coûte plus rien."));
        }
    }

    public static void onLogout(UUID id) {
        REVEALED.remove(id);
        TURN.remove(id);
        LAST_SAVE.remove(id);
    }
}
