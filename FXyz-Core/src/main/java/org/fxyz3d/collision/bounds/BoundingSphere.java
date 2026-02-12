package org.fxyz3d.collision;

/**
 * Immutable bounding sphere in 3D.
 */
public record BoundingSphere(
        double centerX,
        double centerY,
        double centerZ,
        double radius) {

    public BoundingSphere {
        validateFinite(centerX, "centerX");
        validateFinite(centerY, "centerY");
        validateFinite(centerZ, "centerZ");
        validateFinite(radius, "radius");
        if (radius < 0.0) {
            throw new IllegalArgumentException("radius must be >= 0");
        }
    }

    public boolean intersects(BoundingSphere other) {
        return Intersection3D.intersects(this, other);
    }

    public boolean intersects(Aabb other) {
        return Intersection3D.intersects(this, other);
    }

    public Aabb toAabb() {
        return new Aabb(
                centerX - radius,
                centerY - radius,
                centerZ - radius,
                centerX + radius,
                centerY + radius,
                centerZ + radius);
    }

    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
