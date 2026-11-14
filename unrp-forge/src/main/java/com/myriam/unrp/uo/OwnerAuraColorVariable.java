package com.myriam.unrp.uo;

import com.myriam.unrp.look.ClientSettingsCache;


import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.client.dynamictexture.variable.ITextureVariable;
import net.threetag.palladium.client.dynamictexture.variable.ITextureVariableSerializer;
import net.threetag.palladium.documentation.JsonDocumentationBuilder;
import net.threetag.palladium.util.context.DataContext;

public class OwnerAuraColorVariable implements ITextureVariable {
   private final int fallback;

   public OwnerAuraColorVariable() {
      this(16777215);
   }

   public OwnerAuraColorVariable(int fallback) {
      this.fallback = fallback;
   }

   public Object get(DataContext context) {
      Entity entity = context.getEntity();
      if (entity == null) {
         return this.fallback;
      } else {
         Player owner = null;
         if (entity instanceof Player) {
            Player p_0 = (Player)entity;
            owner = p_0;
         } else if (entity instanceof Projectile) {
            Projectile proj = (Projectile)entity;
            Entity var7 = proj.getOwner();
            if (var7 instanceof Player) {
               Player p = (Player)var7;
               owner = p;
            }
         }

         if (owner == null) {
            LocalPlayer lp = Minecraft.getInstance().player;
            if (lp != null) {
               owner = lp;
            }
         }

         if (owner == null) {
            return this.fallback;
         } else if (ClientSettingsCache.isAM(owner.getUUID())) {
            return this.fallback;
         } else {
            int c = ClientSettingsCache.getColor(owner.getUUID());
            return c == -1 ? this.fallback : c;
         }
      }
   }

   public static class Serializer implements ITextureVariableSerializer {
      public static final ResourceLocation ID = new ResourceLocation("unrp", "owner_aura");

      public ITextureVariable parse(JsonObject json) {
         int fb = 16777215;
         if (json.has("default")) {
            String s = json.get("default").getAsString();
            if (s.startsWith("#")) {
               s = s.substring(1);
            }

            fb = Integer.parseInt(s, 16);
         }

         return new OwnerAuraColorVariable(fb);
      }

      public ResourceLocation getId() {
         return ID;
      }

      public void addDocumentationFields(JsonDocumentationBuilder builder) {
      }

      public String getDocumentationDescription() {
         return "Reads the unrp aura color of the entity or its owner.";
      }
   }
}
