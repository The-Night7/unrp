package com.myriam.unrp.client;

import java.util.ArrayList;
import java.util.List;

/** Geometry shared by the hub graphs; no rendering or game state. */
public final class HubGraphLayout {
    private HubGraphLayout() {}

    public record Point(float x, float y) {}
    public record Tree(List<Point> nodes, int height) {}
    /** Clockwise from the top: power, speed, trick, recovery, defense. */
    public static final int[] RADAR_ORDER = {0, 4, 3, 2, 1};

    public static List<Point> radar(double[] stats, float radius) {
        double maximum = 10;
        for (double stat : stats) if (Double.isFinite(stat)) maximum = Math.max(maximum, Math.ceil(stat));
        List<Point> points = new ArrayList<>();
        for (int axis = 0; axis < 5; axis++) {
            int index = RADAR_ORDER[axis];
            double value = index < stats.length && Double.isFinite(stats[index]) ? Math.max(0, stats[index]) : 0;
            double angle = -Math.PI / 2 + axis * Math.PI * 2 / 5;
            double length = radius * Math.min(1, value / maximum);
            points.add(new Point((float) (Math.cos(angle) * length), (float) (Math.sin(angle) * length)));
        }
        return points;
    }

    /** Root first, then moves in their original order. Negative y goes up from the root. */
    public static Tree tree(int[] parents, int[] ranks, int width) {
        int count = parents.length;
        int[] levels = new int[count];
        for (int i = 0; i < count; i++) level(i, parents, ranks, levels, new boolean[count]);
        int maxLevel = 0;
        for (int level : levels) maxLevel = Math.max(maxLevel, level);
        Point[] nodes = new Point[count + 1];
        nodes[0] = new Point(width / 2f, 0);
        int row = 0;
        for (int level = 1; level <= maxLevel; level++) {
            List<Integer> group = new ArrayList<>();
            for (int i = 0; i < count; i++) if (levels[i] == level) group.add(i);
            for (int offset = 0; offset < group.size(); offset += 4) {
                row++;
                int columns = Math.min(4, group.size() - offset);
                for (int column = 0; column < columns; column++) {
                    nodes[group.get(offset + column) + 1] = new Point(width * (column + 1f) / (columns + 1), -row * 48f);
                }
            }
        }
        return new Tree(List.of(nodes), (row + 1) * 48);
    }

    private static int level(int index, int[] parents, int[] ranks, int[] levels, boolean[] visiting) {
        if (levels[index] > 0) return levels[index];
        if (visiting[index]) return 1; // malformed datapack: keep the graph usable
        visiting[index] = true;
        int value = Math.max(1, index < ranks.length ? ranks[index] - 1 : 1);
        int parent = parents[index];
        if (parent >= 0 && parent < parents.length && parent != index) {
            value = Math.max(value, level(parent, parents, ranks, levels, visiting) + 1);
        }
        visiting[index] = false;
        return levels[index] = value;
    }
}
