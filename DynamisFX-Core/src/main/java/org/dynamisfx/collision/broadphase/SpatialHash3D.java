package org.dynamisfx.collision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Uniform-grid broad phase that returns potential colliding pairs.
 */
public final class SpatialHash3D<T> implements BroadPhase3D<T> {

    private final double cellSize;

    public SpatialHash3D(double cellSize) {
        if (!Double.isFinite(cellSize) || cellSize <= 0.0) {
            throw new IllegalArgumentException("cellSize must be > 0 and finite");
        }
        this.cellSize = cellSize;
    }

    public double getCellSize() {
        return cellSize;
    }

    @Override
    public Set<CollisionPair<T>> findPotentialPairs(
            Collection<T> items,
            Function<T, Aabb> aabbProvider) {

        if (items == null || aabbProvider == null) {
            throw new IllegalArgumentException("items and aabbProvider must not be null");
        }

        Map<Cell, List<T>> buckets = new HashMap<>();
        for (T item : items) {
            if (item == null) {
                continue;
            }
            Aabb aabb = aabbProvider.apply(item);
            if (aabb == null) {
                continue;
            }
            int minCellX = cell(aabb.minX());
            int maxCellX = cell(aabb.maxX());
            int minCellY = cell(aabb.minY());
            int maxCellY = cell(aabb.maxY());
            int minCellZ = cell(aabb.minZ());
            int maxCellZ = cell(aabb.maxZ());

            for (int x = minCellX; x <= maxCellX; x++) {
                for (int y = minCellY; y <= maxCellY; y++) {
                    for (int z = minCellZ; z <= maxCellZ; z++) {
                        Cell cell = new Cell(x, y, z);
                        buckets.computeIfAbsent(cell, key -> new ArrayList<>()).add(item);
                    }
                }
            }
        }

        Set<CollisionPair<T>> pairs = new LinkedHashSet<>();
        for (List<T> bucket : buckets.values()) {
            for (int i = 0; i < bucket.size(); i++) {
                for (int j = i + 1; j < bucket.size(); j++) {
                    T first = bucket.get(i);
                    T second = bucket.get(j);
                    if (first != second) {
                        pairs.add(new CollisionPair<>(first, second));
                    }
                }
            }
        }
        return pairs;
    }

    private int cell(double coordinate) {
        return (int) Math.floor(coordinate / cellSize);
    }

    private record Cell(int x, int y, int z) {
    }
}
