package com.myriam.unrp.deal;

import com.myriam.unrp.ability.AbilityEngine;
import com.myriam.unrp.ability.Superpowers;
import com.myriam.unrp.data.AbilityPower;
import com.myriam.unrp.data.UnodexData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DealBridge {
    private DealBridge() {}

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "unrp");
    public static final RegistryObject<RecipeSerializer<SoulBindRecipe>> SOUL_BIND =
            SERIALIZERS.register("soul_bind", () -> new SimpleCraftingRecipeSerializer<>(SoulBindRecipe::new));

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }

    // ------------------------------------------------------------------ pouvoirs (MoveGovernor, OriginsConfig)
    public static final Set<String> ORIGIN_POWERS = Set.of("sarcophage", "edict", "wyrm_engine", "singularity", "limit_breaker", "dealmaker",
            "counterweight", "crimson_mark", "hollow_mark", "nadir", "chained_one");

    public static boolean isOriginPower(String s) {
        return ORIGIN_POWERS.contains(s);
    }

    public static Map<String, Integer> seats() {
        return Map.of();
    }

    public static String powerOf(ServerPlayer p) {
        String s = AbilityEngine.basePowerId(p);
        return s == null || Superpowers.revoked(p).contains(s) ? "" : s;
    }

    public static boolean systemPower(String s) {
        return s.equals("soul_bound") || s.equals("disguise") || s.equals("boon_projection") || s.equals("boon_travel");
    }

    public static class MoveInfo {
        public final String power;
        public final int slot;
        public final String title;
        public final List<String> tags;
        public final int cooldown;

        MoveInfo(String power, int slot, String title, List<String> tags, int cooldown) {
            this.power = power;
            this.slot = slot;
            this.title = title;
            this.tags = tags;
            this.cooldown = cooldown;
        }
    }

    public static List<String> grantablePowers(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        for (String k : UnodexData.get().powers.keySet()) if (!systemPower(k) && !k.endsWith("_am")) out.add(k);
        java.util.Collections.sort(out);
        return out;
    }

    public static Map<String, List<MoveInfo>> table(MinecraftServer server) {
        Map<String, List<MoveInfo>> out = new LinkedHashMap<>();
        for (Map.Entry<String, AbilityPower> e : UnodexData.get().powers.entrySet()) {
            List<MoveInfo> l = new ArrayList<>();
            int i = 0;
            for (AbilityPower.Move m : e.getValue().moves) {
                l.add(new MoveInfo(e.getKey(), ++i, m.name, m.triggerTag == null ? List.of() : List.of(m.triggerTag), Math.max(0, m.cooldown)));
            }
            out.put(e.getKey(), l);
        }
        return out;
    }

    public static int run(MinecraftServer server, String cmd) {
        String[] w = cmd.trim().split("\\s+");
        if (w.length == 4 && w[0].equals("superpower") && w[2].startsWith("unrp:")) {
            ServerPlayer p = server.getPlayerList().getPlayerByName(w[3]);
            if (p == null) return 0;
            String id = w[2].substring("unrp:".length());
            if (w[1].equals("add")) Superpowers.add(p, id);
            else if (w[1].equals("remove")) Superpowers.remove(p, id);
            return 1;
        }
        return server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd);
    }

    public static void lend(ServerPlayer p, String power) {
        p.addTag("unrp.am_shown." + power);
    }

    public static SoundEvent sound(String id) {
        ResourceLocation rl = new ResourceLocation(id);
        SoundEvent s = BuiltInRegistries.SOUND_EVENT.get(rl);
        return s != null ? s : SoundEvent.createVariableRangeEvent(rl);
    }

    public static int originLevel(ServerPlayer p) {
        return com.myriam.unrp.ability.OriginGrowth.level(p);
    }

    public static CompoundTag settingsGet(ServerPlayer p) {
        var s = com.myriam.unrp.style.StyleService.get(p.server, p.getUUID());
        CompoundTag t = new CompoundTag();
        int ac = s.auraColor & 0xFFFFFF;
        t.putInt("AuraColor", ac == com.myriam.unrp.pass.PassServer.UNSET ? -1 : ac);
        t.putInt("EyeColor", s.eyeColor & 0xFFFFFF);
        return t;
    }

    public static void settingsApply(ServerPlayer p, CompoundTag t) {
        var s = com.myriam.unrp.style.StyleService.get(p.server, p.getUUID()).copy();
        if (t.contains("AuraColor")) {
            int c = t.getInt("AuraColor");
            s.auraColor = c == -1 ? com.myriam.unrp.pass.PassServer.UNSET : c & 0xFFFFFF;
        }
        if (t.contains("EyeColor")) s.eyeColor = t.getInt("EyeColor") & 0xFFFFFF;
        com.myriam.unrp.style.StyleService.save(p, s);
    }

    public static void invalidateGear(Object... a) {}
    public static boolean inFight(Object... a) { return false; }
    public static boolean myWorldMoving(Object... a) { return false; }
    public static boolean myWorldReturning(Object... a) { return false; }
    public static void puppetLetGo(Object... a) {}
    public static boolean puppetPulling(Object... a) { return false; }
    public static boolean sgForgeHas(Object... a) { return false; }
    public static boolean sgOutside(Object... a) { return false; }
    public static boolean sgSealed(Object... a) { return false; }
    public static int sgStageOf(Object... a) { return 0; }
    public static boolean titanHolds(Object... a) { return false; }

    public static void look_seal(Object... a) {}
    public static void look_borrowed(Object... a) {}
    public static void look_imbue(Object... a) {}
    public static void look_rip(Object... a) {}
    public static int sealSigned(Object... a) { return 0; }
    public static int sealWaxed(Object... a) { return 0; }

    // ------------------------------------------------------------------ client
    public static int powerVariant(Player p, String shortName) {
        java.util.Set<String> k1 = com.myriam.unrp.pass.PassBridge.keysOf(p.getUUID(), shortName);
        if (k1 != null && k1.contains("toggle")) return 1;
        java.util.Set<String> k2 = com.myriam.unrp.pass.PassBridge.keysOf(p.getUUID(), shortName + "_am");
        return k2 != null && k2.contains("toggle") ? 2 : 0;
    }

    public static int colorForViewer(int fallback) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        return mc.player == null ? fallback : colorForOwner(mc.player.getUUID(), fallback);
    }

    public static int colorForOwner(UUID uuid, int fallback) {
        if (uuid == null) return fallback;
        if (com.myriam.unrp.pass.PassBridge.isAM(uuid)) return 3026486;
        int c = com.myriam.unrp.pass.PassBridge.color(uuid);
        return c == -1 ? fallback : c;
    }

    public static final Map<String, Integer> EYE_DEFAULTS;

    static {
        Map<String, Integer> m = new HashMap<>();
        m.put("ability_gauging", 16767626);
        m.put("barrier", 16766976);
        m.put("conjure_disks", 14885888);
        m.put("contact_shred", 15445046);
        m.put("demon_claw", 13111342);
        m.put("dizzy_punch", 14378559);
        m.put("energy_discharge", 16711680);
        m.put("explosion", 8863024);
        m.put("flame_claw", 14562304);
        m.put("healing", 5364563);
        m.put("heat_palm", 2281471);
        m.put("hydrofreeze", 65415);
        m.put("hypnosis", 11554353);
        m.put("illumination", 16425016);
        m.put("lightning", 4174079);
        m.put("needles", 13498240);
        m.put("nightmare", 14927616);
        m.put("phase_shift", 2743247);
        m.put("regeneration", 32526);
        m.put("rock_arm", 11897382);
        m.put("shadow_flame", 7471334);
        m.put("speed", 16580403);
        m.put("aura_transference", 5692644);
        m.put("armament_dominion", 15254106);
        m.put("daystar", 16763210);
        m.put("black_lash", 5891826);
        m.put("salt_seraph", 15782530);
        m.put("chained_one", 10771711);
        m.put("stone_skin", 9656569);
        m.put("strong_kick", 2205067);
        m.put("strong_punch", 2591831);
        m.put("predator", 15242256);
        m.put("whirlwind", 15462645);
        m.put("time_manipulation", 16740288);
        m.put("catalyst_ray", 15258938);
        m.put("armor_suit", 5220607);
        m.put("vine_barrier", 8183914);
        m.put("explosive_missile", 16742972);
        m.put("homing_explosive_missile", 16734762);
        m.put("blizzard", 10479871);
        m.put("livewire", 13172570);
        m.put("arcroot", 12576842);
        m.put("warhead", 13159892);
        m.put("fusework", 14256714);
        m.put("missile_punch", 16757575);
        m.put("hand_blade", 15781984);
        m.put("spikes", 12550143);
        m.put("crescent_slash", 8377599);
        m.put("quick_strike", 16748608);
        m.put("arcane_shot", 11567080);
        m.put("missiles", 16740432);
        m.put("conjure_vines", 4055130);
        m.put("energy_conduit", 7264488);
        m.put("phantom_push", 5884159);
        m.put("phantom_fist", 5234920);
        m.put("tremor", 14198869);
        m.put("spectral_claw", 10178520);
        m.put("umbral_shift", 9067752);
        m.put("azure_storm", 5220584);
        m.put("arachnid", 14174815);
        m.put("particles", 15160448);
        m.put("sarcophage", 14594656);
        m.put("edict", 15254090);
        m.put("wyrm_engine", 3532906);
        m.put("singularity", 13156080);
        m.put("dealmaker", 15254378);
        m.put("limit_breaker", 16765519);
        m.put("counterweight", 16753964);
        m.put("crimson_mark", 13114940);
        m.put("hollow_mark", 8381416);
        m.put("nadir", 14477554);
        m.put("legacy", 16770976);
        EYE_DEFAULTS = m;
    }

    // ------------------------------------------------------------------ WorldRule
    public static boolean oldDim(String dim) {
        return dim == null || !dim.startsWith("unrp:");
    }

    public static String dimOf(String where) {
        if (where == null) return "";
        int bar = where.indexOf('|');
        return bar < 0 ? where : where.substring(0, bar);
    }
}
