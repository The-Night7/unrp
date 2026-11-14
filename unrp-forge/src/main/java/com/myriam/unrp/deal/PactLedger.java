package com.myriam.unrp.deal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class PactLedger extends SavedData {
   private final Map<UUID, PactLedger.Pact> pacts = new HashMap();
   private final Map<UUID, UUID> soulOwner = new HashMap();
   private final Map<UUID, Integer> soulSerial = new HashMap();
   private final Map<UUID, UUID[]> binds = new HashMap();
   private final Map<UUID, String[]> bindNames = new HashMap();
   private final Map<UUID, Long> reviveCd = new HashMap();
   private final Map<UUID, Long> greaterCd = new HashMap();
   private final Map<UUID, Integer> plots = new HashMap();
   private final Map<String, UUID> portals = new HashMap();
   private final Map<String, UUID> lanterns = new HashMap();
   private final Map<UUID, String> titles = new HashMap();
   private final Map<UUID, String> pendingAlias = new HashMap();
   private final Map<UUID, CompoundTag> swordTags = new HashMap();
   private final Map<UUID, String> returnPos = new HashMap();
   private final Map<UUID, ListTag> pendingItems = new HashMap();
   private final Map<UUID, String> locations = new HashMap();
   private final Map<UUID, Integer> pendingLevels = new HashMap();
   private final Map<UUID, String> pendingPowers = new HashMap();
   private final Map<UUID, CompoundTag> boundItems = new HashMap();
   private int nextPlot;

   public static PactLedger get(MinecraftServer server) {
      return (PactLedger)server.overworld().getDataStorage().computeIfAbsent(PactLedger::load, PactLedger::new, "unrp_pacts");
   }

   public PactLedger.Pact create(UUID owner, String ownerName) {
      PactLedger.Pact p = new PactLedger.Pact();
      p.id = UUID.randomUUID();
      p.owner = owner;
      p.ownerName = ownerName;
      this.pacts.put(p.id, p);
      this.setDirty();
      return p;
   }

   public PactLedger.Pact pact(UUID id) {
      return id == null ? null : (PactLedger.Pact)this.pacts.get(id);
   }

   public void remove(UUID id) {
      if (this.pacts.remove(id) != null) {
         this.setDirty();
      }

   }

   public List<PactLedger.Pact> signedPactsTouching(UUID player) {
      List<PactLedger.Pact> out = new ArrayList();

      for(PactLedger.Pact p : this.pacts.values()) {
         if (p.signed && (player.equals(p.owner) || player.equals(p.signer))) {
            out.add(p);
         }
      }

      out.sort((a, b) -> Long.compare(a.signTime, b.signTime));
      return out;
   }

   public List<PactLedger.Pact> pactsOwnedBy(UUID owner) {
      List<PactLedger.Pact> out = new ArrayList();

      for(PactLedger.Pact p : this.pacts.values()) {
         if (owner.equals(p.owner)) {
            out.add(p);
         }
      }

      out.sort((a, b) -> {
         int bySigned = Boolean.compare(b.signed, a.signed);
         if (bySigned != 0) {
            return bySigned;
         } else {
            int byTime = Long.compare(a.signTime, b.signTime);
            return byTime != 0 ? byTime : a.id.compareTo(b.id);
         }
      });
      return out;
   }

   public void resetRitualsBy(UUID player) {
      boolean changed = false;

      for(PactLedger.Pact p : this.pacts.values()) {
         if (player.equals(p.ritualBy) && p.ritual > 0) {
            p.ritual = 0;
            p.ritualBy = null;
            changed = true;
         }
      }

      if (changed) {
         this.setDirty();
      }

   }

   public boolean ritualRunning(UUID player) {
      for(PactLedger.Pact p : this.pacts.values()) {
         if (player.equals(p.ritualBy) && p.ritual > 0) {
            return true;
         }
      }

      return false;
   }

   public String locationOf(UUID pactId) {
      return (String)this.locations.getOrDefault(pactId, "");
   }

   public void setLocation(UUID pactId, String where) {
      String old = (String)this.locations.get(pactId);
      if (where != null && !where.isEmpty()) {
         if (!where.equals(old)) {
            this.locations.put(pactId, where);
            this.setDirty();
         }

      } else {
         if (this.locations.remove(pactId) != null) {
            this.setDirty();
         }

      }
   }

   public CompoundTag boundItemOf(UUID target) {
      return (CompoundTag)this.boundItems.get(target);
   }

   public void setBoundItem(UUID target, CompoundTag item) {
      if (item == null) {
         this.boundItems.remove(target);
      } else {
         this.boundItems.put(target, item);
      }

      this.setDirty();
   }

   public int takePendingLevels(UUID player) {
      Integer n = (Integer)this.pendingLevels.remove(player);
      if (n != null) {
         this.setDirty();
         return n;
      } else {
         return 0;
      }
   }

   public void addPendingLevels(UUID player, int levels) {
      this.pendingLevels.merge(player, levels, Integer::sum);
      this.setDirty();
   }

   public String takePendingPowers(UUID player) {
      String s = (String)this.pendingPowers.remove(player);
      if (s != null) {
         this.setDirty();
      }

      return s;
   }

   public void addPendingPower(UUID player, String power) {
      this.pendingPowers.merge(player, power, (a, b) -> a + "," + b);
      this.setDirty();
   }

   public UUID ownerOfPlot(int index) {
      for(Map.Entry<UUID, Integer> e : this.plots.entrySet()) {
         if (e.getValue() == index) {
            return (UUID)e.getKey();
         }
      }

      return null;
   }

   public List<PactLedger.Pact> allSigned() {
      List<PactLedger.Pact> out = new ArrayList();

      for(PactLedger.Pact p : this.pacts.values()) {
         if (p.signed) {
            out.add(p);
         }
      }

      out.sort((a, b) -> Long.compare(a.signTime, b.signTime));
      return out;
   }

   public boolean soulPactExists(UUID target) {
      for(PactLedger.Pact p : this.pacts.values()) {
         if (p.soul && p.signed && target.equals(p.signer)) {
            return true;
         }
      }

      return false;
   }

   public UUID soulOwnerOf(UUID target) {
      return (UUID)this.soulOwner.get(target);
   }

   public void setSoulOwner(UUID target, UUID dealmaker) {
      if (dealmaker == null) {
         this.soulOwner.remove(target);
      } else {
         this.soulOwner.put(target, dealmaker);
      }

      this.setDirty();
   }

   public List<UUID> soulsOwnedBy(UUID dealmaker) {
      List<UUID> out = new ArrayList();

      for(Map.Entry<UUID, UUID> e : this.soulOwner.entrySet()) {
         if (dealmaker.equals(e.getValue())) {
            out.add((UUID)e.getKey());
         }
      }

      out.sort((a, b) -> a.compareTo(b));
      return out;
   }

   public int serialOf(UUID target) {
      return this.soulSerial.getOrDefault(target, 0);
   }

   public int bumpSerial(UUID target) {
      int s = this.serialOf(target) + 1;
      this.soulSerial.put(target, s);
      this.setDirty();
      return s;
   }

   public UUID[] bindsOf(UUID dealmaker) {
      return (UUID[])this.binds.computeIfAbsent(dealmaker, (k) -> new UUID[3]);
   }

   public String[] bindNamesOf(UUID dealmaker) {
      return (String[])this.bindNames.computeIfAbsent(dealmaker, (k) -> new String[]{"", "", ""});
   }

   public void setBind(UUID dealmaker, int rank, UUID ally, String name) {
      UUID[] arr = this.bindsOf(dealmaker);
      String[] names = this.bindNamesOf(dealmaker);
      arr[rank] = ally;
      names[rank] = name == null ? "" : name;
      this.setDirty();
   }

   public int rankOf(UUID dealmaker, UUID ally) {
      UUID[] arr = this.bindsOf(dealmaker);

      for(int i = 0; i < 3; ++i) {
         if (ally.equals(arr[i])) {
            return i;
         }
      }

      return -1;
   }

   public UUID dealmakerOf(UUID ally) {
      for(Map.Entry<UUID, UUID[]> e : this.binds.entrySet()) {
         for(UUID u : (UUID[])e.getValue()) {
            if (ally.equals(u)) {
               return (UUID)e.getKey();
            }
         }
      }

      return null;
   }

   public long reviveCdOf(UUID ally) {
      return this.reviveCd.getOrDefault(ally, 0L);
   }

   public void setReviveCd(UUID ally, long until) {
      this.reviveCd.put(ally, until);
      this.setDirty();
   }

   public long greaterCdOf(UUID ally) {
      return this.greaterCd.getOrDefault(ally, 0L);
   }

   public void setGreaterCd(UUID ally, long until) {
      this.greaterCd.put(ally, until);
      this.setDirty();
   }

   public int plotOf(UUID player) {
      Integer plot = (Integer)this.plots.get(player);
      if (plot != null) {
         return plot;
      } else {
         int assigned = ++this.nextPlot;
         this.plots.put(player, assigned);
         this.setDirty();
         return assigned;
      }
   }

   public void linkPortal(String key, UUID target) {
      this.portals.put(key, target);
      this.setDirty();
   }

   public UUID portalTarget(String key) {
      return (UUID)this.portals.get(key);
   }

   public void setLantern(String key, UUID target) {
      if (target == null) {
         this.lanterns.remove(key);
      } else {
         this.lanterns.put(key, target);
      }

      this.setDirty();
   }

   public UUID lanternTarget(String key) {
      return (UUID)this.lanterns.get(key);
   }

   public List<UUID> lanternLit() {
      return new ArrayList(this.lanterns.values());
   }

   public String titleOf(UUID player) {
      return (String)this.titles.getOrDefault(player, "");
   }

   public void setTitle(UUID player, String title) {
      if (title != null && !title.isEmpty()) {
         this.titles.put(player, title);
      } else {
         this.titles.remove(player);
      }

      this.setDirty();
   }

   public String takePendingAlias(UUID player) {
      String alias = (String)this.pendingAlias.remove(player);
      if (alias != null) {
         this.setDirty();
      }

      return alias;
   }

   public void setPendingAlias(UUID player, String alias) {
      this.pendingAlias.put(player, alias);
      this.setDirty();
   }

   public CompoundTag swordTag(UUID ally) {
      return (CompoundTag)this.swordTags.get(ally);
   }

   public void setSwordTag(UUID ally, CompoundTag tag) {
      if (tag == null) {
         this.swordTags.remove(ally);
      } else {
         this.swordTags.put(ally, tag);
      }

      this.setDirty();
   }

   public String takeReturnPos(UUID player) {
      String pos = (String)this.returnPos.remove(player);
      if (pos != null) {
         this.setDirty();
      }

      return pos;
   }

   public String peekReturnPos(UUID player) {
      return (String)this.returnPos.get(player);
   }

   public void setReturnPos(UUID player, String pos) {
      this.returnPos.put(player, pos);
      this.setDirty();
   }

   public ListTag takePendingItems(UUID player) {
      ListTag list = (ListTag)this.pendingItems.remove(player);
      if (list != null) {
         this.setDirty();
      }

      return list;
   }

   public void setPendingItems(UUID player, ListTag list) {
      ListTag have = (ListTag)this.pendingItems.get(player);
      if (have == null) {
         this.pendingItems.put(player, list);
      } else {
         for(int i = 0; i < list.size(); ++i) {
            have.add(list.get(i));
         }
      }

      this.setDirty();
   }

   public int shed() {
      int gone = 0;
      int had = this.portals.size() + this.lanterns.size() + this.returnPos.size();
      this.portals.keySet().removeIf((key) -> DealBridge.oldDim(DealBridge.dimOf(key)));
      this.lanterns.keySet().removeIf((key) -> DealBridge.oldDim(DealBridge.dimOf(key)));
      this.returnPos.values().removeIf((where) -> DealBridge.oldDim(DealBridge.dimOf(where)));
      gone += had - this.portals.size() - this.lanterns.size() - this.returnPos.size();

      for(PactLedger.Pact p : this.pacts.values()) {
         for(PactLedger.Term t : p.terms) {
            if ("prohibit_area".equals(t.key) && DealBridge.oldDim(t.a)) {
               t.a = "unrp:old_world";
               ++gone;
            }
         }
      }

      if (gone > 0) {
         this.setDirty();
      }

      return gone;
   }

   public long latest() {
      long t = 0L;

      for(PactLedger.Pact p : this.pacts.values()) {
         if (p.signed) {
            t = Math.max(t, p.signTime);
         }
      }

      return t;
   }

   public static PactLedger load(CompoundTag tag) {
      PactLedger data = new PactLedger();
      ListTag list = tag.getList("Pacts", 10);

      for(int i = 0; i < list.size(); ++i) {
         PactLedger.Pact p = PactLedger.Pact.load(list.getCompound(i));
         data.pacts.put(p.id, p);
      }

      readUuidMap(tag, "SoulOwner", data.soulOwner);
      ListTag serials = tag.getList("SoulSerial", 10);

      for(int i = 0; i < serials.size(); ++i) {
         CompoundTag t_0 = serials.getCompound(i);
         data.soulSerial.put(t_0.getUUID("K"), t_0.getInt("V"));
      }

      ListTag bindList = tag.getList("Binds", 10);

      for(int i = 0; i < bindList.size(); ++i) {
         CompoundTag t = bindList.getCompound(i);
         UUID[] arr = new UUID[3];
         String[] names = new String[]{"", "", ""};

         for(int r = 0; r < 3; ++r) {
            if (t.hasUUID("U" + r)) {
               arr[r] = t.getUUID("U" + r);
            }

            names[r] = t.getString("N" + r);
         }

         data.binds.put(t.getUUID("K"), arr);
         data.bindNames.put(t.getUUID("K"), names);
      }

      readLongMap(tag, "ReviveCd", data.reviveCd);
      readLongMap(tag, "GreaterCd", data.greaterCd);
      ListTag plotList = tag.getList("Plots", 10);

      for(int i = 0; i < plotList.size(); ++i) {
         CompoundTag t = plotList.getCompound(i);
         data.plots.put(t.getUUID("K"), t.getInt("V"));
      }

      data.nextPlot = tag.getInt("NextPlot");
      readStringKeyMap(tag, "Portals", data.portals);
      readStringKeyMap(tag, "Lanterns", data.lanterns);
      ListTag titleList = tag.getList("Titles", 10);

      for(int i = 0; i < titleList.size(); ++i) {
         CompoundTag t = titleList.getCompound(i);
         data.titles.put(t.getUUID("K"), t.getString("V"));
      }

      ListTag aliasList = tag.getList("PendingAlias", 10);

      for(int i = 0; i < aliasList.size(); ++i) {
         CompoundTag t = aliasList.getCompound(i);
         data.pendingAlias.put(t.getUUID("K"), t.getString("V"));
      }

      ListTag swordList = tag.getList("Swords", 10);

      for(int i = 0; i < swordList.size(); ++i) {
         CompoundTag t = swordList.getCompound(i);
         data.swordTags.put(t.getUUID("K"), t.getCompound("V"));
      }

      ListTag posList = tag.getList("ReturnPos", 10);

      for(int i = 0; i < posList.size(); ++i) {
         CompoundTag t = posList.getCompound(i);
         data.returnPos.put(t.getUUID("K"), t.getString("V"));
      }

      ListTag itemList = tag.getList("PendingItems", 10);

      for(int i = 0; i < itemList.size(); ++i) {
         CompoundTag t = itemList.getCompound(i);
         data.pendingItems.put(t.getUUID("K"), t.getList("V", 10));
      }

      ListTag locList = tag.getList("PactLoc", 10);

      for(int i = 0; i < locList.size(); ++i) {
         CompoundTag t = locList.getCompound(i);
         data.locations.put(t.getUUID("K"), t.getString("V"));
      }

      ListTag lvlList = tag.getList("PendingLevels", 10);

      for(int i = 0; i < lvlList.size(); ++i) {
         CompoundTag t = lvlList.getCompound(i);
         data.pendingLevels.put(t.getUUID("K"), t.getInt("V"));
      }

      ListTag powList = tag.getList("PendingPowers", 10);

      for(int i = 0; i < powList.size(); ++i) {
         CompoundTag t = powList.getCompound(i);
         data.pendingPowers.put(t.getUUID("K"), t.getString("V"));
      }

      ListTag boundList = tag.getList("BoundItems", 10);

      for(int i = 0; i < boundList.size(); ++i) {
         CompoundTag t = boundList.getCompound(i);
         data.boundItems.put(t.getUUID("K"), t.getCompound("V"));
      }

      return data;
   }

   private static void readUuidMap(CompoundTag tag, String name, Map<UUID, UUID> into) {
      ListTag list = tag.getList(name, 10);

      for(int i = 0; i < list.size(); ++i) {
         CompoundTag t = list.getCompound(i);
         into.put(t.getUUID("K"), t.getUUID("V"));
      }

   }

   private static void readLongMap(CompoundTag tag, String name, Map<UUID, Long> into) {
      ListTag list = tag.getList(name, 10);

      for(int i = 0; i < list.size(); ++i) {
         CompoundTag t = list.getCompound(i);
         into.put(t.getUUID("K"), t.getLong("V"));
      }

   }

   private static void readStringKeyMap(CompoundTag tag, String name, Map<String, UUID> into) {
      ListTag list = tag.getList(name, 10);

      for(int i = 0; i < list.size(); ++i) {
         CompoundTag t = list.getCompound(i);
         into.put(t.getString("K"), t.getUUID("V"));
      }

   }

   public CompoundTag save(CompoundTag tag) {
      ListTag list = new ListTag();

      for(PactLedger.Pact p : this.pacts.values()) {
         list.add(p.save());
      }

      tag.put("Pacts", list);
      writeUuidMap(tag, "SoulOwner", this.soulOwner);
      ListTag serials = new ListTag();

      for(Map.Entry<UUID, Integer> e_0 : this.soulSerial.entrySet()) {
         CompoundTag t_0 = new CompoundTag();
         t_0.putUUID("K", (UUID)e_0.getKey());
         t_0.putInt("V", e_0.getValue());
         serials.add(t_0);
      }

      tag.put("SoulSerial", serials);
      ListTag bindList = new ListTag();

      for(Map.Entry<UUID, UUID[]> e : this.binds.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         String[] names = (String[])this.bindNames.getOrDefault(e.getKey(), new String[]{"", "", ""});

         for(int r = 0; r < 3; ++r) {
            if (((UUID[])e.getValue())[r] != null) {
               t.putUUID("U" + r, ((UUID[])e.getValue())[r]);
            }

            t.putString("N" + r, names[r]);
         }

         bindList.add(t);
      }

      tag.put("Binds", bindList);
      writeLongMap(tag, "ReviveCd", this.reviveCd);
      writeLongMap(tag, "GreaterCd", this.greaterCd);
      ListTag plotList = new ListTag();

      for(Map.Entry<UUID, Integer> e : this.plots.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putInt("V", e.getValue());
         plotList.add(t);
      }

      tag.put("Plots", plotList);
      tag.putInt("NextPlot", this.nextPlot);
      writeStringKeyMap(tag, "Portals", this.portals);
      writeStringKeyMap(tag, "Lanterns", this.lanterns);
      ListTag titleList = new ListTag();

      for(Map.Entry<UUID, String> e : this.titles.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putString("V", (String)e.getValue());
         titleList.add(t);
      }

      tag.put("Titles", titleList);
      ListTag aliasList = new ListTag();

      for(Map.Entry<UUID, String> e : this.pendingAlias.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putString("V", (String)e.getValue());
         aliasList.add(t);
      }

      tag.put("PendingAlias", aliasList);
      ListTag swordList = new ListTag();

      for(Map.Entry<UUID, CompoundTag> e : this.swordTags.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.put("V", (Tag)e.getValue());
         swordList.add(t);
      }

      tag.put("Swords", swordList);
      ListTag posList = new ListTag();

      for(Map.Entry<UUID, String> e : this.returnPos.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putString("V", (String)e.getValue());
         posList.add(t);
      }

      tag.put("ReturnPos", posList);
      ListTag itemList = new ListTag();

      for(Map.Entry<UUID, ListTag> e : this.pendingItems.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.put("V", (Tag)e.getValue());
         itemList.add(t);
      }

      tag.put("PendingItems", itemList);
      ListTag locList = new ListTag();

      for(Map.Entry<UUID, String> e : this.locations.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putString("V", (String)e.getValue());
         locList.add(t);
      }

      tag.put("PactLoc", locList);
      ListTag lvlList = new ListTag();

      for(Map.Entry<UUID, Integer> e : this.pendingLevels.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putInt("V", e.getValue());
         lvlList.add(t);
      }

      tag.put("PendingLevels", lvlList);
      ListTag powList = new ListTag();

      for(Map.Entry<UUID, String> e : this.pendingPowers.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putString("V", (String)e.getValue());
         powList.add(t);
      }

      tag.put("PendingPowers", powList);
      ListTag boundList = new ListTag();

      for(Map.Entry<UUID, CompoundTag> e : this.boundItems.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.put("V", (Tag)e.getValue());
         boundList.add(t);
      }

      tag.put("BoundItems", boundList);
      return tag;
   }

   private static void writeUuidMap(CompoundTag tag, String name, Map<UUID, UUID> from) {
      ListTag list = new ListTag();

      for(Map.Entry<UUID, UUID> e : from.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putUUID("V", (UUID)e.getValue());
         list.add(t);
      }

      tag.put(name, list);
   }

   private static void writeLongMap(CompoundTag tag, String name, Map<UUID, Long> from) {
      ListTag list = new ListTag();

      for(Map.Entry<UUID, Long> e : from.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putUUID("K", (UUID)e.getKey());
         t.putLong("V", e.getValue());
         list.add(t);
      }

      tag.put(name, list);
   }

   private static void writeStringKeyMap(CompoundTag tag, String name, Map<String, UUID> from) {
      ListTag list = new ListTag();

      for(Map.Entry<String, UUID> e : from.entrySet()) {
         CompoundTag t = new CompoundTag();
         t.putString("K", (String)e.getKey());
         t.putUUID("V", (UUID)e.getValue());
         list.add(t);
      }

      tag.put(name, list);
   }

   public static class Pact {
      public UUID id;
      public UUID owner;
      public String ownerName = "";
      public UUID signer;
      public String signerName = "";
      public boolean signed;
      public long signTime;
      public boolean soul;
      public int ritual;
      public UUID ritualBy;
      public boolean proposed;
      public boolean submitted;
      public final List<PactLedger.Term> terms = new ArrayList();

      public CompoundTag save() {
         CompoundTag t = new CompoundTag();
         t.putUUID("Id", this.id);
         t.putUUID("Owner", this.owner);
         t.putString("OwnerName", this.ownerName);
         if (this.signer != null) {
            t.putUUID("Signer", this.signer);
         }

         t.putString("SignerName", this.signerName);
         t.putBoolean("Signed", this.signed);
         t.putLong("SignTime", this.signTime);
         t.putBoolean("Soul", this.soul);
         t.putInt("Ritual", this.ritual);
         if (this.ritualBy != null) {
            t.putUUID("RitualBy", this.ritualBy);
         }

         t.putBoolean("Proposed", this.proposed);
         t.putBoolean("Submitted", this.submitted);
         ListTag list = new ListTag();

         for(PactLedger.Term term : this.terms) {
            list.add(term.save());
         }

         t.put("Terms", list);
         return t;
      }

      public static PactLedger.Pact load(CompoundTag t) {
         PactLedger.Pact p = new PactLedger.Pact();
         p.id = t.getUUID("Id");
         p.owner = t.getUUID("Owner");
         p.ownerName = t.getString("OwnerName");
         if (t.hasUUID("Signer")) {
            p.signer = t.getUUID("Signer");
         }

         p.signerName = t.getString("SignerName");
         p.signed = t.getBoolean("Signed");
         p.signTime = t.getLong("SignTime");
         p.soul = t.getBoolean("Soul");
         p.ritual = t.getInt("Ritual");
         if (t.hasUUID("RitualBy")) {
            p.ritualBy = t.getUUID("RitualBy");
         }

         p.proposed = t.getBoolean("Proposed");
         p.submitted = t.getBoolean("Submitted");
         ListTag list = t.getList("Terms", 10);

         for(int i = 0; i < list.size(); ++i) {
            p.terms.add(PactLedger.Term.load(list.getCompound(i)));
         }

         return p;
      }

      public UUID partyOf(int side) {
         return side == 1 ? this.owner : this.signer;
      }
   }

   public static class Term {
      public String cat = "";
      public String key = "";
      public String a = "";
      public double v;
      public int x1;
      public int z1;
      public int x2;
      public int z2;
      public int side;
      public String text = "";
      public boolean locked;

      public CompoundTag save() {
         CompoundTag t = new CompoundTag();
         t.putString("Cat", this.cat);
         t.putString("Key", this.key);
         t.putString("A", this.a);
         t.putDouble("V", this.v);
         t.putInt("X1", this.x1);
         t.putInt("Z1", this.z1);
         t.putInt("X2", this.x2);
         t.putInt("Z2", this.z2);
         t.putInt("Side", this.side);
         t.putString("Text", this.text);
         t.putBoolean("Locked", this.locked);
         return t;
      }

      public static PactLedger.Term load(CompoundTag t) {
         PactLedger.Term term = new PactLedger.Term();
         term.cat = t.getString("Cat");
         term.key = t.getString("Key");
         term.a = t.getString("A");
         term.v = t.getDouble("V");
         term.x1 = t.getInt("X1");
         term.z1 = t.getInt("Z1");
         term.x2 = t.getInt("X2");
         term.z2 = t.getInt("Z2");
         term.side = t.getInt("Side");
         term.text = t.getString("Text");
         term.locked = t.getBoolean("Locked");
         return term;
      }
   }
}
