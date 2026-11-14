package com.myriam.unrp.uo;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

public class VoidLedger extends SavedData {
   private final Map<String, List<VoidLedger.Mark>> books = new HashMap();

   public static VoidLedger get(MinecraftServer server) {
      return (VoidLedger)server.overworld().getDataStorage().computeIfAbsent(VoidLedger::load, VoidLedger::new, "unrp_void_ledger");
   }

   public void mark(String book, ServerLevel level, BlockPos pos, BlockState was) {
      ResourceLocation id = ForgeRegistries.BLOCKS.getKey(was.getBlock());
      if (id != null) {
         ((List)this.books.computeIfAbsent(book, (k) -> new ArrayList())).add(new VoidLedger.Mark(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), id.toString(), was == was.getBlock().defaultBlockState() ? null : NbtUtils.writeBlockState(was)));
         this.setDirty();
      }
   }

   public int size(String book) {
      List<VoidLedger.Mark> list = (List)this.books.get(book);
      return list == null ? 0 : list.size();
   }

   public List<VoidLedger.Mark> take(String book) {
      List<VoidLedger.Mark> list = (List)this.books.remove(book);
      if (list != null) {
         this.setDirty();
      }

      return (List<VoidLedger.Mark>)(list == null ? new ArrayList() : list);
   }

   public List<VoidLedger.Mark> peek(String book) {
      List<VoidLedger.Mark> list = (List)this.books.get(book);
      return list == null ? new ArrayList() : new ArrayList(list);
   }

   public List<String> books() {
      return new ArrayList<>(this.books.keySet());
   }

   public int shed() {
      int gone = 0;
      Iterator<Map.Entry<String, List<VoidLedger.Mark>>> it = this.books.entrySet().iterator();

      while(it.hasNext()) {
         List<VoidLedger.Mark> marks = (List)((Map.Entry)it.next()).getValue();
         int had = marks.size();
         marks.removeIf((left) -> WorldRule.old(left.dim));
         gone += had - marks.size();
         if (marks.isEmpty()) {
            it.remove();
         }
      }

      if (gone > 0) {
         this.setDirty();
      }

      return gone;
   }

   public static VoidLedger load(CompoundTag tag) {
      VoidLedger data = new VoidLedger();
      ListTag list = tag.getList("Books", 10);

      for(int i = 0; i < list.size(); ++i) {
         CompoundTag bt = list.getCompound(i);
         String key = bt.getString("Key");
         List<VoidLedger.Mark> marks = new ArrayList();
         ListTag mt = bt.getList("Marks", 10);

         for(int j = 0; j < mt.size(); ++j) {
            CompoundTag m = mt.getCompound(j);
            marks.add(new VoidLedger.Mark(m.getString("Dim"), m.getInt("X"), m.getInt("Y"), m.getInt("Z"), m.getString("Was"), m.contains("State", 10) ? m.getCompound("State") : null));
         }

         data.books.put(key, marks);
      }

      return data;
   }

   public CompoundTag save(CompoundTag tag) {
      ListTag list = new ListTag();

      for(Map.Entry<String, List<VoidLedger.Mark>> e : this.books.entrySet()) {
         CompoundTag bt = new CompoundTag();
         bt.putString("Key", (String)e.getKey());
         ListTag mt = new ListTag();

         for(VoidLedger.Mark m : (List<VoidLedger.Mark>)e.getValue()) {
            CompoundTag one = new CompoundTag();
            one.putString("Dim", m.dim);
            one.putInt("X", m.x);
            one.putInt("Y", m.y);
            one.putInt("Z", m.z);
            one.putString("Was", m.was);
            if (m.state != null) {
               one.put("State", m.state);
            }

            mt.add(one);
         }

         bt.put("Marks", mt);
         list.add(bt);
      }

      tag.put("Books", list);
      return tag;
   }

   public static int restore(MinecraftServer server, String book, Block corrupted) {
      VoidLedger data = get(server);
      List<VoidLedger.Mark> marks = data.take(book);
      int put = 0;

      for(VoidLedger.Mark m : marks) {
         ServerLevel level = null;

         for(ServerLevel candidate : server.getAllLevels()) {
            if (candidate.dimension().location().toString().equals(m.dim)) {
               level = candidate;
               break;
            }
         }

         if (level != null) {
            BlockPos pos = new BlockPos(m.x, m.y, m.z);
            if (level.getBlockState(pos).getBlock() == corrupted) {
               Block was = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(m.was));
               if (was != null) {
                  level.setBlock(pos, m.stateOf(level, was), 3);
                  ++put;
               }
            }
         }
      }

      return put;
   }

   public static class Mark {
      public String dim;
      public int x;
      public int y;
      public int z;
      public String was;
      public CompoundTag state;

      public Mark(String dim, int x, int y, int z, String was) {
         this(dim, x, y, z, was, (CompoundTag)null);
      }

      public Mark(String dim, int x, int y, int z, String was, CompoundTag state) {
         this.dim = dim;
         this.x = x;
         this.y = y;
         this.z = z;
         this.was = was;
         this.state = state;
      }

      public BlockState stateOf(ServerLevel level, Block block) {
         if (this.state != null) {
            try {
               BlockState st = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), this.state);
               if (st.getBlock() == block) {
                  return st;
               }
            } catch (RuntimeException var4) {
            }
         }

         return block.defaultBlockState();
      }
   }
}
