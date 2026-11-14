package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.myriam.unrp.UnrpMod;
import com.myriam.unrp.item.Cosmetics;
import com.myriam.unrp.item.UnItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.ForgeRegistries;

/** Dessine les vêtements cosmétiques par-dessus le corps, sans passer par les cases d'armure. */
public class CosmeticLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final HumanoidModel<AbstractClientPlayer> outer, inner;

    public CosmeticLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.outer = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        this.inner = new HumanoidModel<>(models.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer pl, float limbSwing, float limbSwingAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (pl.isInvisible()) return;
        String[] v = Cosmetics.CLIENT.get(pl.getUUID());
        if (v == null) return;
        for (int i = 0; i < 4; i++) {
            if (v[i] == null || v[i].isEmpty()) continue;
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(UnrpMod.MOD_ID, v[i]));
            if (!(item instanceof UnItems.Outfit o)) continue;
            boolean legs = o.slot == EquipmentSlot.LEGS;
            HumanoidModel<AbstractClientPlayer> m = legs ? inner : outer;
            getParentModel().copyPropertiesTo(m);
            m.setAllVisible(false);
            switch (o.slot) {
                case HEAD -> { m.head.visible = true; m.hat.visible = true; }
                case CHEST -> { m.body.visible = true; m.rightArm.visible = true; m.leftArm.visible = true; }
                case LEGS -> { m.body.visible = true; m.rightLeg.visible = true; m.leftLeg.visible = true; }
                default -> { m.rightLeg.visible = true; m.leftLeg.visible = true; }
            }
            ResourceLocation tex = new ResourceLocation("unrp", "textures/models/armor/" + o.set + (legs ? "_layer_2.png" : "_layer_1.png"));
            m.renderToBuffer(ps, buf.getBuffer(RenderType.armorCutoutNoCull(tex)), light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }
}
