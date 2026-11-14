package com.myriam.unrp.client;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class HubGraphLayoutTest {
    @Test
    void radarPlacesPowerAtTopAndSpeedAtRight() {
        var points = HubGraphLayout.radar(new double[] {10, 2, 4, 6, 8}, 50);
        assertEquals(0, points.get(0).x(), 0.001);
        assertEquals(-50, points.get(0).y(), 0.001);
        assertEquals(40, Math.hypot(points.get(1).x(), points.get(1).y()), 0.001);
        assertEquals(10, Math.hypot(points.get(4).x(), points.get(4).y()), 0.001);
    }

    @Test
    void radarKeepsBoostedAndInvalidStatsInsideChart() {
        var points = HubGraphLayout.radar(new double[] {20, -5, Double.NaN, 3, 7}, 50);
        for (var point : points) assertTrue(Math.hypot(point.x(), point.y()) <= 50.001);
        assertEquals(0, Math.hypot(points.get(4).x(), points.get(4).y()), 0.001);
        assertEquals(0, Math.hypot(points.get(3).x(), points.get(3).y()), 0.001);
    }

    @Test
    void prerequisiteChainsGrowUpFromRootWithoutOverlapping() {
        int[] parents = {-1, -1, 1, 2, -1, 4, 5};
        var tree = HubGraphLayout.tree(parents, new int[] {0, 2, 3, 4, 2, 3, 4}, 250);
        assertEquals(8, tree.nodes().size());
        assertEquals(tree.nodes().size(), new HashSet<>(tree.nodes()).size());
        for (int i = 0; i < parents.length; i++) {
            var point = tree.nodes().get(i + 1);
            assertTrue(point.x() > 15 && point.x() < 235);
            if (parents[i] >= 0) assertTrue(point.y() < tree.nodes().get(parents[i] + 1).y());
        }
    }

    @Test
    void largeTreesWrapAndEmptyTreesRetainRoot() {
        int[] parents = new int[13];
        java.util.Arrays.fill(parents, -1);
        var tree = HubGraphLayout.tree(parents, new int[13], 250);
        assertEquals(14, tree.nodes().size());
        assertEquals(14, new HashSet<>(tree.nodes()).size());
        assertTrue(tree.height() > 190);
        assertEquals(1, HubGraphLayout.tree(new int[0], new int[0], 250).nodes().size());
    }

    @Test
    void malformedDatapackDependenciesDoNotRecurseForever() {
        var tree = HubGraphLayout.tree(new int[] {1, 0, 99, 3}, new int[4], 250);
        assertEquals(5, tree.nodes().size());
        assertTrue(tree.nodes().stream().allMatch(java.util.Objects::nonNull));
    }
}
