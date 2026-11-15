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
    private final net.minecraft.client.model.geom.ModelPart skirt, hatBrim;

    private static HumanoidModel<AbstractClientPlayer> outfitModel(boolean inner) {
        var inflate = new net.minecraft.client.model.geom.builders.CubeDeformation(inner ? 0.5f : 1.0f);
        var mesh = HumanoidModel.createMesh(inflate, 0);
        mesh.getRoot().addOrReplaceChild("left_arm", net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(32,48).mirror().addBox(-1,-2,-2,4,12,4,inflate),
                net.minecraft.client.model.geom.PartPose.offset(5,2,0));
        mesh.getRoot().addOrReplaceChild("left_leg", net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(16,48).mirror().addBox(-2,0,-2,4,12,4,inflate),
                net.minecraft.client.model.geom.PartPose.offset(1.9f,12,0));
        return new HumanoidModel<>(net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot());
    }

    private static net.minecraft.client.model.geom.ModelPart detail(String name,int u,int v,float x,float y,float z,float w,float h,float d) {
        var mesh = new net.minecraft.client.model.geom.builders.MeshDefinition();
        mesh.getRoot().addOrReplaceChild(name, net.minecraft.client.model.geom.builders.CubeListBuilder.create()
                .texOffs(u,v).addBox(x,y,z,w,h,d,new net.minecraft.client.model.geom.builders.CubeDeformation(0.45f)),
                net.minecraft.client.model.geom.PartPose.ZERO);
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot().getChild(name);
    }

    public CosmeticLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, EntityModelSet models) {
        super(parent);
        this.outer = outfitModel(false);
        this.inner = outfitModel(true);
        this.skirt = detail("skirt",16,16,-4,11,-2,8,7,4);
        this.hatBrim = detail("brim",0,0,-5,-5,-5,10,1,10);
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buf, int light, AbstractClientPlayer pl, float limbSwing, float limbSwingAmount,
                       float partial, float age, float headYaw, float headPitch) {
        if (pl.isInvisible()) return;
        if (pl.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof com.myriam.unrp.item.ModItems.Mask) {
            getParentModel().copyPropertiesTo(outer);
            outer.setAllVisible(false);
            outer.head.visible = true;
            outer.renderToBuffer(ps,buf.getBuffer(RenderType.armorCutoutNoCull(new ResourceLocation("unrp","textures/models/armor/joker_mask_layer_1.png"))),light,OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
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
            boolean hasSkirt = legs && o.set.contains("girl_uniform");
            if (hasSkirt) m.body.visible = false;
            ResourceLocation tex = new ResourceLocation("unrp", "textures/models/armor/" + o.set + (legs ? "_layer_2.png" : "_layer_1.png"));
            var vertices = buf.getBuffer(RenderType.armorCutoutNoCull(tex));
            m.renderToBuffer(ps, vertices, light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
            if (hasSkirt) {
                skirt.copyFrom(m.body);
                skirt.render(ps,vertices,light,OverlayTexture.NO_OVERLAY,1,1,1,1);
            }
            if (o.slot == EquipmentSlot.HEAD && o.set.equals("ember_hat")) {
                hatBrim.copyFrom(m.head);
                hatBrim.render(ps,vertices,light,OverlayTexture.NO_OVERLAY,1,1,1,1);
            }
        }
    }
}
