package com.myriam.unrp.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.myriam.unrp.entity.Agent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class AgentRenderer extends HumanoidMobRenderer<Agent, PlayerModel<Agent>> {
    private static final ResourceLocation[] TEXTURES = {
            new ResourceLocation("unrp", "textures/entity/agent.png"),
            new ResourceLocation("unrp", "textures/entity/agent_leader.png"),
            new ResourceLocation("unrp", "textures/entity/agent_spectre.png"),
            new ResourceLocation("unrp", "textures/entity/agent_authority.png")};
    public static final ModelLayerLocation CLAWS_LAYER = new ModelLayerLocation(new ResourceLocation("unrp", "agent_claws"), "main");

    public AgentRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PlayerModel<>(ctx.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        addLayer(new Claws(this, new ClawsModel(ctx.bakeLayer(CLAWS_LAYER))));
    }

    @Override
    public ResourceLocation getTextureLocation(Agent e) {
        int v = e.getVariant();
        return TEXTURES[v < 0 || v >= TEXTURES.length ? 0 : v];
    }

    static final class Claws extends RenderLayer<Agent, PlayerModel<Agent>> {
        private static final ResourceLocation TEX = new ResourceLocation("unrp", "textures/models/fire_claws.png");
        private static final ResourceLocation TEX_SURGE = new ResourceLocation("unrp", "textures/models/soulfire_claws.png");
        private final ClawsModel model;

        Claws(RenderLayerParent<Agent, PlayerModel<Agent>> parent, ClawsModel model) {
            super(parent);
            this.model = model;
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buf, int light, Agent e, float limbSwing, float limbSwingAmount,
                           float partial, float age, float headYaw, float headPitch) {
            if (e.getVariant() != 1) return;
            getParentModel().copyPropertiesTo(model);
            model.renderToBuffer(ps, buf.getBuffer(RenderType.eyes(e.isSurging() ? TEX_SURGE : TEX)), light, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        }
    }

    static final class ClawsModel extends HumanoidModel<Agent> {
        ClawsModel(ModelPart root) {
            super(root);
        }
    }

    public static LayerDefinition clawsLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0f, 0f, 0f));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0f, 0f, 0f));
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0f, 0f, 0f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12f, 0f));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12f, 0f));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5f, 1.5f, -2.5f, 5f, 9f, 5f)
                .texOffs(20, 0).addBox(-3.5f, 10.5f, -2.5f, 3f, 5f, 0f)
                .texOffs(20, 10).addBox(-3.5f, 10.5f, -0.75f, 3f, 5f, 0f)
                .texOffs(20, 20).addBox(-3.5f, 10.5f, 0.75f, 3f, 5f, 0f)
                .texOffs(23, 5).mirror().addBox(-3.5f, 10.5f, 2.5f, 3f, 5f, 0f).mirror(false),
                PartPose.offset(-5f, 2f, 0f));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1.5f, 1.5f, -2.5f, 5f, 9f, 5f)
                .texOffs(23, 15).addBox(0.5f, 10.5f, -2.5f, 3f, 5f, 0f)
                .texOffs(20, 25).mirror().addBox(0.5f, 10.5f, -0.75f, 3f, 5f, 0f).mirror(false)
                .texOffs(26, 0).addBox(0.5f, 10.5f, 0.75f, 3f, 5f, 0f)
                .texOffs(26, 10).addBox(0.5f, 10.5f, 2.5f, 3f, 5f, 0f),
                PartPose.offset(5f, 2f, 0f));
        return LayerDefinition.create(mesh, 32, 32);
    }
}
