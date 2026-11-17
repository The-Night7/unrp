package com.myriam.unrp.net;

import com.myriam.unrp.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerStatePacket {
    public boolean hasProfile, hasAbility;
    public String name = "", kind = "", tier = "unknown", level = "?";
    public String abilityName = "", abilityType = "", abilityNature = "", abilityDesc = "";
    public String schools = "", leaders = "";
    public int members;
    /** power, defense, recovery, trick, speed */
    public int[] stats = {1, 1, 1, 1, 1};
    public int aura, maxAura, regen;

    public boolean active;
    /** forme courante (Modes, Phase Shift) ou vide */
    public String modeName = "";

    public String[] moveNames = new String[0];
    public boolean[] moveHeld = new boolean[0];

    public int[] moveCharge = new int[0];

    public String[] moveDesc = new String[0];
    public boolean[] moveToggle = new boolean[0], moveOn = new boolean[0];
    public int[] moveCost = new int[0], moveCd = new int[0], moveLeft = new int[0];

    public int abilityPts, abilityPtsTotal, unlockMask;

    public int mastery = -1;
    public boolean[] moveLocked = new boolean[0];
    public int[] moveRank = new int[0];

    public String powerIcon = "", powerColor = "";
    public int[] barSlot = new int[0], barCd = new int[0], barLeft = new int[0];
    public String[] barIcon = new String[0], barColor = new String[0], barName = new String[0];
    public boolean[] barLocked = new boolean[0], barOn = new boolean[0];

    public boolean barMore;

    public int[] barPower = new int[0], barMove = new int[0];

    public String[] powIcon = new String[0], powColor = new String[0], powName = new String[0];
    public boolean[] powOn = new boolean[0];

    public static void encode(PlayerStatePacket p, FriendlyByteBuf b) {
        b.writeBoolean(p.hasProfile);
        b.writeBoolean(p.hasAbility);
        b.writeUtf(p.name, 64);
        b.writeUtf(p.kind, 16);
        b.writeUtf(p.tier, 16);
        b.writeUtf(p.level, 8);
        b.writeUtf(p.abilityName, 96);
        b.writeUtf(p.abilityType, 32);
        b.writeUtf(p.abilityNature, 32);
        b.writeUtf(p.abilityDesc, 600);
        b.writeUtf(p.schools, 200);
        b.writeUtf(p.leaders, 300);
        b.writeVarInt(p.members);
        for (int s : p.stats) b.writeVarInt(s);
        b.writeVarInt(p.aura);
        b.writeVarInt(p.maxAura);
        b.writeVarInt(p.regen);
        b.writeBoolean(p.active);
        b.writeUtf(p.modeName, 32);
        b.writeInt(p.mastery);
        b.writeVarInt(p.abilityPts);
        b.writeVarInt(p.abilityPtsTotal);
        b.writeVarInt(p.unlockMask);
        b.writeVarInt(p.moveNames.length);
        for (int i = 0; i < p.moveNames.length; i++) {
            b.writeUtf(p.moveNames[i], 40);
            b.writeBoolean(p.moveHeld[i]);
            b.writeVarInt(p.moveCharge[i]);
            b.writeUtf(p.moveDesc[i], 200);
            b.writeBoolean(p.moveToggle[i]);
            b.writeBoolean(p.moveOn[i]);
            b.writeVarInt(p.moveCost[i]);
            b.writeVarInt(p.moveCd[i]);
            b.writeVarInt(p.moveLeft[i]);
            b.writeBoolean(p.moveLocked.length > i && p.moveLocked[i]);
            b.writeVarInt(p.moveRank.length > i ? p.moveRank[i] : 0);
        }
        b.writeUtf(p.powerIcon == null ? "" : p.powerIcon);
        b.writeUtf(p.powerColor == null ? "" : p.powerColor);
        b.writeVarInt(p.barSlot.length);
        for (int k = 0; k < p.barSlot.length; k++) {
            b.writeVarInt(p.barSlot[k]);
            b.writeVarInt(p.barCd[k]);
            b.writeVarInt(p.barLeft[k]);
            b.writeUtf(p.barIcon[k] == null ? "" : p.barIcon[k]);
            b.writeUtf(p.barColor[k] == null ? "" : p.barColor[k]);
            b.writeUtf(p.barName[k] == null ? "" : p.barName[k]);
            b.writeBoolean(p.barLocked[k]);
            b.writeBoolean(p.barOn[k]);
            b.writeVarInt(k < p.barPower.length ? p.barPower[k] : 0);
            b.writeVarInt(k < p.barMove.length ? p.barMove[k] : k);
        }
        b.writeVarInt(p.powIcon.length);
        for (int k = 0; k < p.powIcon.length; k++) {
            b.writeUtf(p.powIcon[k] == null ? "" : p.powIcon[k]);
            b.writeUtf(p.powColor[k] == null ? "" : p.powColor[k]);
            b.writeUtf(p.powName[k] == null ? "" : p.powName[k]);
            b.writeBoolean(p.powOn[k]);
        }
        b.writeBoolean(p.barMore);
    }

    public static PlayerStatePacket decode(FriendlyByteBuf b) {
        PlayerStatePacket p = new PlayerStatePacket();
        p.hasProfile = b.readBoolean();
        p.hasAbility = b.readBoolean();
        p.name = b.readUtf(64);
        p.kind = b.readUtf(16);
        p.tier = b.readUtf(16);
        p.level = b.readUtf(8);
        p.abilityName = b.readUtf(96);
        p.abilityType = b.readUtf(32);
        p.abilityNature = b.readUtf(32);
        p.abilityDesc = b.readUtf(600);
        p.schools = b.readUtf(200);
        p.leaders = b.readUtf(300);
        p.members = b.readVarInt();
        for (int i = 0; i < p.stats.length; i++) p.stats[i] = b.readVarInt();
        p.aura = b.readVarInt();
        p.maxAura = b.readVarInt();
        p.regen = b.readVarInt();
        p.active = b.readBoolean();
        p.modeName = b.readUtf(32);
        p.mastery = b.readInt();
        p.abilityPts = b.readVarInt();
        p.abilityPtsTotal = b.readVarInt();
        p.unlockMask = b.readVarInt();
        int n = readCount(b, com.myriam.unrp.ability.AbilityInputProtocol.CAPACITY, "moves");
        p.moveNames = new String[n];
        p.moveHeld = new boolean[n];
        p.moveCharge = new int[n];
        p.moveDesc = new String[n];
        p.moveToggle = new boolean[n];
        p.moveOn = new boolean[n];
        p.moveCost = new int[n];
        p.moveCd = new int[n];
        p.moveLeft = new int[n];
        p.moveLocked = new boolean[n];
        p.moveRank = new int[n];
        for (int i = 0; i < n; i++) {
            p.moveNames[i] = b.readUtf(40);
            p.moveHeld[i] = b.readBoolean();
            p.moveCharge[i] = b.readVarInt();
            p.moveDesc[i] = b.readUtf(200);
            p.moveToggle[i] = b.readBoolean();
            p.moveOn[i] = b.readBoolean();
            p.moveCost[i] = b.readVarInt();
            p.moveCd[i] = b.readVarInt();
            p.moveLeft[i] = b.readVarInt();
            p.moveLocked[i] = b.readBoolean();
            p.moveRank[i] = b.readVarInt();
        }
        p.powerIcon = b.readUtf();
        p.powerColor = b.readUtf();
        int bn = readCount(b, 8192, "bar entries");
        p.barSlot = new int[bn]; p.barCd = new int[bn]; p.barLeft = new int[bn];
        p.barIcon = new String[bn]; p.barColor = new String[bn]; p.barName = new String[bn];
        p.barLocked = new boolean[bn]; p.barOn = new boolean[bn];
        p.barPower = new int[bn]; p.barMove = new int[bn];
        for (int k = 0; k < bn; k++) {
            p.barSlot[k] = b.readVarInt();
            p.barCd[k] = b.readVarInt();
            p.barLeft[k] = b.readVarInt();
            p.barIcon[k] = b.readUtf();
            p.barColor[k] = b.readUtf();
            p.barName[k] = b.readUtf();
            p.barLocked[k] = b.readBoolean();
            p.barOn[k] = b.readBoolean();
            p.barPower[k] = b.readVarInt();
            p.barMove[k] = b.readVarInt();
        }
        int pn = readCount(b, 256, "powers");
        p.powIcon = new String[pn]; p.powColor = new String[pn]; p.powName = new String[pn]; p.powOn = new boolean[pn];
        for (int k = 0; k < pn; k++) {
            p.powIcon[k] = b.readUtf();
            p.powColor[k] = b.readUtf();
            p.powName[k] = b.readUtf();
            p.powOn[k] = b.readBoolean();
        }
        p.barMore = b.readBoolean();
        return p;
    }

    private static int readCount(FriendlyByteBuf buffer, int maximum, String field) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) throw new io.netty.handler.codec.DecoderException("Invalid " + field + " count: " + count);
        return count;
    }

    public static void handle(PlayerStatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.onPlayerState(p)));
        ctx.get().setPacketHandled(true);
    }
}
