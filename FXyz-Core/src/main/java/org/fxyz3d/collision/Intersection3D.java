package org.fxyz3d.collision;

import java.util.OptionalDouble;

/**
 * Utility methods for primitive 3D intersection checks.
 */
public final class Intersection3D {

    private Intersection3D() {
    }

    public static boolean intersects(Aabb a, Aabb b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("a and b must not be null");
        }
        return a.minX() <= b.maxX() && a.maxX() >= b.minX()
                && a.minY() <= b.maxY() && a.maxY() >= b.minY()
                && a.minZ() <= b.maxZ() && a.maxZ() >= b.minZ();
    }

    public static boolean intersects(BoundingSphere a, BoundingSphere b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("a and b must not be null");
        }
        double dx = a.centerX() - b.centerX();
        double dy = a.centerY() - b.centerY();
        double dz = a.centerZ() - b.centerZ();
        double radius = a.radius() + b.radius();
        return (dx * dx) + (dy * dy) + (dz * dz) <= radius * radius;
    }

    public static boolean intersects(BoundingSphere sphere, Aabb aabb) {
        if (sphere == null || aabb == null) {
            throw new IllegalArgumentException("sphere and aabb must not be null");
        }
        double x = clamp(sphere.centerX(), aabb.minX(), aabb.maxX());
        double y = clamp(sphere.centerY(), aabb.minY(), aabb.maxY());
        double z = clamp(sphere.centerZ(), aabb.minZ(), aabb.maxZ());

        double dx = sphere.centerX() - x;
        double dy = sphere.centerY() - y;
        double dz = sphere.centerZ() - z;
        return (dx * dx) + (dy * dy) + (dz * dz) <= sphere.radius() * sphere.radius();
    }

    public static boolean intersects(Aabb aabb, BoundingSphere sphere) {
        return intersects(sphere, aabb);
    }

    /**
     * Returns the nearest non-negative distance along the ray to the first AABB intersection.
     * Empty when no intersection exists.
     */
    public static OptionalDouble rayAabbIntersectionDistance(Ray3D ray, Aabb aabb) {
        if (ray == null || aabb == null) {
            throw new IllegalArgumentException("ray and aabb must not be null");
        }

        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;

        AxisResult xAxis = axisInterval(ray.originX(), ray.dirX(), aabb.minX(), aabb.maxX());
        if (!xAxis.hit()) {
            return OptionalDouble.empty();
        }
        tMin = Math.max(tMin, xAxis.tMin());
        tMax = Math.min(tMax, xAxis.tMax());
        if (tMax < tMin) {
            return OptionalDouble.empty();
        }

        AxisResult yAxis = axisInterval(ray.originY(), ray.dirY(), aabb.minY(), aabb.maxY());
        if (!yAxis.hit()) {
            return OptionalDouble.empty();
        }
        tMin = Math.max(tMin, yAxis.tMin());
        tMax = Math.min(tMax, yAxis.tMax());
        if (tMax < tMin) {
            return OptionalDouble.empty();
        }

        AxisResult zAxis = axisInterval(ray.originZ(), ray.dirZ(), aabb.minZ(), aabb.maxZ());
        if (!zAxis.hit()) {
            return OptionalDouble.empty();
        }
        tMin = Math.max(tMin, zAxis.tMin());
        tMax = Math.min(tMax, zAxis.tMax());
        if (tMax < tMin) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(tMin);
    }

    public static boolean intersects(Ray3D ray, Aabb aabb) {
        return rayAabbIntersectionDistance(ray, aabb).isPresent();
    }

    private static AxisResult axisInterval(double origin, double direction, double min, double max) {
        if (direction == 0.0) {
            return origin >= min && origin <= max
                    ? new AxisResult(true, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
                    : new AxisResult(false, 0.0, -1.0);
        }
        double inv = 1.0 / direction;
        double t0 = (min - origin) * inv;
        double t1 = (max - origin) * inv;
        if (t0 > t1) {
            double temp = t0;
            t0 = t1;
            t1 = temp;
        }
        return new AxisResult(true, t0, t1);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record AxisResult(boolean hit, double tMin, double tMax) {
    }
}
