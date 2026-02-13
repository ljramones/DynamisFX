package org.dynamisfx.collision;

/**
 * 3D collision manifold with unit normal and penetration depth.
 */
public record CollisionManifold3D(
        double normalX,
        double normalY,
        double normalZ,
        double penetrationDepth) {

    public CollisionManifold3D {
        if (!Double.isFinite(normalX) || !Double.isFinite(normalY)
                || !Double.isFinite(normalZ) || !Double.isFinite(penetrationDepth)) {
            throw new IllegalArgumentException("values must be finite");
        }
        if (penetrationDepth < 0.0) {
            throw new IllegalArgumentException("penetrationDepth must be >= 0");
        }
        double lenSq = normalX * normalX + normalY * normalY + normalZ * normalZ;
        if (Math.abs(lenSq - 1.0) > 1e-6) {
            throw new IllegalArgumentException("normal must be unit length");
        }
    }
}
