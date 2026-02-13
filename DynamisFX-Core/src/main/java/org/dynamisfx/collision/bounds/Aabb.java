package org.dynamisfx.collision;

import javafx.geometry.Bounds;

/**
 * Immutable axis-aligned bounding box in 3D.
 */
public record Aabb(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ) {

    public Aabb {
        validateFinite(minX, "minX");
        validateFinite(minY, "minY");
        validateFinite(minZ, "minZ");
        validateFinite(maxX, "maxX");
        validateFinite(maxY, "maxY");
        validateFinite(maxZ, "maxZ");
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("Aabb min values must be <= max values");
        }
    }

    public static Aabb fromBounds(Bounds bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds must not be null");
        }
        return new Aabb(
                bounds.getMinX(),
                bounds.getMinY(),
                bounds.getMinZ(),
                bounds.getMaxX(),
                bounds.getMaxY(),
                bounds.getMaxZ());
    }

    public double centerX() {
        return (minX + maxX) * 0.5;
    }

    public double centerY() {
        return (minY + maxY) * 0.5;
    }

    public double centerZ() {
        return (minZ + maxZ) * 0.5;
    }

    public double sizeX() {
        return maxX - minX;
    }

    public double sizeY() {
        return maxY - minY;
    }

    public double sizeZ() {
        return maxZ - minZ;
    }

    public boolean intersects(Aabb other) {
        return Intersection3D.intersects(this, other);
    }

    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
