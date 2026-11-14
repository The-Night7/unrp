package com.myriam.unrp.uo;

import com.myriam.unrp.anim.MoveAnimClient;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class SaltAutopilot {
   static final long FRESH_MS = 1000L;
   private static int mode;
   private static int target;
   private static double[] goal = new double[]{0.0D, 0.0D, 0.0D};
   private static long seenMs;
   private static float yawNow;
   private static float yawWas;
   private static float pitchNow;
   private static float pitchWas;
   private static boolean aimed;
   private static ItemStack spear;
   private static final Map<Integer, ItemStack> STASH = new HashMap();

   private SaltAutopilot() {
   }

   public static void steer(int m, int t, float x, float y, float z) {
      mode = m;
      target = t;
      goal = new double[]{(double)x, (double)y, (double)z};
      seenMs = System.currentTimeMillis();
      if (m == 0) {
         aimed = false;
      }

   }

   public static boolean active() {
      return mode != 0 && System.currentTimeMillis() - seenMs < 1000L;
   }

   static ItemStack spearStack() {
      if (spear == null) {
         spear = new ItemStack(com.myriam.unrp.item.ModItems.SALT_SPEAR.get());
      }

      return spear;
   }

   static double[] goalOf(LocalPlayer p) {
      if (target != 0) {
         Entity e = p.level().getEntity(target);
         if (e != null && !e.isRemoved()) {
            return new double[]{e.getX(), e.getY() + (double)e.getBbHeight() * 0.72D, e.getZ()};
         }
      }

      return new double[]{goal[0], goal[1] + 1.2D, goal[2]};
   }

   static void face(LocalPlayer p, double[] g) {
      double[] eye = new double[]{p.getX(), p.getEyeY(), p.getZ()};
      double dx = g[0] - eye[0];
      double dz = g[2] - eye[2];
      float yaw = (float)SaltMath.yawTo(eye, g);
      float pitch = (float)Mth.clamp(-Math.toDegrees(Math.atan2(g[1] - eye[1], Math.sqrt(dx * dx + dz * dz))), -60.0D, 60.0D);
      if (!aimed) {
         yawWas = yaw;
         pitchWas = pitch;
         aimed = true;
      } else {
         yawWas = yawNow;
         pitchWas = pitchNow;
      }

      yawNow = yawWas + Mth.wrapDegrees(yaw - yawWas);
      pitchNow = pitch;
      p.setYRot(yawNow);
      p.setXRot(pitchNow);
      p.yHeadRot = yawNow;
   }

   static void drive(LocalPlayer p) {
      Vec3 v = p.getDeltaMovement();
      if (mode != 1 && mode != 5) {
         double[] g = goalOf(p);
         face(p, g);
         double[] vel = new double[]{v.x, v.y, v.z};
         if (mode == 3) {
            vel = SaltMath.slowVel(vel, 0.5D);
         } else if (mode == 4) {
            vel = SaltMath.slowVel(vel, 0.3D);
         } else {
            double[] pos = new double[]{p.getX(), p.getY(), p.getZ()};
            double[] dir = SaltMath.flat(SaltMath.sub(g, pos));
            int wall = SaltTerrain.wallAhead(p.level(), pos, dir);
            boolean wet = wall == 0 && SaltTerrain.leapAhead(p.level(), pos, dir);
            vel = SaltMath.runVel(dir, vel, p.onGround(), wall, wet);
         }

         p.setDeltaMovement(vel[0], vel[1], vel[2]);
      } else {
         p.setDeltaMovement(0.0D, v.y, 0.0D);
      }
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.START) {
         Minecraft mc = Minecraft.getInstance();
         restore(mc);
         LocalPlayer p = mc.player;
         if (p != null && mc.level != null) {
            if (!active()) {
               aimed = false;
            } else {
               while(mc.options.keyDrop.consumeClick()) {
                  mc.options.keyDrop.setDown(false);
               }

               while(mc.options.keySwapOffhand.consumeClick()) {
                  mc.options.keySwapOffhand.setDown(false);
               }

               p.setSprinting(false);
               drive(p);
            }
         } else {
            mode = 0;
            aimed = false;
         }
      }
   }

   @SubscribeEvent
   public static void onRenderTick(TickEvent.RenderTickEvent event) {
      if (event.phase == Phase.START && active() && aimed) {
         LocalPlayer p = Minecraft.getInstance().player;
         if (p != null && mode != 1 && mode != 5) {
            float k = Mth.clamp(event.renderTickTime, 0.0F, 1.0F);
            float yaw = Mth.lerp(k, yawWas, yawNow);
            float pitch = Mth.lerp(k, pitchWas, pitchNow);
            p.setYRot(yaw);
            p.setXRot(pitch);
            p.yRotO = yaw;
            p.xRotO = pitch;
         }
      }
   }

   @SubscribeEvent
   public static void onInput(MovementInputUpdateEvent event) {
      if (active()) {
         Input in = event.getInput();
         in.forwardImpulse = 0.0F;
         in.leftImpulse = 0.0F;
         in.up = false;
         in.down = false;
         in.left = false;
         in.right = false;
         in.jumping = false;
         in.shiftKeyDown = false;
      }
   }

   @SubscribeEvent
   public static void onClick(InputEvent.InteractionKeyMappingTriggered event) {
      if (active()) {
         event.setSwingHand(false);
         event.setCanceled(true);
      }

   }

   static boolean speared(AbstractClientPlayer p, float pt) {
      return p != null && !p.isInvisible() && !p.isSpectator() && ClientSaltSeraph.spear(p.getId(), (double)((float)p.level().getGameTime() + pt));
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void onHand(RenderHandEvent event) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer p = mc.player;
      if (event.getHand() == InteractionHand.MAIN_HAND && speared(p, event.getPartialTick())) {
         event.setCanceled(true);
         PoseStack pose = event.getPoseStack();
         boolean right = p.getMainArm() == HumanoidArm.RIGHT;
         int l = right ? 1 : -1;
         float sw = event.getSwingProgress();
         float eq = event.getEquipProgress();
         pose.pushPose();

         try {
            MoveAnimClient.hand(p, InteractionHand.MAIN_HAND, event.getPartialTick(), pose);
            float f5 = -0.4F * Mth.sin(Mth.sqrt(sw) * (float)Math.PI);
            float f6 = 0.2F * Mth.sin(Mth.sqrt(sw) * ((float)Math.PI * 2F));
            float f10 = -0.2F * Mth.sin(sw * (float)Math.PI);
            pose.translate((float)l * f5, f6, f10);
            pose.translate((float)l * 0.56F, -0.52F + eq * -0.6F, -0.72F);
            float f = Mth.sin(sw * sw * (float)Math.PI);
            pose.mulPose(Axis.YP.rotationDegrees((float)l * (45.0F + f * -20.0F)));
            float f1 = Mth.sin(Mth.sqrt(sw) * (float)Math.PI);
            pose.mulPose(Axis.ZP.rotationDegrees((float)l * f1 * -20.0F));
            pose.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
            pose.mulPose(Axis.YP.rotationDegrees((float)l * -45.0F));
            mc.gameRenderer.itemInHandRenderer.renderItem(p, spearStack(), right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !right, pose, event.getMultiBufferSource(), event.getPackedLight());
         } finally {
            pose.popPose();
         }

      }
   }

   @SubscribeEvent
   public static void onBodyPre(RenderPlayerEvent.Pre event) {
      Player var2 = event.getEntity();
      if (var2 instanceof AbstractClientPlayer p) {
         if (speared(p, event.getPartialTick())) {
            if (!STASH.containsKey(p.getId())) {
               STASH.put(p.getId(), p.getItemBySlot(EquipmentSlot.MAINHAND));
            }

            p.setItemSlot(EquipmentSlot.MAINHAND, spearStack());
            return;
         }
      }

   }

   @SubscribeEvent
   public static void onBodyPost(RenderPlayerEvent.Post event) {
      ItemStack was = (ItemStack)STASH.remove(event.getEntity().getId());
      if (was != null) {
         event.getEntity().setItemSlot(EquipmentSlot.MAINHAND, was);
      }

   }

   static void restore(Minecraft mc) {
      if (!STASH.isEmpty()) {
         for(Iterator<Map.Entry<Integer, ItemStack>> it = STASH.entrySet().iterator(); it.hasNext(); it.remove()) {
            Map.Entry<Integer, ItemStack> en = (Map.Entry)it.next();
            Entity e = mc.level == null ? null : mc.level.getEntity(en.getKey());
            if (e instanceof AbstractClientPlayer) {
               AbstractClientPlayer p = (AbstractClientPlayer)e;
               p.setItemSlot(EquipmentSlot.MAINHAND, (ItemStack)en.getValue());
            }
         }

      }
   }
}
