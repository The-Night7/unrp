package com.myriam.unrp.worldgen;

import java.util.List;
import java.util.Random;

/** One seed-derived site per school, independent of chunk generation order and server restarts. */
public final class SchoolSites {
    public static final int SPACING = 128, SEPARATION = 64, SALT = 91827463;
    public record School(String id, String name, int regionX, int regionZ, int floors, String palette) {}
    public record Site(int chunkX, int chunkZ) {
        public int centerX() { return chunkX * 16 + 8; }
        public int centerZ() { return chunkZ * 16 + 8; }
    }
    public static final List<School> SCHOOLS = List.of(
            new School("wellston", "Wellston", 0, 0, 3, "wellston"),
            new School("agwin", "Agwin", -1, 0, 2, "blue"),
            new School("new_bostin", "New Bostin", 0, -1, 2, "green"),
            new School("rowden", "Rowden", -1, -1, 2, "red"),
            new School("westview", "Westview", 1, 0, 2, "cyan"),
            new School("wellston_university", "Wellston University", 0, 1, 3, "purple"));
    private SchoolSites() {}
    public static School school(String id) {
        return SCHOOLS.stream().filter(s -> s.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown school: " + id));
    }
    public static Site site(long seed, School school) {
        // Vanilla random_spread / LINEAR uses the legacy Java LCG and these region salts.
        Random random = new Random(seed + school.regionX() * 341873128712L
                + school.regionZ() * 132897987541L + SALT);
        return new Site(school.regionX() * SPACING + random.nextInt(SPACING - SEPARATION),
                school.regionZ() * SPACING + random.nextInt(SPACING - SEPARATION));
    }
    public static boolean isSite(long seed, School school, int chunkX, int chunkZ) {
        Site site = site(seed, school);
        return site.chunkX() == chunkX && site.chunkZ() == chunkZ;
    }
}
