package org.fxyz3d.collision;

import java.util.OptionalDouble;
import org.fxyz3d.geometry.Vector3D;

/**
 * Continuous collision detection utilities (time-of-impact style).
 */
public final class Ccd3D {

    private Ccd3D() {
    }

    /**
     * Returns first time of impact in [0,1] for segment start->end vs AABB.
     */
    public static OptionalDouble segmentAabbTimeOfImpact(Vector3D start, Vector3D end, Aabb aabb) {
        if (start == null || end == null || aabb == null) {
            throw new IllegalArgumentException("start, end and aabb must not be null");
        }
        double dirX = end.getX() - start.getX();
        double dirY = end.getY() - start.getY();
        double dirZ = end.getZ() - start.getZ();

        double tMin = 0.0;
        double tMax = 1.0;

        AxisResult x = clipAxis(start.getX(), dirX, aabb.minX(), aabb.maxX(), tMin, tMax);
        if (!x.hit()) {
            return OptionalDouble.empty();
        }
        tMin = x.tMin();
        tMax = x.tMax();

        AxisResult y = clipAxis(start.getY(), dirY, aabb.minY(), aabb.maxY(), tMin, tMax);
        if (!y.hit()) {
            return OptionalDouble.empty();
        }
        tMin = y.tMin();
        tMax = y.tMax();

        AxisResult z = clipAxis(start.getZ(), dirZ, aabb.minZ(), aabb.maxZ(), tMin, tMax);
        if (!z.hit()) {
            return OptionalDouble.empty();
        }
        tMin = z.tMin();

        return OptionalDouble.of(tMin);
    }

    public static boolean intersectsSegmentAabb(Vector3D start, Vector3D end, Aabb aabb) {
        return segmentAabbTimeOfImpact(start, end, aabb).isPresent();
    }

    /**
     * Returns first time of impact in [0,1] for moving AABB swept by delta against a static AABB.
     */
    public static OptionalDouble sweptAabbTimeOfImpact(Aabb moving, Vector3D delta, Aabb target) {
        if (moving == null || delta == null || target == null) {
            throw new IllegalArgumentException("moving, delta and target must not be null");
        }
        double halfX = moving.sizeX() * 0.5;
        double halfY = moving.sizeY() * 0.5;
        double halfZ = moving.sizeZ() * 0.5;

        Aabb expanded = new Aabb(
                target.minX() - halfX,
                target.minY() - halfY,
                target.minZ() - halfZ,
                target.maxX() + halfX,
                target.maxY() + halfY,
                target.maxZ() + halfZ);

        Vector3D start = new Vector3D(moving.centerX(), moving.centerY(), moving.centerZ());
        Vector3D end = new Vector3D(
                moving.centerX() + delta.getX(),
                moving.centerY() + delta.getY(),
                moving.centerZ() + delta.getZ());

        return segmentAabbTimeOfImpact(start, end, expanded);
    }

    /**
     * Approximates first time of impact in [0,1] for two moving convex shapes.
     *
     * This method performs sampled bracketing followed by binary refinement over GJK intersection tests.
     * For exact physics-grade TOI, replace with a full conservative advancement implementation.
     */
    public static OptionalDouble sweptConvexTimeOfImpact(
            ConvexSupport3D shapeA,
            Vector3D deltaA,
            ConvexSupport3D shapeB,
            Vector3D deltaB) {
        return sweptConvexTimeOfImpact(shapeA, deltaA, shapeB, deltaB, 32, 24);
    }

    /**
     * Approximates first time of impact in [0,1] for two moving convex shapes.
     *
     * @param samples number of coarse steps used to bracket first collision.
     * @param refinements number of bisection iterations after bracketing.
     */
    public static OptionalDouble sweptConvexTimeOfImpact(
            ConvexSupport3D shapeA,
            Vector3D deltaA,
            ConvexSupport3D shapeB,
            Vector3D deltaB,
            int samples,
            int refinements) {
        if (shapeA == null || deltaA == null || shapeB == null || deltaB == null) {
            throw new IllegalArgumentException("shape/delta arguments must not be null");
        }
        if (samples < 2) {
            throw new IllegalArgumentException("samples must be >= 2");
        }
        if (refinements < 1) {
            throw new IllegalArgumentException("refinements must be >= 1");
        }

        if (Gjk3D.intersects(shapeA, shapeB)) {
            return OptionalDouble.of(0.0);
        }

        double previousT = 0.0;
        for (int i = 1; i <= samples; i++) {
            double t = (double) i / samples;
            if (intersectsAt(shapeA, deltaA, shapeB, deltaB, t)) {
                double low = previousT;
                double high = t;
                for (int r = 0; r < refinements; r++) {
                    double mid = (low + high) * 0.5;
                    if (intersectsAt(shapeA, deltaA, shapeB, deltaB, mid)) {
                        high = mid;
                    } else {
                        low = mid;
                    }
                }
                return OptionalDouble.of(high);
            }
            previousT = t;
        }
        return OptionalDouble.empty();
    }

    private static boolean intersectsAt(
            ConvexSupport3D shapeA,
            Vector3D deltaA,
            ConvexSupport3D shapeB,
            Vector3D deltaB,
            double t) {
        ConvexSupport3D movedA = direction -> add(shapeA.support(direction), scaled(deltaA, t));
        ConvexSupport3D movedB = direction -> add(shapeB.support(direction), scaled(deltaB, t));
        return Gjk3D.intersects(movedA, movedB);
    }

    private static Vector3D scaled(Vector3D value, double scale) {
        return new Vector3D(value.getX() * scale, value.getY() * scale, value.getZ() * scale);
    }

    private static Vector3D add(Vector3D left, Vector3D right) {
        return new Vector3D(
                left.getX() + right.getX(),
                left.getY() + right.getY(),
                left.getZ() + right.getZ());
    }

    private static AxisResult clipAxis(double origin, double direction, double min, double max, double tMin, double tMax) {
        if (direction == 0.0) {
            if (origin < min || origin > max) {
                return AxisResult.miss();
            }
            return new AxisResult(true, tMin, tMax);
        }
        double inv = 1.0 / direction;
        double t0 = (min - origin) * inv;
        double t1 = (max - origin) * inv;
        if (t0 > t1) {
            double tmp = t0;
            t0 = t1;
            t1 = tmp;
        }
        double nextMin = Math.max(tMin, t0);
        double nextMax = Math.min(tMax, t1);
        if (nextMax < nextMin) {
            return AxisResult.miss();
        }
        return new AxisResult(true, nextMin, nextMax);
    }

    private record AxisResult(boolean hit, double tMin, double tMax) {
        private static AxisResult miss() {
            return new AxisResult(false, 0.0, -1.0);
        }
    }
}
