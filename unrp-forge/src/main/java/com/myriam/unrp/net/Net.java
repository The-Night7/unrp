package com.myriam.unrp.net;

import com.myriam.unrp.UnrpMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal réseau du mod : le serveur décide de tout, le client n'envoie que des demandes. */
public final class Net {
    private static final String PROTOCOL = "7";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(UnrpMod.MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered;

    private Net() {}

    private static <M> java.util.function.BiConsumer<M, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> magic(
            java.util.function.BiConsumer<M, java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context>> handler) {
        return (message, supplier) -> {
            var context = supplier.get();
            context.enqueueWork(() -> {
                var sender = context.getSender();
                if (sender == null || !com.myriam.unrp.ability.AbilityEngine.disabled(sender)) handler.accept(message, supplier);
            });
            context.setPacketHandled(true);
        };
    }

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        int id = 0;
        CHANNEL.registerMessage(id++, CovenantPacket.class, CovenantPacket::encode, CovenantPacket::decode, CovenantPacket::handle);
        CHANNEL.registerMessage(id++, MenuDataPacket.class, MenuDataPacket::encode, MenuDataPacket::decode, MenuDataPacket::handle);
        CHANNEL.registerMessage(id++, ClaimRequestPacket.class, ClaimRequestPacket::encode, ClaimRequestPacket::decode, ClaimRequestPacket::handle);
        CHANNEL.registerMessage(id++, PlayerStatePacket.class, PlayerStatePacket::encode, PlayerStatePacket::decode, PlayerStatePacket::handle);
        CHANNEL.registerMessage(id++, CreateOcPacket.class, CreateOcPacket::encode, CreateOcPacket::decode, CreateOcPacket::handle);
        CHANNEL.registerMessage(id++, UseAbilityPacket.class, UseAbilityPacket::encode, UseAbilityPacket::decode, UseAbilityPacket::handle);
        CHANNEL.registerMessage(id++, HubActionPacket.class, HubActionPacket::encode, HubActionPacket::decode, HubActionPacket::handle);
        CHANNEL.registerMessage(id++, HubStatePacket.class, HubStatePacket::encode, HubStatePacket::decode, HubStatePacket::handle);
        CHANNEL.registerMessage(id++, StyleSavePacket.class, StyleSavePacket::encode, StyleSavePacket::decode, StyleSavePacket::handle);
        CHANNEL.registerMessage(id++, StyleSyncPacket.class, StyleSyncPacket::encode, StyleSyncPacket::decode, StyleSyncPacket::handle);
        CHANNEL.registerMessage(id++, FxPacket.class, FxPacket::encode, FxPacket::decode, FxPacket::handle);
        CHANNEL.registerMessage(id++, PosePacket.class, PosePacket::encode, PosePacket::decode, PosePacket::handle);
        CHANNEL.registerMessage(id++, CosmeticPacket.class, CosmeticPacket::encode, CosmeticPacket::decode, CosmeticPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.CodexSyncPacket.class, com.myriam.unrp.uo.CodexSyncPacket::encode, com.myriam.unrp.uo.CodexSyncPacket::decode, com.myriam.unrp.uo.CodexSyncPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.CodexActionPacket.class, com.myriam.unrp.uo.CodexActionPacket::encode, com.myriam.unrp.uo.CodexActionPacket::decode, com.myriam.unrp.uo.CodexActionPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.FormWheelPacket.class, com.myriam.unrp.uo.FormWheelPacket::encode, com.myriam.unrp.uo.FormWheelPacket::decode, com.myriam.unrp.uo.FormWheelPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.FormPickPacket.class, com.myriam.unrp.uo.FormPickPacket::encode, com.myriam.unrp.uo.FormPickPacket::decode, magic(com.myriam.unrp.uo.FormPickPacket::handle));
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LbFormPacket.class, com.myriam.unrp.uo.LbFormPacket::encode, com.myriam.unrp.uo.LbFormPacket::decode, com.myriam.unrp.uo.LbFormPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LbBecomingPacket.class, com.myriam.unrp.uo.LbBecomingPacket::encode, com.myriam.unrp.uo.LbBecomingPacket::decode, com.myriam.unrp.uo.LbBecomingPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.GritHudPacket.class, com.myriam.unrp.uo.GritHudPacket::encode, com.myriam.unrp.uo.GritHudPacket::decode, com.myriam.unrp.uo.GritHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.OriginLookPacket.class, com.myriam.unrp.uo.OriginLookPacket::encode, com.myriam.unrp.uo.OriginLookPacket::decode, com.myriam.unrp.uo.OriginLookPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.BreakCinePacket.class, com.myriam.unrp.uo.BreakCinePacket::encode, com.myriam.unrp.uo.BreakCinePacket::decode, com.myriam.unrp.uo.BreakCinePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.SandHudPacket.class, com.myriam.unrp.uo.SandHudPacket::encode, com.myriam.unrp.uo.SandHudPacket::decode, com.myriam.unrp.uo.SandHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.CraterPacket.class, com.myriam.unrp.uo.CraterPacket::encode, com.myriam.unrp.uo.CraterPacket::decode, com.myriam.unrp.uo.CraterPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LandfallPacket.class, com.myriam.unrp.uo.LandfallPacket::encode, com.myriam.unrp.uo.LandfallPacket::decode, com.myriam.unrp.uo.LandfallPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.StonesPacket.class, com.myriam.unrp.uo.StonesPacket::encode, com.myriam.unrp.uo.StonesPacket::decode, com.myriam.unrp.uo.StonesPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.ZonePacket.class, com.myriam.unrp.uo.ZonePacket::encode, com.myriam.unrp.uo.ZonePacket::decode, com.myriam.unrp.uo.ZonePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashFxPacket.class, com.myriam.unrp.uo.LashFxPacket::encode, com.myriam.unrp.uo.LashFxPacket::decode, com.myriam.unrp.uo.LashFxPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashGiveUpPacket.class, com.myriam.unrp.uo.LashGiveUpPacket::encode, com.myriam.unrp.uo.LashGiveUpPacket::decode, com.myriam.unrp.uo.LashGiveUpPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashHudPacket.class, com.myriam.unrp.uo.LashHudPacket::encode, com.myriam.unrp.uo.LashHudPacket::decode, com.myriam.unrp.uo.LashHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashMovePacket.class, com.myriam.unrp.uo.LashMovePacket::encode, com.myriam.unrp.uo.LashMovePacket::decode, com.myriam.unrp.uo.LashMovePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashSwingPacket.class, com.myriam.unrp.uo.LashSwingPacket::encode, com.myriam.unrp.uo.LashSwingPacket::decode, magic(com.myriam.unrp.uo.LashSwingPacket::handle));
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.LashWindPacket.class, com.myriam.unrp.uo.LashWindPacket::encode, com.myriam.unrp.uo.LashWindPacket::decode, com.myriam.unrp.uo.LashWindPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.FlashPacket.class, com.myriam.unrp.uo.FlashPacket::encode, com.myriam.unrp.uo.FlashPacket::decode, com.myriam.unrp.uo.FlashPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.StrugglePacket.class, com.myriam.unrp.uo.StrugglePacket::encode, com.myriam.unrp.uo.StrugglePacket::decode, com.myriam.unrp.uo.StrugglePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.StruggleMashPacket.class, com.myriam.unrp.uo.StruggleMashPacket::encode, com.myriam.unrp.uo.StruggleMashPacket::decode, com.myriam.unrp.uo.StruggleMashPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.SaltFxPacket.class, com.myriam.unrp.uo.SaltFxPacket::encode, com.myriam.unrp.uo.SaltFxPacket::decode, com.myriam.unrp.uo.SaltFxPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.SaltHudPacket.class, com.myriam.unrp.uo.SaltHudPacket::encode, com.myriam.unrp.uo.SaltHudPacket::decode, com.myriam.unrp.uo.SaltHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.SaltMarkPacket.class, com.myriam.unrp.uo.SaltMarkPacket::encode, com.myriam.unrp.uo.SaltMarkPacket::decode, com.myriam.unrp.uo.SaltMarkPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.SaltSteerPacket.class, com.myriam.unrp.uo.SaltSteerPacket::encode, com.myriam.unrp.uo.SaltSteerPacket::decode, com.myriam.unrp.uo.SaltSteerPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.DaystarHudPacket.class, com.myriam.unrp.uo.DaystarHudPacket::encode, com.myriam.unrp.uo.DaystarHudPacket::decode, com.myriam.unrp.uo.DaystarHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.CwHudPacket.class, com.myriam.unrp.uo.CwHudPacket::encode, com.myriam.unrp.uo.CwHudPacket::decode, com.myriam.unrp.uo.CwHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.WeaveSyncPacket.class, com.myriam.unrp.uo.WeaveSyncPacket::encode, com.myriam.unrp.uo.WeaveSyncPacket::decode, com.myriam.unrp.uo.WeaveSyncPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.WeaveFxPacket.class, com.myriam.unrp.uo.WeaveFxPacket::encode, com.myriam.unrp.uo.WeaveFxPacket::decode, com.myriam.unrp.uo.WeaveFxPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.uo.WeaveReachPacket.class, com.myriam.unrp.uo.WeaveReachPacket::encode, com.myriam.unrp.uo.WeaveReachPacket::decode, magic(com.myriam.unrp.uo.WeaveReachPacket::handle));
        CHANNEL.registerMessage(id++, PunchPacket.class, PunchPacket::encode, PunchPacket::decode, PunchPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.pass.PassFxPacket.class, com.myriam.unrp.pass.PassFxPacket::encode, com.myriam.unrp.pass.PassFxPacket::decode, com.myriam.unrp.pass.PassFxPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.am.AmWheelPacket.class, com.myriam.unrp.am.AmWheelPacket::encode, com.myriam.unrp.am.AmWheelPacket::decode, com.myriam.unrp.am.AmWheelPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.am.AmWheelAskPacket.class, com.myriam.unrp.am.AmWheelAskPacket::encode, com.myriam.unrp.am.AmWheelAskPacket::decode, com.myriam.unrp.am.AmWheelAskPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.am.AmWheelPickPacket.class, com.myriam.unrp.am.AmWheelPickPacket::encode, com.myriam.unrp.am.AmWheelPickPacket::decode, magic(com.myriam.unrp.am.AmWheelPickPacket::handle));
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.PactEditPacket.class, com.myriam.unrp.deal.PactEditPacket::encode, com.myriam.unrp.deal.PactEditPacket::decode, magic(com.myriam.unrp.deal.PactEditPacket::handle));
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.PactViewPacket.class, com.myriam.unrp.deal.PactViewPacket::encode, com.myriam.unrp.deal.PactViewPacket::decode, com.myriam.unrp.deal.PactViewPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.PactHudPacket.class, com.myriam.unrp.deal.PactHudPacket::encode, com.myriam.unrp.deal.PactHudPacket::decode, com.myriam.unrp.deal.PactHudPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.LedgerActionPacket.class, com.myriam.unrp.deal.LedgerActionPacket::encode, com.myriam.unrp.deal.LedgerActionPacket::decode, com.myriam.unrp.deal.LedgerActionPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.LedgerViewPacket.class, com.myriam.unrp.deal.LedgerViewPacket::encode, com.myriam.unrp.deal.LedgerViewPacket::decode, com.myriam.unrp.deal.LedgerViewPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.PlaneSkyPacket.class, com.myriam.unrp.deal.PlaneSkyPacket::encode, com.myriam.unrp.deal.PlaneSkyPacket::decode, com.myriam.unrp.deal.PlaneSkyPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.DialoguePacket.class, com.myriam.unrp.deal.DialoguePacket::encode, com.myriam.unrp.deal.DialoguePacket::decode, com.myriam.unrp.deal.DialoguePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.deal.DialogueChoicePacket.class, com.myriam.unrp.deal.DialogueChoicePacket::encode, com.myriam.unrp.deal.DialogueChoicePacket::decode, com.myriam.unrp.deal.DialogueChoicePacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.anim.MoveAnimPacket.class, com.myriam.unrp.anim.MoveAnimPacket::encode, com.myriam.unrp.anim.MoveAnimPacket::decode, com.myriam.unrp.anim.MoveAnimPacket::handle);
        CHANNEL.registerMessage(id++, com.myriam.unrp.pass.PassStatePacket.class, com.myriam.unrp.pass.PassStatePacket::encode, com.myriam.unrp.pass.PassStatePacket::decode, com.myriam.unrp.pass.PassStatePacket::handle);
    }
}
