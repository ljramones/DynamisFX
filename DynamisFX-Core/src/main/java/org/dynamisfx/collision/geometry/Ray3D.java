package org.dynamisfx.collision;

/**
 * Immutable 3D ray defined by origin and direction.
 */
public record Ray3D(
        double originX,
        double originY,
        double originZ,
        double dirX,
        double dirY,
        double dirZ) {

    public Ray3D {
        validateFinite(originX, "originX");
        validateFinite(originY, "originY");
        validateFinite(originZ, "originZ");
        validateFinite(dirX, "dirX");
        validateFinite(dirY, "dirY");
        validateFinite(dirZ, "dirZ");
        if (dirX == 0.0 && dirY == 0.0 && dirZ == 0.0) {
            throw new IllegalArgumentException("Ray direction must be non-zero");
        }
    }

    private static void validateFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
