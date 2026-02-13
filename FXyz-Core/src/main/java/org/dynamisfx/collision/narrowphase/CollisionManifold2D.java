package org.dynamisfx.collision;

/**
 * SAT narrow-phase result containing minimum translation vector direction and depth.
 */
public record CollisionManifold2D(
        double normalX,
        double normalY,
        double penetrationDepth) {

    public CollisionManifold2D {
        if (!Double.isFinite(normalX) || !Double.isFinite(normalY) || !Double.isFinite(penetrationDepth)) {
            throw new IllegalArgumentException("values must be finite");
        }
        if (penetrationDepth < 0.0) {
            throw new IllegalArgumentException("penetrationDepth must be >= 0");
        }
        double lenSq = normalX * normalX + normalY * normalY;
        if (Math.abs(lenSq - 1.0) > 1e-6) {
            throw new IllegalArgumentException("normal must be unit length");
        }
    }
}
