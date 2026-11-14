package com.myriam.unrp.uo;



import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE,
   value = {Dist.CLIENT}
)
public final class ClientOriginLooks {
   public static final double SEEN = 96.0D;
   public static final int SHELL_FADE = 6;
   public static final int MOST_LOOKS = 64;
   public static final double SHELL_RADIUS = 1.35D;
   public static final double BANNER_RADIUS = 2.2D;
   public static final double BALANCE_HEIGHT = 7.8D;
   private static final ResourceLocation WHITE = new ResourceLocation("unrp", "textures/misc/white.png");
   private static final float[] GOLD = new float[]{0.91F, 0.76F, 0.29F};
   private static final float[] PARCH = new float[]{0.95F, 0.91F, 0.79F};
   private static final float[] GUN = new float[]{0.72F, 0.77F, 0.82F};
   private static final float[] AMBER = new float[]{1.0F, 0.65F, 0.17F};
   private static final float[] HOT = new float[]{1.0F, 0.96F, 0.86F};
   private static final int[] LAW_TINT = new int[]{15254090, 16757575, 16773552, 12618302, 16183514, 10517032, 12109920, 15590340, 14247242};
   private static final Map<Integer, long[]> COLOSSI = new HashMap();
   private static final Map<Integer, long[]> SHELLS = new HashMap();
   private static final Map<Integer, Object[]> RECORDS = new HashMap();
   private static final List<ClientOriginLooks.Look> LOOKS = new ArrayList();
   private static ClientLevel where;

   private ClientOriginLooks() {
   }

   public static void accept(OriginLookPacket msg) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         if (mc.level != where) {
            clearAll();
            where = mc.level;
         }

         long now = mc.level.getGameTime();
         switch (msg.kind) {
            case 1:
               if (msg.life <= 0) {
                  COLOSSI.remove(msg.entity);
               } else {
                  COLOSSI.put(msg.entity, new long[]{now + (long)msg.life, (long)msg.get(0)});
               }
               break;
            case 2:
               long[] had = (long[])SHELLS.get(msg.entity);
               if (msg.life <= 0) {
                  if (had != null && had[0] > now) {
                     had[0] = now;
                  }
               } else if (had != null && had[0] >= now) {
                  had[0] = now + (long)msg.life;
               } else {
                  SHELLS.put(msg.entity, new long[]{now + (long)msg.life, now});
               }
               break;
            case 3:
            case 6:
            case 7:
               if (LOOKS.size() >= 64) {
                  LOOKS.remove(0);
               }

               LOOKS.add(new ClientOriginLooks.Look(msg, now));
               break;
            case 4:
               if (!msg.names.isEmpty()) {
                  RECORDS.put(msg.entity, new Object[]{now + (long)msg.life, msg.names.get(0)});
               }
               break;
            case 5:
               ClientSgHold.still(msg.get(0), msg.get(1), msg.get(2), msg.life);
               break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
               ClientLbLooks.accept(msg, now);
               break;
            case 13:
            case 14:
            case 15:
            case 16:
               ClientDmLooks.accept(msg, now);
               break;
            case 17:
            case 18:
            case 19:
               ClientHollowDeep.accept(msg, now);
               break;
            case 20:
            case 21:
               ClientSarcophage.accept(msg, now);
         }

      }
   }

   public static int eyeOver(AbstractClientPlayer player) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return -1;
      } else {
         int dm = ClientDmLooks.borrowed(player.getId(), mc.level.getGameTime());
         if (dm >= 0) {
            return dm;
         } else {
            return LbEdgeLayer.ours(player) && LbEdgeLayer.healthOf(player) <= 0.1F ? LbEdgeLayer.colorOf(player) : -1;
         }
      }
   }

   public static int colossusTier(int entity) {
      long[] c = (long[])COLOSSI.get(entity);
      return c == null ? 0 : (int)c[1];
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            if (mc.level != where) {
               clearAll();
               where = mc.level;
            } else {
               long now = mc.level.getGameTime();
               COLOSSI.values().removeIf((c) -> now > c[0]);
               SHELLS.values().removeIf((s) -> now > s[0] + 6L);
               RECORDS.values().removeIf((r) -> now > (Long)r[0]);
               LOOKS.removeIf((l) -> now > l.born + (long)l.msg.life + 2L || now < l.born - 200L);
            }
         } else {
            if (!COLOSSI.isEmpty() || !SHELLS.isEmpty() || !RECORDS.isEmpty() || !LOOKS.isEmpty() || ClientLbLooks.busy() || ClientDmLooks.busy()) {
               clearAll();
            }

         }
      }
   }

   @SubscribeEvent
   public static void onNameTag(RenderNameTagEvent event) {
      Object[] r = RECORDS.get(event.getEntity().getId());
      if (r != null && !event.getEntity().isInvisible()) {
         event.setContent(event.getContent().copy().append(Component.literal("  " + String.valueOf(r[1]))));
      }
   }

   @SubscribeEvent
   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         if (!SHELLS.isEmpty() || !LOOKS.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null && level == where) {
               Vec3 cam = event.getCamera().getPosition();
               float pt = event.getPartialTick();
               double now = (double)level.getGameTime() + (double)pt;
               MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
               PoseStack ps = event.getPoseStack();
               RenderType type = RenderType.entityTranslucentEmissive(WHITE);
               Matrix4f m = ps.last().pose();
               float fogStart = RenderSystem.getShaderFogStart();
               RenderSystem.setShaderFogStart(1000000.0F);

               try {
                  VertexConsumer vc = buffers.getBuffer(type);
                  boolean first = mc.options.getCameraType() == CameraType.FIRST_PERSON;

                  for(Map.Entry<Integer, long[]> en : SHELLS.entrySet()) {
                     Entity e = level.getEntity(en.getKey());
                     if (e != null && !e.isInvisible() && (!first || e != mc.player)) {
                        shell(vc, m, cam, e, (long[])en.getValue(), now, pt);
                     }
                  }

                  for(ClientOriginLooks.Look l : LOOKS) {
                     double age = now - (double)l.born;
                     if (!(age < 0.0D) && !(age > (double)l.msg.life)) {
                        switch (l.msg.kind) {
                           case 3:
                              banners(vc, m, cam, l.msg, age);
                           case 4:
                           case 5:
                           default:
                              break;
                           case 6:
                              balance(vc, m, cam, level, l.msg, age, pt);
                              break;
                           case 7:
                              scar(vc, m, cam, l.msg, age);
                        }
                     }
                  }

                  buffers.endBatch(type);
               } catch (Throwable var21) {
                  SHELLS.clear();
                  LOOKS.clear();
               } finally {
                  RenderSystem.setShaderFogStart(fogStart);
               }

            }
         }
      }
   }

   private static float[] rgb(int c) {
      return new float[]{(float)(c >> 16 & 255) / 255.0F, (float)(c >> 8 & 255) / 255.0F, (float)(c & 255) / 255.0F};
   }

   private static double smooth(double t) {
      double k = Mth.clamp(t, 0.0D, 1.0D);
      return k * k * (3.0D - 2.0D * k);
   }

   private static void shell(VertexConsumer vc, Matrix4f m, Vec3 cam, Entity e, long[] rec, double now, float pt) {
      Vec3 c = new Vec3(Mth.lerp((double)pt, e.xo, e.getX()), Mth.lerp((double)pt, e.yo, e.getY()) + 1.0D, Mth.lerp((double)pt, e.zo, e.getZ()));
      if (!(c.distanceToSqr(cam) > 9216.0D)) {
         double in = Mth.clamp((now - (double)rec[1]) / 4.0D, 0.0D, 1.0D);
         double out = Mth.clamp(((double)(rec[0] + 6L) - now) / 6.0D, 0.0D, 1.0D);
         float a = (float)Math.min(in, out);
         if (!(a < 0.02F)) {
            double[] lats = new double[]{-0.55D, -0.18D, 0.18D, 0.55D};
            int seed = e.getId();

            for(int k = 0; k < lats.length; ++k) {
               double ry = Math.sin(lats[k]) * 1.35D;
               double rr = Math.cos(lats[k]) * 1.35D;
               double spin = now * (k % 2 == 0 ? 0.02D : -0.025D) + (double)k;
               script(vc, m, cam, c.add(0.0D, ry, 0.0D), rr, spin, 0.16D, seed * 7 + k, GOLD, 0.7F * a, 40);
            }

            for(int k = 0; k < 2; ++k) {
               double yaw = now * 0.015D + (double)k * Math.PI;
               Vec3 prev = null;

               for(int i = 0; i <= 16; ++i) {
                  double lat = (-Math.PI / 2D) + Math.PI * (double)i / 16.0D;
                  Vec3 p = c.add(Math.cos(yaw) * Math.cos(lat) * 1.35D, Math.sin(lat) * 1.35D, Math.sin(yaw) * Math.cos(lat) * 1.35D);
                  if (prev != null) {
                     ClientVertigo.line(vc, m, cam, prev, p, 0.025D, PARCH, 0.3F * a);
                  }

                  prev = p;
               }
            }

         }
      }
   }

   static void script(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 c, double r, double spin, double g, int seed, float[] rgb, float a, int slots) {
      if (!(r < 0.05D)) {
         for(int i = 0; i < slots; ++i) {
            double h = ClientVertigo.hash(seed * 131 + i);
            if (!(h < 0.18D)) {
               double ang = spin + (Math.PI * 2D) * (double)i / (double)slots;
               Vec3 at = c.add(Math.cos(ang) * r, 0.0D, Math.sin(ang) * r);
               Vec3 t = (new Vec3(-Math.sin(ang), 0.0D, Math.cos(ang))).scale(g * 0.32D);
               Vec3 up = new Vec3(0.0D, g * 0.5D, 0.0D);
               double w = Math.max(0.012D, g * 0.11D);
               ClientVertigo.line(vc, m, cam, at.subtract(up), at.add(up), w, rgb, a);
               int bits = (int)(h * 64.0D);
               if ((bits & 1) != 0) {
                  ClientVertigo.line(vc, m, cam, at.add(up).subtract(t), at.add(up).add(t), w, rgb, a);
               }

               if ((bits & 2) != 0) {
                  ClientVertigo.line(vc, m, cam, at.subtract(t), at.add(t), w, rgb, a);
               }

               if ((bits & 4) != 0) {
                  ClientVertigo.line(vc, m, cam, at.subtract(up).subtract(t), at.add(up).add(t), w, rgb, a);
               }

               if ((bits & 24) == 8) {
                  ClientVertigo.line(vc, m, cam, at.subtract(up).subtract(t), at.subtract(up).add(t), w, rgb, a);
               }
            }
         }

      }
   }

   private static void banners(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, double age) {
      Vec3 c = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      if (!(c.distanceToSqr(cam) > 9216.0D)) {
         double h = 1.7D * smooth(age / 8.0D);
         double flick = age >= 20.0D && age <= 24.0D ? 0.25D + 0.75D * Math.abs(Math.cos(Math.PI * (age - 20.0D) / 4.0D)) : 1.0D;
         double bow = Math.toRadians(70.0D) * smooth((age - 26.0D) / 12.0D);
         double sink = 1.9D * smooth((age - 38.0D) / 14.0D);
         float a = (float)(0.72D * flick * (1.0D - smooth((age - 44.0D) / 10.0D)));
         if (!(a < 0.02F) && !(h < 0.02D)) {
            for(int i = 0; i < 9; ++i) {
               double th = (Math.PI * 2D) * (double)i / 9.0D;
               Vec3 n = new Vec3(-Math.cos(th), 0.0D, -Math.sin(th));
               Vec3 t = (new Vec3(-Math.sin(th), 0.0D, Math.cos(th))).scale(0.28D);
               Vec3 base = c.add(Math.cos(th) * 2.2D, -sink, Math.sin(th) * 2.2D);
               Vec3 top = (new Vec3(0.0D, Math.cos(bow), 0.0D)).add(n.scale(Math.sin(bow))).scale(h);
               Vec3 b0 = base.subtract(t);
               Vec3 b1 = base.add(t);
               Vec3 t1 = b1.add(top);
               Vec3 t0 = b0.add(top);
               float[] col = rgb(LAW_TINT[i]);
               ClientVertigo.quad(vc, m, col, b0.x - cam.x, b0.y - cam.y, b0.z - cam.z, b1.x - cam.x, b1.y - cam.y, b1.z - cam.z, t1.x - cam.x, t1.y - cam.y, t1.z - cam.z, t0.x - cam.x, t0.y - cam.y, t0.z - cam.z, a * 0.5F, a * 0.5F, a, a);

               for(int k = 1; k <= 3; ++k) {
                  double f = 0.3D + (double)k * 0.17D;
                  Vec3 l0 = b0.add(top.scale(f)).add(t.scale(0.35D));
                  Vec3 l1 = b1.add(top.scale(f)).subtract(t.scale(0.35D + (double)(k % 2) * 0.3D));
                  ClientVertigo.line(vc, m, cam, l0, l1, 0.03D, PARCH, a * 0.9F);
               }

               ClientVertigo.line(vc, m, cam, t0, t1, 0.05D, HOT, a);
            }

         }
      }
   }

   private static void balance(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientLevel level, OriginLookPacket msg, double age, float pt) {
      Vec3 base = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      Vec3 anchor = base.add(0.0D, 7.8D, 0.0D);
      if (!(anchor.distanceToSqr(cam) > 9216.0D)) {
         boolean even = msg.get(4) > 0.5D;
         double foldAt = (double)msg.life - 10.0D;
         double swing = age < 8.0D ? 0.38D : 0.38D * Math.exp(-(age - 8.0D) / 7.0D) * Math.cos((age - 8.0D) * 0.45D);
         double tilt = age >= 30.0D ? 0.0D : swing;
         double fold = smooth((age - foldAt) / 10.0D);
         Entity e = level.getEntity(msg.entity);
         Minecraft mc = Minecraft.getInstance();
         boolean mine = e != null && e == mc.player && mc.options.getCameraType() == CameraType.FIRST_PERSON;
         Vec3 into = e != null && !e.isInvisible() && !mine ? new Vec3(Mth.lerp((double)pt, e.xo, e.getX()), Mth.lerp((double)pt, e.yo, e.getY()) + 1.15D, Mth.lerp((double)pt, e.zo, e.getZ())) : base.add(0.0D, 1.15D, 0.0D);
         if (mine) {
            fold = 0.0D;
         }

         double gone = 1.0D - smooth((age - foldAt) / 10.0D);
         float post = (float)(smooth(age / 4.0D) * gone);
         float beam = (float)(smooth((age - 3.0D) / 4.0D) * gone);
         float pans = (float)(smooth((age - 6.0D) / 4.0D) * gone);
         Vec3 dir = new Vec3(Math.cos(tilt), Math.sin(tilt), 0.0D);
         Vec3 left = anchor.subtract(dir.scale(1.3D));
         Vec3 right = anchor.add(dir.scale(1.3D));
         Vec3 lowL = left.add(0.0D, -1.0D, 0.0D);
         Vec3 lowR = right.add(0.0D, -1.0D, 0.0D);
         Vec3 foot = anchor.add(0.0D, -1.6D, 0.0D);
         if (post > 0.02F) {
            seg(vc, m, cam, foot, anchor, into, fold, 0.07D, GUN, 0.85F * post);
            seg(vc, m, cam, foot.add(-0.35D, 0.0D, 0.0D), foot.add(0.35D, 0.0D, 0.0D), into, fold, 0.06D, GUN, 0.85F * post);
         }

         if (beam > 0.02F) {
            seg(vc, m, cam, left, right, into, fold, 0.07D, GUN, 0.9F * beam);
            seg(vc, m, cam, anchor.add(0.0D, 0.18D, 0.0D), anchor.add(0.0D, -0.18D, 0.0D), into, fold, 0.1D, AMBER, 0.9F * beam);
         }

         if (pans > 0.02F) {
            for(Vec3[] side : new Vec3[][]{{left, lowL}, {right, lowR}}) {
               seg(vc, m, cam, side[0], side[1].add(-0.42D, 0.0D, 0.0D), into, fold, 0.025D, GUN, 0.8F * pans);
               seg(vc, m, cam, side[0], side[1].add(0.42D, 0.0D, 0.0D), into, fold, 0.025D, GUN, 0.8F * pans);
               Vec3 pc = side[1].lerp(into, fold);
               double pr = 0.45D * (1.0D - fold * 0.8D);
               ClientVertigo.flatRing(vc, m, cam, pc.x, pc.y, pc.z, pr - 0.06D, pr, 0.0D, GUN, 0.85F * pans, 0.85F * pans, 20);
               ClientVertigo.flatRing(vc, m, cam, pc.x, pc.y + 0.01D, pc.z, 0.0D, pr - 0.06D, 0.0D, AMBER, 0.35F * pans, 0.05F * pans, 20);
            }
         }

         if (even && age >= 30.0D) {
            float ring = (float)(0.6D * smooth((age - 30.0D) / 3.0D) * gone);
            ClientVertigo.flatRing(vc, m, cam, anchor.x, anchor.y + 0.02D, anchor.z, 0.2D, 0.32D, age * 0.2D, HOT, ring, 0.0F, 16);
         }

      }
   }

   private static void seg(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 a, Vec3 b, Vec3 into, double fold, double width, float[] rgb, float alpha) {
      Vec3 fa = a.lerp(into, fold);
      Vec3 fb = b.lerp(into, fold);
      ClientVertigo.line(vc, m, cam, fa, fb, width * (1.0D - fold * 0.6D), rgb, alpha);
   }

   private static void scar(VertexConsumer vc, Matrix4f m, Vec3 cam, OriginLookPacket msg, double age) {
      Vec3 from = new Vec3(msg.get(0), msg.get(1), msg.get(2));
      Vec3 to = new Vec3(msg.get(3), msg.get(4), msg.get(5));
      Vec3 d = to.subtract(from);
      double len = d.length();
      if (!(len < 1.5D)) {
         Vec3 dir = d.scale(1.0D / len);
         Vec3 start = from.add(dir);
         double along = Mth.clamp(cam.subtract(start).dot(dir), 0.0D, len - 1.0D);
         double near = cam.distanceTo(start.add(dir.scale(along)));
         double s = Mth.clamp(age / (double)Math.max(1, msg.life), 0.0D, 1.0D);
         float a = (float)((1.0D - s) * Mth.clamp((near - 0.4D) / 0.8D, 0.0D, 1.0D));
         if (!(a < 0.02F)) {
            ClientVertigo.line(vc, m, cam, start, to, 0.13D, AMBER, 0.35F * a);
            ClientVertigo.line(vc, m, cam, start, to, 0.035D, HOT, 0.95F * a);
         }
      }
   }

   public static void clearAll() {
      COLOSSI.clear();
      SHELLS.clear();
      RECORDS.clear();
      LOOKS.clear();
      ClientSarcophage.clearAll();
      ClientLbLooks.clearAll();
      ClientDmLooks.clearAll();
      where = null;
   }

   private static final class Look {
      final OriginLookPacket msg;
      final long born;

      Look(OriginLookPacket msg, long born) {
         this.msg = msg;
         this.born = born;
      }
   }
}
