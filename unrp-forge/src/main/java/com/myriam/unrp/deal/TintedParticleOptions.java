package com.myriam.unrp.deal;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class TintedParticleOptions implements ParticleOptions {
   public static final ParticleOptions.Deserializer<TintedParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<TintedParticleOptions>() {
      public TintedParticleOptions fromCommand(ParticleType<TintedParticleOptions> type, StringReader reader) throws CommandSyntaxException {
         reader.skipWhitespace();
         int rgb = reader.canRead() ? reader.readInt() : 16777215;
         return new TintedParticleOptions(type, rgb);
      }

      public TintedParticleOptions fromNetwork(ParticleType<TintedParticleOptions> type, FriendlyByteBuf buf) {
         return new TintedParticleOptions(type, buf.readInt());
      }
   };
   private final ParticleType<TintedParticleOptions> type;
   private final int rgb;

   public TintedParticleOptions(ParticleType<TintedParticleOptions> type, int rgb) {
      this.type = type;
      this.rgb = rgb & 16777215;
   }

   public static Codec<TintedParticleOptions> codec(ParticleType<TintedParticleOptions> type) {
      return Codec.INT.fieldOf("rgb").codec().xmap((rgb) -> new TintedParticleOptions(type, rgb), (o) -> o.rgb);
   }

   public int rgb() {
      return this.rgb;
   }

   public float red() {
      return (float)(this.rgb >> 16 & 255) / 255.0F;
   }

   public float green() {
      return (float)(this.rgb >> 8 & 255) / 255.0F;
   }

   public float blue() {
      return (float)(this.rgb & 255) / 255.0F;
   }

   public ParticleType<?> getType() {
      return this.type;
   }

   public void writeToNetwork(FriendlyByteBuf buf) {
      buf.writeInt(this.rgb);
   }

   public String writeToString() {
      return String.valueOf(BuiltInRegistries.PARTICLE_TYPE.getKey(this.type)) + " " + this.rgb;
   }
}
