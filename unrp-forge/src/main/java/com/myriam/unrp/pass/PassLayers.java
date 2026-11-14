package com.myriam.unrp.pass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Calques joueur et réglages client du modèle liés au moteur d'effets : TimeGearsLayer, PredatorTransLayer, PredatorClientHandler. */
public final class PassLayers {
    private PassLayers() {}

    /** TimeGearsLayer : engrenages de Time Manipulation. */
    public static final class TimeGears extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public TimeGears(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                           float pt, float age, float headYaw, float headPitch) {
            if (!player.isInvisible()) ClientPassTm.layer(player, ps, pt);
        }
    }

    /** PredatorTransLayer : rayures, brûlure et griffes de la transformation de Predator. */
    public static final class PredatorTrans extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        private static final ResourceLocation STRIPES = new ResourceLocation("unrp", "textures/models/predator_stripes.png");
        private static final ResourceLocation CLAWS = new ResourceLocation("unrp", "textures/models/predator_claws.png");
        private static final ResourceLocation GLOW = new ResourceLocation("unrp", "textures/models/predator_stripes_glow.png");
        private final HumanoidModel<AbstractClientPlayer> claws;

        public PredatorTrans(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
            super(parent);
            this.claws = new HumanoidModel<>(models.bakeLayer(com.myriam.unrp.client.AgentRenderer.CLAWS_LAYER));
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                           float pt, float ageInTicks, float headYaw, float headPitch) {
            if (!PassBridge.isTransformed(player.getUUID()) || player.isInvisible()) return;
            double age = ClientPassPd.transAge(player, pt);
            float stripes = (float) PassPd.stripes(age);
            float burn = (float) Math.min(1.0, PassPd.burn(age, player.level().getGameTime() + pt));
            float cl = (float) PassPd.clawsIn(age);
            if (stripes > 0.004F)
                getParentModel().renderToBuffer(ps, buf.getBuffer(RenderType.entityTranslucent(STRIPES)), light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, stripes);
            if (burn > 0.004F)
                getParentModel().renderToBuffer(ps, buf.getBuffer(RenderType.eyes(GLOW)), light, OverlayTexture.NO_OVERLAY,
                        (float) PassPd.BURN[0] * burn, (float) PassPd.BURN[1] * burn, (float) PassPd.BURN[2] * burn, 1f);
            if (cl > 0.004F) {
                getParentModel().copyPropertiesTo(claws);
                claws.renderToBuffer(ps, buf.getBuffer(RenderType.eyes(CLAWS)), light, OverlayTexture.NO_OVERLAY, cl, cl, cl, 1f);
            }
        }
    }

    /** PredatorClientHandler : champ de vision resserré pendant Sens aiguisé, réticule sur la proie verrouillée. */
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "unrp", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class PredatorClient {
        private static final ResourceLocation CROSSHAIR = new ResourceLocation("unrp", "textures/gui/predator_crosshair.png");

        static boolean focusing(java.util.UUID id) {
            PassBridge.State s = PassBridge.STATES.get(id);
            return s != null && s.on().contains("sense_focus");
        }

        @SubscribeEvent
        public static void onFov(ComputeFovModifierEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && focusing(mc.player.getUUID())) event.setNewFovModifier(event.getNewFovModifier() * 0.5F);
        }

        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            LivingEntity entity = event.getEntity();
            if (PassBridge.lockedId(mc.player.getUUID()) != entity.getId() || ClientPassPd.marks(mc.player, entity.getId())) return;
            int color = PassBridge.color(mc.player.getUUID());
            if (color == -1 || PassBridge.isAM(mc.player.getUUID())) color = 15242256;
            float r = (color >> 16 & 255) / 255f, g = (color >> 8 & 255) / 255f, b = (color & 255) / 255f;
            PoseStack ps = event.getPoseStack();
            ps.pushPose();
            ps.translate(0.0F, entity.getBbHeight() + 0.75F, 0.0F);
            ps.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
            float size = 0.45F;
            VertexConsumer vc = event.getMultiBufferSource().getBuffer(RenderType.eyes(CROSSHAIR));
            PoseStack.Pose pose = ps.last();
            quad(vc, pose, -size, -size, size, size, r, g, b);
            ps.popPose();
        }

        private static void quad(VertexConsumer vc, PoseStack.Pose pose, float x1, float y1, float x2, float y2, float r, float g, float b) {
            v(vc, pose, x1, y1, 0, 1, r, g, b); v(vc, pose, x2, y1, 1, 1, r, g, b); v(vc, pose, x2, y2, 1, 0, r, g, b); v(vc, pose, x1, y2, 0, 0, r, g, b);
            v(vc, pose, x1, y2, 0, 0, r, g, b); v(vc, pose, x2, y2, 1, 0, r, g, b); v(vc, pose, x2, y1, 1, 1, r, g, b); v(vc, pose, x1, y1, 0, 1, r, g, b);
        }

        private static void v(VertexConsumer vc, PoseStack.Pose pose, float x, float y, float u, float vv, float r, float g, float b) {
            vc.vertex(pose.pose(), x, y, 0f).color(r, g, b, 1f).uv(u, vv).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pose.normal(), 0f, 0f, -1f).endVertex();
        }
    }
}
