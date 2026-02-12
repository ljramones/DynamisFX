package org.fxyz3d.collision;

/**
 * Contact point in 3D.
 */
public record ContactPoint3D(double x, double y, double z) {

    public ContactPoint3D {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("contact point coordinates must be finite");
        }
    }
}
