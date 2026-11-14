package com.myriam.unrp.uo;

import com.myriam.unrp.deal.Spectacle;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

/** NadirEvents du modèle, réduit aux fonctions utilisées. Cratères de terrain (creusés puis rebouchés), partagés par Black Lash, Daystar et Nadir. */
@EventBusSubscriber(
   modid = "unrp",
   bus = Bus.FORGE
)
public final class NadirEvents {
   private NadirEvents() {}

   /** Rebouchage des cratères (part de onServerTick du modèle). */
   @SubscribeEvent
   public static void onServerTick(TickEvent.ServerTickEvent event) {
      if (event.phase == TickEvent.Phase.END) {
         MinecraftServer server = event.getServer();
         if (!REFILLS.isEmpty() && server.getTickCount() % 20 == 0) {
            tickRefills(server);
         }
      }
   }

   /** Cratères laissés ouverts à l'arrêt : rebouchés au démarrage (onServerStarted du modèle). */
   @SubscribeEvent
   public static void onServerStarted(ServerStartedEvent event) {
      MinecraftServer server = event.getServer();
      List<String> pending = new ArrayList<>();
      for (String book_0 : VoidLedger.get(server).books()) {
         if (book_0.startsWith("ndcrater_")) pending.add(book_0);
      }
      pending.sort(Comparator.comparingInt((b) -> b.contains("_fell_") ? 1 : 0));
      for (String book : pending) {
         try {
            refill(server, book, true);
         } catch (RuntimeException ignored) {
         }
      }
   }

   @SubscribeEvent
   public static void onServerStopped(ServerStoppedEvent event) {
      REFILLS.clear();
   }

   public static final int CRATER_KEEP = 400;

   public static final int CRATER_RETRY = 40;

   public static final int CRATER_TRIES = 90;

   public static final double CRATER_SEEN = 128.0D;

   public static final String CRATER_BOOK = "ndcrater_";

   public static final Map<String, long[]> REFILLS = new HashMap();

   public static long books;


   public static int crater(ServerLevel level, Vec3 at, double radius, ServerPlayer owner) {
      int dug = 0;
      if (owner != null && realCraters(level)) {
         BlockPos c = BlockPos.containing(at.x, at.y - 0.5D, at.z);
         int r = (int)Math.ceil(radius);
         List<BlockPos> bowl = new ArrayList();

         for(int dx = -r; dx <= r; ++dx) {
            for(int dz = -r; dz <= r; ++dz) {
               for(int dy = 0; dy >= -r; --dy) {
                  if ((double)(dx * dx + dz * dz) + (double)(dy * dy) * 2.6D <= radius * radius) {
                     bowl.add(c.offset(dx, dy, dz));
                  }
               }
            }
         }

         dug = carve(level, owner, "ndcrater_" + String.valueOf(owner.getUUID()) + "_" + level.getGameTime() + "_" + c.getX() + "_" + c.getZ(), bowl, 400);
      }

      craterFx(level, at, radius, 400, dug > 0);
      return dug;
   }

   public static void craterFx(ServerLevel level, Vec3 at, double radius, int keep, boolean real) {
      CraterPacket msg = new CraterPacket(at.x, at.y, at.z, (float)radius, keep, real);
      double seen = 16384.0D;

      for(ServerPlayer p : level.players()) {
         if (p.position().distanceToSqr(at) <= seen) {
            com.myriam.unrp.net.Net.CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), msg);
         }
      }

      Spectacle.debris(level, at, radius, (int)Math.min(40.0D, 10.0D + radius * 6.0D));
   }

   public static int carve(ServerLevel level, ServerPlayer owner, String book, List<BlockPos> spots, int keep) {
      return carve(level, owner, book, spots, keep, false);
   }

   public static int carve(ServerLevel level, ServerPlayer owner, String book, List<BlockPos> spots, int keep, boolean fell) {
      if (owner != null && !spots.isEmpty()) {
         int x0 = Integer.MAX_VALUE;
         int y0 = Integer.MAX_VALUE;
         int z0 = Integer.MAX_VALUE;
         int x1 = Integer.MIN_VALUE;
         int y1 = Integer.MIN_VALUE;
         int z1 = Integer.MIN_VALUE;

         for(BlockPos p_0 : spots) {
            x0 = Math.min(x0, p_0.getX());
            y0 = Math.min(y0, p_0.getY());
            z0 = Math.min(z0, p_0.getZ());
            x1 = Math.max(x1, p_0.getX());
            y1 = Math.max(y1, p_0.getY());
            z1 = Math.max(z1, p_0.getZ());
         }

         Set<Long> spared = new HashSet();
         AABB area = new AABB((double)x0 - 1.0D, (double)y0, (double)z0 - 1.0D, (double)x1 + 2.0D, (double)y1 + 5.0D, (double)z1 + 2.0D);

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (e != owner && (!OriginStrike.fair(owner, e) || TimeWeft.everpresent(e))) {
               AABB box = e.getBoundingBox().inflate(0.3D, 0.0D, 0.3D);

               for(int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); ++x) {
                  for(int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); ++z) {
                     spared.add(column(x, z));
                  }
               }
            }
         }

         VoidLedger ledger = VoidLedger.get(level.getServer());
         boolean heal = level.getGameRules().getBoolean(UnrpGameRules.CRATERS_HEAL);
         Map<Long, List<BlockPos>> columns = new LinkedHashMap();

         for(BlockPos p : spots) {
            ((List)columns.computeIfAbsent(column(p.getX(), p.getZ()), (k) -> new ArrayList())).add(p);
         }

         List<BlockPos> cut = new ArrayList();
         List<BlockPos> stems = new ArrayList();
         Map<BlockPos, Boolean> wood = new HashMap();

         for(BlockPos p : spots) {
            BlockPos t = fell ? p : p.above();
            if (!wood.containsKey(t) && trunk(level.getBlockState(t))) {
               wildWood(level, t, wood);
            }
         }

         boolean woody = false;

         for(Map.Entry<Long, List<BlockPos>> col : columns.entrySet()) {
            if (!spared.contains(col.getKey())) {
               List<BlockPos> cells = (List)col.getValue();
               cells.sort((c1, c2) -> Integer.compare(c2.getY(), c1.getY()));

               for(BlockPos p : cells) {
                  BlockState st = level.getBlockState(p);
                  if (!st.isAir()) {
                     boolean ok = fell ? growth(level, p, st) && (!trunk(st) || wildWood(level, p, wood)) : natural(level, p, st) && (!trunk(level.getBlockState(p.above())) || wildWood(level, p.above(), wood));
                     if (ok && !holdsSign(level, p) && level.mayInteract(owner, p)) {
                        if (heal) {
                           ledger.mark(book, level, p, st);
                           level.setBlock(p, Blocks.AIR.defaultBlockState(), 18);
                           takeAlong(level, ledger, book, p.above());
                        } else {
                           clearWild(level, p.above());
                           level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                           woody |= trunk(st) || canopy(st) || canopy(level.getBlockState(p.above()));
                           if (trunk(st)) {
                              stems.add(p);
                           }
                        }

                        cut.add(p);
                     } else if (!st.getCollisionShape(level, p).isEmpty() && !anchored(level, p) && (fell || !trunk(st) && !canopy(st))) {
                        break;
                     }
                  }
               }
            }
         }

         int dug = cut.size();
         if (dug > 0 && heal) {
            REFILLS.put(book, new long[]{(long)(level.getServer().getTickCount() + keep), 0L});
         }

         if (dug > 0 && !heal) {
            topple(level, owner, cut, stems, woody);
         }

         return dug;
      } else {
         return 0;
      }
   }

   public static long column(int x, int z) {
      return (long)x << 32 ^ (long)z & 4294967295L;
   }

   public static boolean natural(ServerLevel level, BlockPos pos, BlockState st) {
      if (earthy(level, pos, st) && !(st.getBlock() instanceof Fallable) && !(level.getBlockState(pos.above()).getBlock() instanceof Fallable) && !level.getBlockState(pos.below()).is(Blocks.POINTED_DRIPSTONE) && bare(level, pos.above())) {
         for(Direction d : Direction.values()) {
            if (!level.getFluidState(pos.relative(d)).isEmpty() || !level.getFluidState(pos.above().relative(d)).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean growth(ServerLevel level, BlockPos pos, BlockState st) {
      if (!st.isAir() && st.getFluidState().isEmpty() && level.getBlockEntity(pos) == null) {
         float hard = st.getDestroySpeed(level, pos);
         if (!(hard < 0.0F) && !(hard > 3.0F) && !st.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            if (persistent(st)) {
               return false;
            } else if (!st.is(BlockTags.LOGS) && !st.is(BlockTags.LEAVES) && !st.is(BlockTags.FLOWERS) && !st.is(BlockTags.SAPLINGS) && !st.is(BlockTags.WART_BLOCKS) && !st.is(Blocks.GRASS) && !st.is(Blocks.FERN) && !st.is(Blocks.DEAD_BUSH) && !st.is(Blocks.BAMBOO) && !st.is(Blocks.CACTUS) && !st.is(Blocks.SUGAR_CANE) && !st.is(Blocks.BROWN_MUSHROOM_BLOCK) && !st.is(Blocks.RED_MUSHROOM_BLOCK) && !st.is(Blocks.MUSHROOM_STEM) && !st.is(Blocks.SHROOMLIGHT)) {
               return false;
            } else {
               for(Direction d : Direction.values()) {
                  if (!level.getFluidState(pos.relative(d)).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isGrowth(ServerLevel level, BlockPos pos, BlockState st) {
      return growth(level, pos, st);
   }

   public static boolean trunk(BlockState st) {
      return st.is(BlockTags.LOGS) || st.is(Blocks.MUSHROOM_STEM);
   }

   public static boolean wildWood(ServerLevel level, BlockPos start, Map<BlockPos, Boolean> wood) {
      Boolean known = (Boolean)wood.get(start);
      if (known != null) {
         return known;
      } else {
         List<BlockPos> logs = new ArrayList();
         Set<BlockPos> seen = new HashSet();
         ArrayDeque<BlockPos> open = new ArrayDeque();
         seen.add(start);
         open.add(start);
         boolean leafy = false;
         boolean signed = false;

         while(!open.isEmpty() && logs.size() <= 1024) {
            BlockPos p_0 = (BlockPos)open.poll();
            logs.add(p_0);
            signed |= holdsSign(level, p_0);

            for(Direction d : Direction.values()) {
               BlockPos q_0 = p_0.relative(d);
               if (level.isLoaded(q_0) && grown(level.getBlockState(q_0))) {
                  leafy = true;
               }
            }

            for(int dx = -1; dx <= 1; ++dx) {
               for(int dy = -1; dy <= 1; ++dy) {
                  for(int dz = -1; dz <= 1; ++dz) {
                     BlockPos q = p_0.offset(dx, dy, dz);
                     if (!seen.contains(q) && level.isLoaded(q) && trunk(level.getBlockState(q))) {
                        seen.add(q);
                        open.add(q);
                     }
                  }
               }
            }
         }

         boolean wild = leafy && !signed && open.isEmpty();

         for(BlockPos p : logs) {
            wood.put(p, wild);
         }

         return wild;
      }
   }

   public static void clearWild(ServerLevel level, BlockPos q) {
      BlockState st = level.getBlockState(q);
      if (!st.is(Blocks.BAMBOO)) {
         if (wild(st)) {
            if (st.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && st.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER && level.getBlockState(q.above()).is(st.getBlock())) {
               level.setBlock(q.above(), Blocks.AIR.defaultBlockState(), 18);
            }

            level.setBlock(q, Blocks.AIR.defaultBlockState(), 18);
         }
      } else {
         BlockPos stalk = q;

         for(int k = 0; k < 32 && level.getBlockState(stalk).is(Blocks.BAMBOO); ++k) {
            level.setBlock(stalk, Blocks.AIR.defaultBlockState(), 18);
            stalk = stalk.above();
         }

      }
   }

   public static boolean canopy(BlockState st) {
      return st.is(BlockTags.LEAVES) || st.is(BlockTags.WART_BLOCKS) || st.is(Blocks.SHROOMLIGHT) || st.is(Blocks.BROWN_MUSHROOM_BLOCK) || st.is(Blocks.RED_MUSHROOM_BLOCK);
   }

   public static boolean holdsSign(ServerLevel level, BlockPos p) {
      for(Direction d : Direction.values()) {
         BlockPos q = p.relative(d);
         if (level.isLoaded(q)) {
            Object be = level.getBlockEntity(q);
            if (be instanceof SignBlockEntity || be instanceof BannerBlockEntity) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean anchored(ServerLevel level, BlockPos p) {
      BlockPos q = p;

      for(int k = 0; k < 4; ++k) {
         q = q.above();
         BlockState s = level.getBlockState(q);
         if (earthy(level, q, s) && !(s.getBlock() instanceof Fallable)) {
            return true;
         }

         if (!s.is(net.minecraftforge.common.Tags.Blocks.ORES) && !(s.getBlock() instanceof Fallable) && !s.is(BlockTags.TERRACOTTA)) {
            return false;
         }
      }

      return false;
   }

   public static void takeAlong(ServerLevel level, VoidLedger ledger, String book, BlockPos q) {
      BlockPos cell = q;

      for(int k = 0; k < 32 && stranded(level, cell); ++k) {
         ledger.mark(book, level, cell, level.getBlockState(cell));
         level.setBlock(cell, Blocks.AIR.defaultBlockState(), 18);
         cell = cell.above();
      }

   }

   public static void topple(ServerLevel level, ServerPlayer owner, List<BlockPos> cut, List<BlockPos> stems, boolean woody) {
      Set<BlockPos> seen = new HashSet();
      List<BlockPos> felled = new ArrayList();
      List<BlockPos> bases = new ArrayList();

      for(BlockPos c : cut) {
         for(Direction d : Direction.values()) {
            BlockPos s = c.relative(d);
            if (!seen.contains(s) && level.isLoaded(s) && trunk(level.getBlockState(s))) {
               List<BlockPos> tree = unheld(level, owner, s, seen);
               if (tree != null) {
                  felled.addAll(tree);
                  bases.add((BlockPos)tree.get(0));
               }
            }
         }
      }

      if (!felled.isEmpty() || woody) {
         Set<BlockPos> wood = new HashSet(felled);
         wood.addAll(stems);
         Map<BlockPos, Integer> crown = new HashMap();
         ArrayDeque<BlockPos> wave = new ArrayDeque();
         List<BlockPos> roots = new ArrayList(felled);
         roots.addAll(cut);

         for(BlockPos p_0 : roots) {
            boolean stem_0 = wood.contains(p_0);

            for(int dx = -1; dx <= 1; ++dx) {
               for(int dy = -1; dy <= 1; ++dy) {
                  for(int dz = -1; dz <= 1; ++dz) {
                     BlockPos q_0 = p_0.offset(dx, dy, dz);
                     if (!crown.containsKey(q_0) && level.isLoaded(q_0)) {
                        BlockState qs_0 = level.getBlockState(q_0);
                        if (qs_0.is(BlockTags.LEAVES) && face(dx, dy, dz) || stem_0 && cap(qs_0)) {
                           crown.put(q_0, 1);
                           wave.add(q_0);
                        }
                     }
                  }
               }
            }
         }

         while(!wave.isEmpty() && crown.size() <= 4096) {
            BlockPos p = (BlockPos)wave.poll();
            int depth_0 = crown.get(p);
            if (depth_0 < 8) {
               boolean leaf_0 = level.getBlockState(p).is(BlockTags.LEAVES);

               for(int dx = -1; dx <= 1; ++dx) {
                  for(int dy = -1; dy <= 1; ++dy) {
                     for(int dz = -1; dz <= 1; ++dz) {
                        BlockPos q = p.offset(dx, dy, dz);
                        if (!crown.containsKey(q) && level.isLoaded(q)) {
                           BlockState qs = level.getBlockState(q);
                           if (leaf_0) {
                              if (!qs.is(BlockTags.LEAVES) || !face(dx, dy, dz)) {
                                 continue;
                              }
                           } else if (!cap(qs)) {
                              continue;
                           }

                           crown.put(q, depth_0 + 1);
                           wave.add(q);
                        }
                     }
                  }
               }
            }
         }

         boolean whole = wave.isEmpty();
         int n = 0;

         for(BlockPos p : felled) {
            BlockState st_0 = level.getBlockState(p);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            if (n++ % 3 == 0) {
               level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st_0), (double)p.getX() + 0.5D, (double)p.getY() + 0.5D, (double)p.getZ() + 0.5D, 6, 0.3D, 0.3D, 0.3D, 0.05D);
            }
         }

         for(int i = 0; i < bases.size() && i < 4; ++i) {
            level.playSound((Player)null, (BlockPos)bases.get(i), CastFxEvents.sound("minecraft:block.wood.break"), SoundSource.BLOCKS, 1.2F, 0.6F);
         }

         if (whole && !crown.isEmpty()) {
            Map<BlockPos, Integer> reach = new HashMap();
            ArrayDeque<BlockPos> out = new ArrayDeque();
            Set<BlockPos> trunks = new HashSet();

            for(BlockPos p : crown.keySet()) {
               reach.put(p, 0);
               out.add(p);
            }

            while(!out.isEmpty() && reach.size() <= 16384) {
               BlockPos p = (BlockPos)out.poll();
               int depth = reach.get(p);
               boolean leaf = level.getBlockState(p).is(BlockTags.LEAVES);

               for(int dx = -1; dx <= 1; ++dx) {
                  for(int dy = -1; dy <= 1; ++dy) {
                     for(int dz = -1; dz <= 1; ++dz) {
                        if (!leaf || face(dx, dy, dz)) {
                           BlockPos q = p.offset(dx, dy, dz);
                           if (!level.isLoaded(q)) {
                              return;
                           }

                           BlockState qs = level.getBlockState(q);
                           if (trunk(qs)) {
                              trunks.add(q);
                           } else if (depth < 6 && !reach.containsKey(q)) {
                              if (leaf) {
                                 if (!qs.is(BlockTags.LEAVES)) {
                                    continue;
                                 }
                              } else if (!cap(qs)) {
                                 continue;
                              }

                              reach.put(q, depth + 1);
                              out.add(q);
                           }
                        }
                     }
                  }
               }
            }

            if (out.isEmpty()) {
               Map<BlockPos, Integer> held = new HashMap();
               ArrayDeque<BlockPos> from = new ArrayDeque(trunks);

               for(BlockPos p : trunks) {
                  held.put(p, 0);
               }

               while(!from.isEmpty()) {
                  BlockPos p = (BlockPos)from.poll();
                  int depth = held.get(p);
                  if (depth < 6) {
                     BlockState ps = level.getBlockState(p);
                     boolean leaf = ps.is(BlockTags.LEAVES);
                     boolean stem = trunk(ps);

                     for(int dx = -1; dx <= 1; ++dx) {
                        for(int dy = -1; dy <= 1; ++dy) {
                           for(int dz = -1; dz <= 1; ++dz) {
                              BlockPos q = p.offset(dx, dy, dz);
                              if (reach.containsKey(q) && !held.containsKey(q)) {
                                 boolean to = level.getBlockState(q).is(BlockTags.LEAVES);
                                 if (to) {
                                    if (!leaf && !stem || !face(dx, dy, dz)) {
                                       continue;
                                    }
                                 } else if (leaf) {
                                    continue;
                                 }

                                 held.put(q, depth + 1);
                                 from.add(q);
                              }
                           }
                        }
                     }
                  }
               }

               for(BlockPos p : crown.keySet()) {
                  BlockState st = level.getBlockState(p);
                  if (!held.containsKey(p) && grown(st) && level.mayInteract(owner, p) && !holdsSign(level, p)) {
                     level.setBlock(p, st.getFluidState().createLegacyBlock(), 3);
                     if (n++ % 6 == 0) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, st), (double)p.getX() + 0.5D, (double)p.getY() + 0.5D, (double)p.getZ() + 0.5D, 4, 0.4D, 0.4D, 0.4D, 0.05D);
                     }
                  }
               }

            }
         }
      }
   }

   public static boolean refill(MinecraftServer server, String book, boolean force) {
      VoidLedger ledger = VoidLedger.get(server);
      List<VoidLedger.Mark> marks = ledger.take(book);
      marks.sort(Comparator.comparingInt((m) -> m.y));
      boolean left = false;
      Map<ServerLevel, double[]> filled = new HashMap();
      List<Object[]> placed = new ArrayList();
      Set<String> stalled = new HashSet();

      for(VoidLedger.Mark m : marks) {
         try {
            ServerLevel level_0 = null;

            for(ServerLevel l : server.getAllLevels()) {
               if (l.dimension().location().toString().equals(m.dim)) {
                  level_0 = l;
                  break;
               }
            }

            if (level_0 != null) {
               BlockPos pos_0 = new BlockPos(m.x, m.y, m.z);
               net.minecraft.world.level.block.Block was = (net.minecraft.world.level.block.Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(m.was));
               if (was != null && was != Blocks.AIR) {
                  String col = m.dim + " " + m.x + " " + m.z;
                  if (force || !stalled.contains(col) && level_0.isLoaded(pos_0) && level_0.getEntitiesOfClass(LivingEntity.class, new AABB(pos_0), TimeWeft::everpresent).isEmpty()) {
                     if (level_0.getBlockState(pos_0).isAir()) {
                        level_0.setBlock(pos_0, m.stateOf(level_0, was), 18);
                        placed.add(new Object[]{level_0, pos_0});
                        double[] b = (double[])filled.computeIfAbsent(level_0, (k) -> new double[]{(double)pos_0.getX(), (double)pos_0.getY(), (double)pos_0.getZ(), (double)pos_0.getX() + 1.0D, (double)pos_0.getY() + 1.0D, (double)pos_0.getZ() + 1.0D});
                        b[0] = Math.min(b[0], (double)pos_0.getX());
                        b[1] = Math.min(b[1], (double)pos_0.getY());
                        b[2] = Math.min(b[2], (double)pos_0.getZ());
                        b[3] = Math.max(b[3], (double)pos_0.getX() + 1.0D);
                        b[4] = Math.max(b[4], (double)pos_0.getY() + 1.0D);
                        b[5] = Math.max(b[5], (double)pos_0.getZ() + 1.0D);
                     }
                  } else {
                     ledger.mark(book, level_0, pos_0, m.stateOf(level_0, was));
                     left = true;
                     stalled.add(col);
                  }
               }
            }
         } catch (RuntimeException var17) {
         }
      }

      for(Object[] pl : placed) {
         try {
            ServerLevel level = (ServerLevel)pl[0];
            BlockPos pos = (BlockPos)pl[1];
            BlockState st = level.getBlockState(pos);
            BlockState shaped = net.minecraft.world.level.block.Block.updateFromNeighbourShapes(st, level, pos);
            if (shaped != st && !shaped.isAir()) {
               level.setBlock(pos, shaped, 18);
            }
         } catch (RuntimeException var16) {
         }
      }

      for(Map.Entry<ServerLevel, double[]> en : filled.entrySet()) {
         double[] b = (double[])en.getValue();
         AABB box = new AABB(b[0] - 1.0D, b[1] - 1.0D, b[2] - 1.0D, b[3] + 1.0D, b[4] + 2.0D, b[5] + 1.0D);

         for(LivingEntity e : ((ServerLevel)en.getKey()).getEntitiesOfClass(LivingEntity.class, box)) {
            if (!TimeWeft.everpresent(e)) {
               unbury(e);
            }
         }
      }

      return left;
   }

   public static void unbury(LivingEntity e) {
      AABB low = new AABB(e.getX() - 0.3D, e.getY(), e.getZ() - 0.3D, e.getX() + 0.3D, e.getY() + 0.6D, e.getZ() + 0.3D);
      if (!e.level().noCollision(e, low)) {
         for(int up = 1; up <= 8; ++up) {
            if (e.level().noCollision(e, e.getBoundingBox().move(0.0D, (double)up, 0.0D))) {
               e.teleportTo(e.getX(), e.getY() + (double)up, e.getZ());
               return;
            }
         }

      }
   }

   public static void tickRefills(MinecraftServer server) {
      long now = (long)server.getTickCount();
      List<String> due = new ArrayList();

      for(Map.Entry<String, long[]> en : REFILLS.entrySet()) {
         if (((long[])en.getValue())[0] <= now) {
            due.add((String)en.getKey());
         }
      }

      due.sort(Comparator.comparingInt((b) -> b.contains("_fell_") ? 1 : 0));

      for(String book : due) {
         long[] d = (long[])REFILLS.remove(book);
         if (refill(server, book, false) && d[1] < 90L) {
            REFILLS.put(book, new long[]{now + 40L, d[1] + 1L});
         }
      }

   }

   public static boolean realCraters(ServerLevel level) {
      return level.getGameRules().getBoolean(UnrpGameRules.REAL_CRATERS) && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
   }

   public static int sunCrater(ServerLevel level, Vec3 at, double radius, ServerPlayer owner) {
      return crater(level, at, radius, owner);
   }

   public static int sunFell(ServerLevel level, ServerPlayer owner, List<BlockPos> spots) {
      return owner != null && realCraters(level) ? carve(level, owner, "ndcrater_" + String.valueOf(owner.getUUID()) + "_" + level.getGameTime() + "_fell_" + books++, spots, 400, true) : 0;
   }

   public static int sunTear(ServerLevel level, ServerPlayer owner, List<BlockPos> spots) {
      return owner != null && realCraters(level) ? carve(level, owner, "ndcrater_" + String.valueOf(owner.getUUID()) + "_" + level.getGameTime() + "_tear_" + books++, spots, 400, false) : 0;
   }

   public static int sunTrench(ServerLevel level, ServerPlayer owner, BlockPos spot) {
      if (owner != null && realCraters(level)) {
         List<BlockPos> one = new ArrayList();
         one.add(spot);
         return carve(level, owner, "ndcrater_" + String.valueOf(owner.getUUID()) + "_trench_" + spot.getX() + "_" + spot.getZ(), one, 400, false);
      } else {
         return 0;
      }
   }

   public static boolean sunReal(ServerLevel level) {
      return realCraters(level);
   }

   public static boolean persistent(BlockState st) {
      return st.hasProperty(BlockStateProperties.PERSISTENT) && st.getValue(BlockStateProperties.PERSISTENT);
   }

   public static boolean wild(BlockState st) {
      if (st.is(BlockTags.LEAVES)) {
         return false;
      } else {
         return st.getBlock() instanceof BushBlock || st.is(BlockTags.FLOWERS) || st.is(BlockTags.SAPLINGS) || st.is(Blocks.GRASS) || st.is(Blocks.FERN) || st.is(Blocks.DEAD_BUSH) || st.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && st.canBeReplaced();
      }
   }

   public static boolean earthy(ServerLevel level, BlockPos pos, BlockState st) {
      if (!st.isAir() && st.getFluidState().isEmpty() && level.getBlockEntity(pos) == null) {
         float hard = st.getDestroySpeed(level, pos);
         if (!(hard < 0.0F) && !(hard > 3.0F)) {
            return st.is(BlockTags.DIRT) || st.is(BlockTags.SAND) || st.is(BlockTags.BASE_STONE_OVERWORLD) || st.is(BlockTags.BASE_STONE_NETHER) || st.is(Blocks.GRAVEL) || st.is(Blocks.CLAY) || st.is(Blocks.SNOW_BLOCK);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean bare(ServerLevel level, BlockPos up) {
      BlockState us = level.getBlockState(up);
      if (us.is(Blocks.POWDER_SNOW)) {
         return false;
      } else if (cap(us)) {
         return false;
      } else {
         return us.getCollisionShape(level, up).isEmpty() || trunk(us) || grown(us) || growth(level, up, us) || us.is(net.minecraftforge.common.Tags.Blocks.ORES) || earthy(level, up, us);
      }
   }

   public static boolean cap(BlockState st) {
      return canopy(st) && !st.is(BlockTags.LEAVES);
   }

   public static boolean face(int dx, int dy, int dz) {
      return Math.abs(dx) + Math.abs(dy) + Math.abs(dz) == 1;
   }

   public static double grown(int poise, int from, int to) {
      return Math.max(0.0D, Math.min(1.0D, ((double)poise - (double)from) / ((double)to - (double)from)));
   }

   public static boolean grown(BlockState st) {
      return canopy(st) && !persistent(st);
   }

   public static boolean stranded(ServerLevel level, BlockPos q) {
      BlockState st = level.getBlockState(q);
      boolean kind = wild(st) || st.is(Blocks.BAMBOO) || st.is(Blocks.SUGAR_CANE) || st.is(Blocks.CACTUS) || st.is(Blocks.SNOW) || st.is(Blocks.TORCH) || st.is(Blocks.SOUL_TORCH);
      return kind && st.getFluidState().isEmpty() && level.getBlockEntity(q) == null && !st.canSurvive(level, q);
   }

   public static List<BlockPos> unheld(ServerLevel level, ServerPlayer owner, BlockPos start, Set<BlockPos> seen) {
      List<BlockPos> logs = new ArrayList();
      ArrayDeque<BlockPos> open = new ArrayDeque();
      seen.add(start);
      open.add(start);
      boolean leafy = false;
      boolean held = false;

      while(!open.isEmpty() && logs.size() <= 1024) {
         BlockPos p_0 = (BlockPos)open.poll();
         logs.add(p_0);
         BlockPos under = p_0.below();
         if (!level.isLoaded(under)) {
            held = true;
         } else {
            BlockState us = level.getBlockState(under);
            if (!trunk(us) && !canopy(us) && !us.getCollisionShape(level, under).isEmpty()) {
               held = true;
            }
         }

         for(Direction d : Direction.values()) {
            BlockPos q_0 = p_0.relative(d);
            if (level.isLoaded(q_0)) {
               if (grown(level.getBlockState(q_0))) {
                  leafy = true;
               }
            } else {
               held = true;
            }
         }

         for(int dx = -1; dx <= 1; ++dx) {
            for(int dy = -1; dy <= 1; ++dy) {
               for(int dz = -1; dz <= 1; ++dz) {
                  BlockPos q = p_0.offset(dx, dy, dz);
                  if (!seen.contains(q) && level.isLoaded(q) && trunk(level.getBlockState(q))) {
                     seen.add(q);
                     open.add(q);
                  }
               }
            }
         }
      }

      if (!held && leafy && open.isEmpty()) {
         int x0 = Integer.MAX_VALUE;
         int y0 = Integer.MAX_VALUE;
         int z0 = Integer.MAX_VALUE;
         int x1 = Integer.MIN_VALUE;
         int y1 = Integer.MIN_VALUE;
         int z1 = Integer.MIN_VALUE;

         for(BlockPos p : logs) {
            if (!level.mayInteract(owner, p) || holdsSign(level, p)) {
               return null;
            }

            x0 = Math.min(x0, p.getX());
            y0 = Math.min(y0, p.getY());
            z0 = Math.min(z0, p.getZ());
            x1 = Math.max(x1, p.getX());
            y1 = Math.max(y1, p.getY());
            z1 = Math.max(z1, p.getZ());
         }

         AABB box = new AABB((double)(x0 - 6), (double)y0 - 1.0D, (double)(z0 - 6), (double)x1 + 1.0D + 6.0D, (double)y1 + 2.0D + 6.0D, (double)z1 + 1.0D + 6.0D);

         for(LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (e != owner && (!OriginStrike.fair(owner, e) || TimeWeft.everpresent(e))) {
               AABB eb = e.getBoundingBox();

               for(double fx : new double[]{eb.minX, eb.maxX}) {
                  for(double fz : new double[]{eb.minZ, eb.maxZ}) {
                     BlockState below = level.getBlockState(BlockPos.containing(fx, eb.minY - 0.1D, fz));
                     if (trunk(below) || canopy(below)) {
                        return null;
                     }
                  }
               }
            }
         }

         logs.sort((c1, c2) -> Integer.compare(c1.getY(), c2.getY()));
         return logs;
      } else {
         return null;
      }
   }

   public static double landfallLift(double age) {
      if (age <= 0.0D) {
         return 0.0D;
      } else if (age < 40.0D) {
         double t = age / 40.0D;
         return 30.0D * t * t * (3.0D - 2.0D * t);
      } else if (age < 60.0D) {
         return 30.0D;
      } else {
         double d = age - 40.0D - 20.0D;
         return Math.max(0.0D, 30.0D - 0.24D * d * d);
      }
   }
}
