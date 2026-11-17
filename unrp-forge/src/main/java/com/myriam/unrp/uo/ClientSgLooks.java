package com.myriam.unrp.uo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

public final class ClientSgLooks {
   private ClientSgLooks() {}

   public static Vec3[] facing(Vec3 cam, Vec3 c) {
      Vec3 fwd = c.subtract(cam);
      if (fwd.lengthSqr() < 1.0E-6D) {
         fwd = new Vec3(0.0D, 0.0D, 1.0D);
      }

      fwd = fwd.normalize();
      Vec3 right = fwd.cross(new Vec3(0.0D, 1.0D, 0.0D));
      if (right.lengthSqr() < 1.0E-6D) {
         right = new Vec3(1.0D, 0.0D, 0.0D);
      }

      right = right.normalize();
      Vec3 up = right.cross(fwd).normalize();
      return new Vec3[]{right, up};
   }

   public static void disc(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 c, Vec3 right, Vec3 up, double inner, double outer, float[] rgb, float ai, float ao, int n) {
      for(int i = 0; i < n; ++i) {
         double a0 = (Math.PI * 2D) * (double)i / (double)n;
         double a1 = (Math.PI * 2D) * (double)(i + 1) / (double)n;
         Vec3 i0 = c.add(right.scale(Math.cos(a0) * inner)).add(up.scale(Math.sin(a0) * inner));
         Vec3 i1 = c.add(right.scale(Math.cos(a1) * inner)).add(up.scale(Math.sin(a1) * inner));
         Vec3 o1 = c.add(right.scale(Math.cos(a1) * outer)).add(up.scale(Math.sin(a1) * outer));
         Vec3 o0 = c.add(right.scale(Math.cos(a0) * outer)).add(up.scale(Math.sin(a0) * outer));
         ClientVertigo.quad(vc, m, rgb, i0.x - cam.x, i0.y - cam.y, i0.z - cam.z, i1.x - cam.x, i1.y - cam.y, i1.z - cam.z, o1.x - cam.x, o1.y - cam.y, o1.z - cam.z, o0.x - cam.x, o0.y - cam.y, o0.z - cam.z, ai, ai, ao, ao);
      }

   }
}
