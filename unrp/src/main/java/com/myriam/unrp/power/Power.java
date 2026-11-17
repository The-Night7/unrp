package com.myriam.unrp.power;

import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.data.PlayerData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Power {
    @FunctionalInterface
    public interface Ability {
        void run(ServerPlayerEntity player, PlayerData data);
    }

    public final String id;
    public final int cost;

    public final int cooldown;
    public final Ability ability;

    public Power(String id, int cost, int cooldown, Ability ability) {
        this.id = id;
        this.cost = cost;
        this.cooldown = cooldown;
        this.ability = ability;
    }

    public Text displayName() {
        return Text.translatable("power." + UnrpMod.MOD_ID + "." + id);
    }
}
