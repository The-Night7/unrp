package com.myriam.unrp.look;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.minecraftforge.fml.loading.FMLPaths;

public class ClientHudConfig {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static boolean loaded = false;
   private static int hudScalePct = 100;
   private static int menuScalePct = 100;
   private static int anchor = 0;
   private static int offX = 8;
   private static int offY = 8;
   private static boolean beamGlow = true;
   private static boolean screenEffects = true;
   private static boolean hubMotion = true;
   private static boolean hubSound = true;
   private static boolean hubFramed = false;
   private static boolean powerPoses = true;

   private static Path file() {
      return FMLPaths.CONFIGDIR.get().resolve("unrp-client.json");
   }

   private static void load() {
      if (!loaded) {
         loaded = true;

         try {
            Path p = file();
            if (!Files.exists(p, new LinkOption[0])) {
               return;
            }

            JsonObject o = (JsonObject)GSON.fromJson(Files.readString(p), JsonObject.class);
            if (o == null) {
               return;
            }

            if (o.has("hudScalePct")) {
               hudScalePct = o.get("hudScalePct").getAsInt();
            }

            if (o.has("menuScalePct")) {
               menuScalePct = o.get("menuScalePct").getAsInt();
            }

            if (o.has("anchor")) {
               anchor = o.get("anchor").getAsInt();
            }

            if (o.has("offX")) {
               offX = o.get("offX").getAsInt();
            }

            if (o.has("offY")) {
               offY = o.get("offY").getAsInt();
            }

            if (o.has("beamGlow")) {
               beamGlow = o.get("beamGlow").getAsBoolean();
            }

            if (o.has("screenEffects")) {
               screenEffects = o.get("screenEffects").getAsBoolean();
            }

            if (o.has("hubMotion")) {
               hubMotion = o.get("hubMotion").getAsBoolean();
            }

            if (o.has("hubSound")) {
               hubSound = o.get("hubSound").getAsBoolean();
            }

            if (o.has("hubFramed")) {
               hubFramed = o.get("hubFramed").getAsBoolean();
            }

            if (o.has("powerPoses")) {
               powerPoses = o.get("powerPoses").getAsBoolean();
            }

            com.myriam.unrp.anim.MoveAnimClient.posesOff = !powerPoses;
            clampAll();
         } catch (Exception var2) {
         }

      }
   }

   private static void clampAll() {
      hudScalePct = Math.max(50, Math.min(150, hudScalePct));
      menuScalePct = Math.max(60, Math.min(140, menuScalePct));
      anchor = Math.max(0, Math.min(3, anchor));
      offX = Math.max(0, Math.min(600, offX));
      offY = Math.max(0, Math.min(600, offY));
   }

   public static void set(int hudPct, int menuPct, int anchorIn, int offXIn, int offYIn, boolean beamGlowIn) {
      load();
      hudScalePct = hudPct;
      menuScalePct = menuPct;
      anchor = anchorIn;
      offX = offXIn;
      offY = offYIn;
      beamGlow = beamGlowIn;
      clampAll();
      save();
   }

   public static void setScreenEffects(boolean on) {
      load();
      screenEffects = on;
      save();
   }

   public static void setHub(boolean motion, boolean sound) {
      load();
      hubMotion = motion;
      hubSound = sound;
      save();
   }

   private static void save() {
      try {
         JsonObject o = new JsonObject();
         o.addProperty("hudScalePct", hudScalePct);
         o.addProperty("menuScalePct", menuScalePct);
         o.addProperty("anchor", anchor);
         o.addProperty("offX", offX);
         o.addProperty("offY", offY);
         o.addProperty("beamGlow", beamGlow);
         o.addProperty("screenEffects", screenEffects);
         o.addProperty("hubMotion", hubMotion);
         o.addProperty("hubSound", hubSound);
         o.addProperty("hubFramed", hubFramed);
         o.addProperty("powerPoses", powerPoses);
         Files.writeString(file(), GSON.toJson(o));
      } catch (IOException var1) {
      }

   }

   public static float hudScale() {
      load();
      return (float)hudScalePct / 100.0F;
   }

   public static float menuScale() {
      load();
      return (float)menuScalePct / 100.0F;
   }

   public static int hudScalePct() {
      load();
      return hudScalePct;
   }

   public static int menuScalePct() {
      load();
      return menuScalePct;
   }

   public static int hudAnchor() {
      load();
      return anchor;
   }

   public static int hudOffX() {
      load();
      return offX;
   }

   public static int hudOffY() {
      load();
      return offY;
   }

   public static boolean beamGlow() {
      load();
      return beamGlow;
   }

   public static boolean screenEffects() {
      load();
      return screenEffects;
   }

   public static boolean hubMotion() {
      load();
      return hubMotion;
   }

   public static boolean hubSound() {
      load();
      return hubSound;
   }

   public static void setHubFramed(boolean framed) {
      load();
      hubFramed = framed;
      save();
   }

   public static boolean hubFramed() {
      load();
      return hubFramed;
   }

   public static void setPowerPoses(boolean on) {
      load();
      powerPoses = on;
      com.myriam.unrp.anim.MoveAnimClient.posesOff = !on;
      save();
   }

   public static boolean powerPoses() {
      load();
      return powerPoses;
   }
}
